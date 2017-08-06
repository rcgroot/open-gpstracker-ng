/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
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
package nl.sogeti.android.gpstracker.ng.util

import android.content.Context
import android.os.Handler
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface

class MockServiceManager : ServiceManagerInterface {

    val broadcaster = MockBroadcastSender()
    val gpsRecorder = Recorder()

    override fun startup(context: Context, runnable: Runnable?) {
        globalState.started = true
        if (globalState.loggingState == STATE_UNKNOWN) {
            globalState.loggingState = STATE_STOPPED
        }
        runnable?.run()
    }

    override fun shutdown(context: Context) {
        globalState.started = false
    }

    override fun getLoggingState(): Int = globalState.loggingState

    override fun getTrackId(): Long = globalState.trackId

    override fun startGPSLogging(context: Context, trackName: String?) {
        globalState.loggingState = STATE_LOGGING
        gpsRecorder.startRecording(trackName)
        broadcaster.sendStartedRecording(context, trackId)
    }

    override fun stopGPSLogging(context: Context) {
        globalState.loggingState = STATE_STOPPED

        broadcaster.sendStoppedRecording(context)
    }

    override fun pauseGPSLogging(context: Context) {
        globalState.loggingState = STATE_PAUSED

        broadcaster.sendPausedRecording(context, trackId)
    }

    override fun resumeGPSLogging(context: Context) {
        globalState.loggingState = STATE_LOGGING

        broadcaster.sendResumedRecording(context, trackId)
        gpsRecorder.resumeRecording()
    }

    override fun isPackageInstalled(context: Context): Boolean = true

    fun reset() {
        started = false
        globalState.loggingState = STATE_UNKNOWN
        globalState.trackId = -1L
    }

    companion object globalState {
        var loggingState = STATE_UNKNOWN
        var pauseWaypointGenerations = false
        private var started = false
        private var trackId = -1L
        private var segmentId = 10L
        private var waypointId = 100L
    }

    class Recorder {
        var shouldScheduleWaypoints = true

        fun startRecording(trackName: String?) {
            recordNewTrack(trackName)
            postNextWaypoint()
        }

        fun resumeRecording() {
            postNextWaypoint()
        }

        private fun postNextWaypoint() {
            if (shouldScheduleWaypoints) {
                Handler().postDelayed({
                    if (loggingState == STATE_LOGGING) {
                        if (!pauseWaypointGenerations) {
                            recordNewWaypoint()
                        }
                        postNextWaypoint()
                    }
                }, 2500)
            }
        }

        private fun recordNewTrack(trackName: String?) {
            if (trackId > 0) trackId++ else trackId = 2
            MockTracksContentProvider.globalState.addTrack(trackId, trackName)
            recordNewSegment()
        }

        private fun recordNewSegment() {
            segmentId++
            MockTracksContentProvider.globalState.addSegment(trackId, segmentId)
            recordNewWaypoint()
        }

        private fun recordNewWaypoint() {
            waypointId++
            val amplitude = 0.0001 + waypointId / 10000.0
            val angularSpeed = 5.0
            val latitude = 51.2605159 + amplitude * Math.cos(Math.toRadians(waypointId.toDouble() * angularSpeed))
            val longitude = 4.2301078 + amplitude * Math.sin(Math.toRadians(waypointId.toDouble() * angularSpeed))
            MockTracksContentProvider.globalState.addWaypoint(trackId, segmentId, waypointId, latitude, longitude)
        }
    }
}
