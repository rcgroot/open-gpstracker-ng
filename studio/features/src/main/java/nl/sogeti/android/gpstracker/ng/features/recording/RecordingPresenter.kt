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
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.features.model.Preferences
import nl.sogeti.android.gpstracker.ng.features.model.valueOrFalse
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingNavigation.Companion.GPS_STATUS_PACKAGE_NAME
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.service.util.readName
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class RecordingPresenter @Inject constructor(
        private val navigation: RecordingNavigation,
        private val contentController: ContentController,
        private val gpsStatusControllerFactory: GpsStatusControllerFactory,
        private val packageManager: PackageManager,
        private val statisticsFormatter: StatisticsFormatter,
        private val summaryManager: SummaryManager,
        private val preferences: Preferences) :
        ConnectedServicePresenter(), ContentController.Listener, GpsStatusController.Listener {

    val viewModel = RecordingViewModel(null)

    private var gpsStatusController: GpsStatusController? = null


    override fun didStart() {
        super.didStart()
        summaryManager.start()

    }

    override fun willStop() {
        super.willStop()
        stopContentUpdates()
        stopGpsUpdates()
        summaryManager.stop()
    }

    //region View

    fun didSelectSignal() {
        val intent = packageManager.getLaunchIntentForPackage(GPS_STATUS_PACKAGE_NAME)
        if (intent == null) {
            navigation.showInstallHintForGpsStatusApp(context)
        } else {
            navigation.openExternalGpsStatusApp(context)
        }
    }

    //endregion

    //region Service connection

    override fun didConnectToService(context: Context, trackUri: Uri?, name: String?, loggingState: Int) {
        updateRecording(context, loggingState, name, trackUri)
    }

    override fun didChangeLoggingState(context: Context, trackUri: Uri?, name: String?, loggingState: Int) {
        updateRecording(context, loggingState, name, trackUri)
    }

    //endregion

    //region ContentController

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        readTrackSummary(contentUri)
    }

    private fun readTrackSummary(trackUri: Uri) {
        summaryManager.collectSummaryInfo(trackUri) {
            val endTime = it.waypoints.last().last().time
            val startTime = it.waypoints.first().first().time
            val speed = statisticsFormatter.convertMeterPerSecondsToSpeed(context, it.distance / (it.trackedPeriod / 1000), preferences.inverseSpeed.valueOrFalse())
            val distance = statisticsFormatter.convertMetersToDistance(context, it.distance)
            val duration = statisticsFormatter.convertSpanDescriptiveDuration(context, endTime - startTime)
            val summary = context.getString(R.string.fragment_recording_summary, distance, duration, speed)
            viewModel.summary.set(summary)
            viewModel.name.set(it.name)
        }
    }

    //endregion

    //region GPS status

    override fun onStart() {
        viewModel.isScanning.set(true)
        viewModel.hasFix.set(false)
    }

    override fun onStop() {
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
            gpsStatusController = gpsStatusControllerFactory.createGpsStatusController(context, this)
            gpsStatusController?.startUpdates()
        }
    }

    private fun stopGpsUpdates() {
        gpsStatusController?.stopUpdates()
        gpsStatusController = null
    }

    private fun updateRecording(context: Context, loggingState: Int, name: String?, trackUri: Uri?) {
        if (trackUri != null) {
            contentController.registerObserver(this, trackUri)
            viewModel.trackUri.set(trackUri)
            if (name != null) {
                viewModel.name.set(name)
            } else {
                viewModel.name.set(trackUri.readName())
            }
        }

        val isRecording = (loggingState == STATE_LOGGING) || (loggingState == STATE_PAUSED)
        viewModel.isRecording.set(isRecording)
        if (isRecording && trackUri != null) {
            startContentUpdates()
            startGpsUpdates()
            readTrackSummary(trackUri)
        } else {
            stopContentUpdates()
            stopGpsUpdates()
        }
        when (loggingState) {
            STATE_LOGGING -> viewModel.state.set(this.context.getString(R.string.state_logging))
            STATE_PAUSED -> viewModel.state.set(this.context.getString(R.string.state_paused))
            STATE_STOPPED -> viewModel.state.set(context.getString(R.string.state_stopped))
        }
    }

    //endregion
}
