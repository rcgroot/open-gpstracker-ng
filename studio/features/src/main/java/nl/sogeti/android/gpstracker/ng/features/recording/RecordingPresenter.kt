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
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.packagemanager.PackageManagerFactory
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingNavigation.Companion.GPS_STATUS_PACKAGE_NAME
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.features.util.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.ng.features.util.DefaultResultHandler
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.service.util.readName
import nl.sogeti.android.gpstracker.service.util.readTrack
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatting
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class RecordingPresenter constructor(private val viewModel: RecordingViewModel) :
        ConnectedServicePresenter<RecordingNavigation>(), ContentController.Listener, GpsStatusController.Listener {

    private var gpsStatusController: GpsStatusController? = null
    internal var executingReader: TrackReader? = null
    @Inject
    lateinit var contentControllerFactory: ContentControllerFactory
    private var contentController: ContentController? = null
    @Inject
    lateinit var gpsStatusControllerFactory: GpsStatusControllerFactory
    @Inject
    lateinit var packageManagerFactory: PackageManagerFactory
    @Inject
    lateinit var statisticsFormatting: StatisticsFormatting

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun willStop() {
        super.willStop()
        stopContentUpdates()
        stopGpsUpdates()
    }

    //region View

    fun didSelectSignal() {
        val packageManager = packageManagerFactory.createPackageManager(context)
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
        if (contentController != null) {
            readTrackSummary(contentUri)
        }
    }

    private fun readTrackSummary(trackUri: Uri) {
        var executingReader = this.executingReader
        if (executingReader == null || executingReader.trackUri != trackUri) {
            executingReader?.cancel(true)
            executingReader = TrackReader(context, trackUri, viewModel)
            executingReader.execute()
            this.executingReader = executingReader
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
        contentController = contentControllerFactory.createContentController(context, this)
        contentController?.registerObserver(viewModel.trackUri.get())
    }

    private fun stopContentUpdates() {
        contentController?.unregisterObserver()
        contentController = null
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
            contentController?.registerObserver(trackUri)
            viewModel.trackUri.set(trackUri)
            if (name != null) {
                viewModel.name.set(name)
            } else {
                viewModel.name.set(trackUri.readName(context))
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

    inner class TrackReader internal constructor(
            private val context: Context, val trackUri: Uri, private val viewModel: RecordingViewModel)
        : AsyncTask<Void, Void, Void>() {

        val handler = DefaultResultHandler()

        override fun doInBackground(vararg p: Void): Void? {
            trackUri.readTrack(context, handler)

            if (handler.waypoints.isEmpty()) {
                return null
            }

            var milliSeconds = 0L
            var meters = 0.0F
            val results = FloatArray(1)
            handler.waypoints.forEach {
                for (i in 1..it.lastIndex) {
                    val w1 = it[i - 1]
                    val w2 = it[i]
                    Location.distanceBetween(w1.latitude, w1.longitude, w2.latitude, w2.longitude, results)
                    meters += results.first()
                    milliSeconds += w2.time - w1.time
                }
            }
            val endTime = handler.waypoints.last().last().time
            val startTime = handler.waypoints.first().first().time
            val speed = statisticsFormatting.convertMeterPerSecondsToSpeed(context, meters, milliSeconds / 1000)
            val distance = statisticsFormatting.convertMetersToDistance(context, meters)
            val duration = statisticsFormatting.convertStartEndToDuration(context, startTime, endTime)
            val summary = context.getString(R.string.fragment_recording_summary, distance, duration, speed)
            viewModel.summary.set(summary)
            viewModel.name.set(handler.name)

            return null
        }

        override fun onPostExecute(result: Void?) {
            if (executingReader == this) {
                executingReader = null
            }
        }
    }

    //endregion
}
