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

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants.*
import nl.sogeti.android.gpstracker.ng.utils.*
import timber.log.Timber
import java.util.*

class MockTracksProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        globalState.context = context.applicationContext
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var cursor: Cursor? = null
        val content = globalState.uriContent[uri]
        if (content != null) {
            cursor = globalState.buildMatrixCursor(content.first, content.second)
        } else {
            Timber.w("Query on $uri did not match anything in global state $globalState ")
        }
        return cursor

    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    fun reset() {
        globalState.uriContent.clear()
    }

    companion object globalState {
        var context: Context? = null
        val uriContent = mutableMapOf<Uri, Pair<Array<String>, MutableList<List<Any>>>>()
            get() {
                val preload = gpxAmsterdam
                if (!preload.isEmpty()) {
                    gpxAmsterdam = listOf()
                    createTrack(1L, preload)
                }
                return field
            }

        // Some random picked points in Amsterdam, NL
        var gpxAmsterdam = listOf(Pair(52.377060, 4.898446), Pair(52.376394, 4.897263), Pair(52.376220, 4.902874), Pair(52.374049, 4.899943))

        fun createTrack(trackId: Long, waypoints: List<Pair<Double, Double>>) {
            addTrack(trackId)
            val segmentId = trackId * 10 + 1L
            addSegment(trackId, segmentId)
            val now = Date().time
            for (i in waypoints.indices) {
                val waypointId = segmentId * 10L + i
                val time = now - (waypoints.size - i) * 60000
                addWaypoint(trackId, segmentId, waypointId, waypoints[i].first, waypoints[i].second, time)
            }
        }

        fun addTrack(trackId: Long) {
            // .../track
            val tracksUri = tracksUri()
            var content = uriContent[tracksUri]
            if (content == null) {
                content = createEmptyTrackContent()
                uriContent[tracksUri] = content
            }
            addContentToTrackContent(content.second, trackId)
            context?.contentResolver?.notifyChange(tracksUri, null)
            // .../track/id
            content = createEmptyTrackContent()
            val trackUri = trackUri(trackId)
            uriContent[trackUri] = content
            addContentToTrackContent(content.second, trackId)
            context?.contentResolver?.notifyChange(trackUri, null)
        }

        private fun createEmptyTrackContent() = Pair(arrayOf(Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME), mutableListOf<List<Any>>())

        private fun addContentToTrackContent(content: MutableList<List<Any>>, trackId: Long) {
            content.add(listOf(
                    trackId,
                    "track $trackId",
                    Date().time))
        }

        fun addSegment(trackId: Long, segmentId: Long) {
            // .../track/id/segments
            val segmentsUri = segmentsUri(trackId)
            var content = uriContent[segmentsUri]
            if (content == null) {
                content = createEmptySegmentContent()
                uriContent[segmentsUri] = content
            }
            addContentToSegmentContent(content.second, trackId, segmentId)
            context?.contentResolver?.notifyChange(segmentsUri, null)
            // .../track/id/segments/id
            content = createEmptySegmentContent()
            val segmentUri = segmentUri(trackId, segmentId)
            uriContent[segmentUri] = content
            addContentToSegmentContent(content.second, trackId, segmentId)
            context?.contentResolver?.notifyChange(segmentUri, null)
        }

        private fun createEmptySegmentContent() = Pair(arrayOf(Segments._ID, Segments.TRACK), mutableListOf<List<Any>>())

        private fun addContentToSegmentContent(content: MutableList<List<Any>>, trackId: Long, segmentId: Long) {
            content.add(listOf(
                    segmentId,
                    trackId))
        }

        fun addWaypoint(trackId: Long, segmentId: Long, waypointId: Long, latitude: Double, longitude: Double, time: Long = Date().time) {
            // .../tracks/id/segments/id/waypoints
            val waypointsUri = waypointsUri(trackId, segmentId)
            var content = uriContent[waypointsUri]
            if (content == null) {
                content = createEmptyWaypointContent()
                uriContent[waypointsUri] = content
            }
            addContentToWaypointContent(content.second, segmentId, waypointId, latitude, longitude, time)
            context?.contentResolver?.notifyChange(waypointsUri, null)
            // .../tracks/id/waypoints
            val waypointsTrackUri = waypointsUri(trackId)
            content = uriContent[waypointsTrackUri]
            if (content == null) {
                content = createEmptyWaypointContent()
                uriContent[waypointsTrackUri] = content
            }
            addContentToWaypointContent(content.second, segmentId, waypointId, latitude, longitude, time)
            context?.contentResolver?.notifyChange(waypointsTrackUri, null)
        }

        private fun createEmptyWaypointContent() = Pair(arrayOf(Waypoints._ID, Waypoints.SEGMENT, Waypoints.LATITUDE, Waypoints.LONGITUDE, Waypoints.TIME), mutableListOf<List<Any>>())

        private fun addContentToWaypointContent(content: MutableList<List<Any>>, segmentId: Long, waypointId: Long, latitude: Double, longitude: Double, time: Long) {
            content.add(listOf(waypointId, segmentId, latitude, longitude, time))
        }

        fun buildMatrixCursor(columns: Array<String>, content: List<List<Any>>): Cursor? {
            val cursor = MatrixCursor(columns)
            for (row in content.iterator()) {
                cursor.addRow(row)
            }
            return cursor
        }
    }
}
