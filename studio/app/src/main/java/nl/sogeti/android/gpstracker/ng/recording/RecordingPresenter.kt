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
package nl.sogeti.android.gpstracker.ng.recording

import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerProvider
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerProvider
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.readTrack
import nl.sogeti.android.gpstracker.v2.R
import javax.inject.Inject

class RecordingPresenter constructor(private val viewModel: RecordingViewModel) : ConnectedServicePresenter(), ContentController.Listener, GpsStatusController.Listener {

    private val FIVE_MINUTES_IN_MS = 5L * 60L * 1000L
    var executingReader: TrackReader? = null
    @Inject
    lateinit var contentControllerProvider: ContentControllerProvider
    private var contentController: ContentController? = null
    @Inject
    lateinit var gpsStatusControllerProvider: GpsStatusControllerProvider
    private var gpsStatusController: GpsStatusController? = null

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    override fun willStop() {
        super.willStop()
        stopContentUpdates()
        stopGpsUpdates()
    }

    //region Service connection

    override fun didConnectToService(trackUri: Uri?, name: String?, loggingState: Int) {
        updateRecording(trackUri, loggingState, name)
    }

    override fun didChangeLoggingState(trackUri: Uri?, name: String?, loggingState: Int) {
        updateRecording(trackUri, loggingState, name)
    }

    //endregion

    //region ContentController

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        readTrackSummary(contentUri)
    }

    private fun readTrackSummary(trackUri: Uri) {
        var executingReader = this.executingReader
        if (executingReader == null || executingReader.trackUri != trackUri) {
            executingReader?.cancel(true)
            executingReader = TrackReader(trackUri, viewModel)
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
        contentController = contentControllerProvider.createContentControllerProvider(context!!, this)
        contentController?.registerObserver(viewModel.trackUri.get())
    }

    private fun stopContentUpdates() {
        contentController?.unregisterObserver()
        contentController = null
    }

    private fun startGpsUpdates() {
        if (gpsStatusController == null) {
            gpsStatusController = gpsStatusControllerProvider.createGpsStatusListenerProvider(context!!, this)
            gpsStatusController?.startUpdates()
        }
    }

    private fun stopGpsUpdates() {
        gpsStatusController?.stopUpdates()
        gpsStatusController = null
    }

    private fun updateRecording(trackUri: Uri?, loggingState: Int, name: String?) {
        if (trackUri != null) {
            contentController?.registerObserver(trackUri)
            viewModel.trackUri.set(trackUri)
        }
        if (name != null) {
            viewModel.name.set(name)
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
            STATE_LOGGING -> viewModel.state.set(context?.getString(R.string.state_logging))
            STATE_PAUSED -> viewModel.state.set(context?.getString(R.string.state_paused))
            STATE_STOPPED -> viewModel.state.set(context?.getString(R.string.state_stopped))
        }
    }

    inner class TrackReader internal constructor(val trackUri: Uri, private val viewModel: RecordingViewModel)
        : AsyncTask<Void, Void, Void>() {

        val handler = DefaultResultHandler()

        override fun doInBackground(vararg p: Void): Void? {
            val sel = ContentConstants.WaypointsColumns.TIME + " > ?"
            val args = listOf<String>(java.lang.Long.toString(System.currentTimeMillis() - FIVE_MINUTES_IN_MS))
            val selection = Pair(sel, args)
            context?.let { trackUri.readTrack(it, handler, selection) }

            var seconds = 0.0
            var meters = 0.0
            var speed = 0.0
            if (handler.headWaypoints.size > 0) {
                for (i in 1..handler.headWaypoints.size - 1) {
                    seconds += (handler.headWaypoints[i].time / 1000 - handler.headWaypoints[i - 1].time / 1000).toDouble()
                    val start = handler.headWaypoints[i]
                    val end = handler.headWaypoints[i - 1]
                    val result = FloatArray(1)
                    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result)
                    meters += result[0].toDouble()
                }
                speed = meters / 1000.0 / (seconds / 3600)
            }
            context?.let {
                val count = handler.waypoints.fold(0, { size, list -> size + list.size })
                val waypointsSummary = it.resources.getQuantityString(R.plurals.fragment_recording_waypoints, count, count)
                val summary = it.getString(R.string.fragment_recording_summary, waypointsSummary, speed, "km/h")
                viewModel.summary.set(summary)
            }
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
