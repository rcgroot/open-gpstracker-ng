/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.features.recording

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.support.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.AbstractPresenter
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateController
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateListener
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.*
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class RecordingPresenter :
        AbstractPresenter(),
        ContentController.Listener,
        GpsStatusController.Listener,
        LoggingStateListener {

    internal val viewModel = RecordingView(null)
    private var gpsStatusController: GpsStatusController? = null
    @Inject
    lateinit var contentController: ContentController
    @Inject
    lateinit var gpsStatusControllerFactory: GpsStatusControllerFactory
    @Inject
    lateinit var packageManager: PackageManager
    @Inject
    lateinit var loggingStateController: LoggingStateController
    @Inject
    lateinit var summaryManager: SummaryManager

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onFirstStart() {
        super.onFirstStart()
        loggingStateController.connect(this)
    }

    override fun onStart() {
        super.onStart()
        summaryManager.start()
    }

    @WorkerThread
    override fun onChange() {
        val trackUri = loggingStateController.trackUri
        val state = loggingStateController.loggingState
        val isRecording = (state == STATE_LOGGING) || (state == STATE_PAUSED)
        viewModel.isRecording.set(isRecording)
        when (state) {
            STATE_LOGGING -> viewModel.state.set(R.string.state_logging)
            STATE_PAUSED -> viewModel.state.set(R.string.state_paused)
            STATE_STOPPED -> viewModel.state.set(R.string.state_stopped)
        }
        if (isRecording) {
            startContentUpdates()
            startGpsUpdates()
            readTrackSummary(trackUri)
        } else {
            stopContentUpdates()
            stopGpsUpdates()
        }

    }

    override fun onStop() {
        super.onStop()
        stopContentUpdates()
        stopGpsUpdates()
        summaryManager.stop()
    }

    override fun onCleared() {
        loggingStateController.disconnect()
        super.onCleared()
    }

    //region View

    fun didSelectSignal() {
        val intent = packageManager.getLaunchIntentForPackage(GPS_STATUS_PACKAGE_NAME)
        if (intent == null) {
            viewModel.navigation.value = Navigation.GpsStatusAppInstallHint()
        } else {
            viewModel.navigation.value = Navigation.GpsStatusAppOpen()
        }
    }

    //endregion

    //region Service connection

    override fun didConnectToService(context: Context, loggingState: Int, trackUri: Uri?) {
        didChangeLoggingState(context, loggingState, trackUri)
    }

    override fun didChangeLoggingState(context: Context, loggingState: Int, trackUri: Uri?) {
        updateRecording(loggingStateController.trackUri)
    }

    //endregion

    //region ContentController

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        readTrackSummary(contentUri)
    }

    private fun readTrackSummary(trackUri: Uri?) {
        if (trackUri == null) {
            viewModel.summary.set(SummaryText(R.string.fragment_recording_summary, 0F, false, 0F, 0))
            viewModel.name.set(null)
        } else {
            summaryManager.collectSummaryInfo(trackUri) {
                val endTime = it.waypoints.last().last().time
                val startTime = it.waypoints.first().first().time
                val meterPerSecond = it.distance / (it.trackedPeriod / 1000)
                val isRunners = it.type.isRunning()
                viewModel.summary.set(SummaryText(R.string.fragment_recording_summary, meterPerSecond, isRunners, it.distance, endTime - startTime))
                viewModel.name.set(it.name)
            }
        }
    }

    //endregion

    //region GPS status

    override fun onStartListening() {
        viewModel.isScanning.set(true)
        viewModel.hasFix.set(false)
    }

    override fun onStopListening() {
        viewModel.hasFix.set(false)
        viewModel.isScanning.set(false)
        onChange(0, 0)
    }

    override fun onChange(usedSatellites: Int, maxSatellites: Int) {
        viewModel.currentSatellites.set(usedSatellites)
        viewModel.maxSatellites.set(maxSatellites)
        when {
            usedSatellites >= 10 -> viewModel.signalQuality.set(excellent)
            usedSatellites >= 8 -> viewModel.signalQuality.set(high)
            usedSatellites >= 6 -> viewModel.signalQuality.set(medium)
            usedSatellites >= 4 -> viewModel.signalQuality.set(low)
            else -> viewModel.signalQuality.set(none)
        }
    }


    override fun onFirstFix() {
        viewModel.hasFix.set(true)
        viewModel.signalQuality.set(excellent)
    }

    //endregion

    //region Private

    private fun startContentUpdates() {
        contentController.registerObserver(this, viewModel.trackUri.get())
    }

    private fun stopContentUpdates() {
        contentController.unregisterObserver()
    }

    private fun startGpsUpdates() {
        if (gpsStatusController == null) {
            gpsStatusController = gpsStatusControllerFactory.createGpsStatusController(this)
            gpsStatusController?.startUpdates()
        }
    }

    private fun stopGpsUpdates() {
        gpsStatusController?.stopUpdates()
        gpsStatusController = null
    }

    private fun updateRecording(trackUri: Uri?) {
        viewModel.trackUri.set(trackUri)
        if (trackUri != null) {
            contentController.registerObserver(this, trackUri)
        } else {
            contentController.unregisterObserver()
        }
        markDirty()
    }

    //endregion
}
