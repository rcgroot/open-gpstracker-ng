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
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants.*
import nl.sogeti.android.gpstracker.ng.utils.*
import timber.log.Timber
import java.util.*

class MockTracksProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val cursor = globalState.uriMap[uri]
        if (cursor == null) {
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
        globalState.uriMap.clear()
    }

    companion object globalState {
        val uriMap = mutableMapOf<Uri, MatrixCursor>()
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
            var cursor = uriMap[tracksUri]
            if (cursor == null) {
                cursor = createTracksCursor()
                uriMap[tracksUri] = cursor
            }
            addTrackCursor(cursor, trackId)
            // .../track/id
            cursor = createTracksCursor()
            val trackUri = trackUri(trackId)
            uriMap[trackUri] = cursor
            addTrackCursor(cursor, trackId)
        }

        private fun createTracksCursor() = MatrixCursor(arrayOf(Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME))

        private fun addTrackCursor(cursor: MatrixCursor, trackId: Long) {
            cursor.newRow().add(trackId).add("track $trackId").add(Date().time)
        }

        fun addSegment(trackId: Long, segmentId: Long) {
            // .../track/id/segments
            val segmentsUri = segmentsUri(trackId)
            var cursor = uriMap[segmentsUri]
            if (cursor == null) {
                cursor = createSegmentsCursor()
                uriMap[segmentsUri] = cursor
            }
            addSegmentCursor(cursor, trackId, segmentId)
            // .../track/id/segments/id
            cursor = createSegmentsCursor()
            val segmentUri = segmentUri(trackId, segmentId)
            uriMap[segmentUri] = cursor
            addSegmentCursor(cursor, trackId, segmentId)
        }

        private fun createSegmentsCursor(): MatrixCursor = MatrixCursor(arrayOf(Segments._ID, Segments.TRACK))

        private fun addSegmentCursor(cursor: MatrixCursor, trackId: Long, segmentId: Long) {
            cursor.newRow().add(segmentId).add(trackId)
        }

        fun addWaypoint(trackId: Long, segmentId: Long, waypointId: Long, latitude: Double, longitude: Double, time: Long = Date().time) {
            // .../tracks/id/segments/id/waypoints
            val waypointsUri = waypointsUri(trackId, segmentId)
            var cursor = uriMap[waypointsUri]
            if (cursor == null) {
                cursor = createWaypointsCursor()
                uriMap[waypointsUri] = cursor
            }
            addWaypointsCursor(cursor, segmentId, waypointId, latitude, longitude, time)
            // .../tracks/id/waypoints
            val waypointsTrackUri = waypointsUri(trackId)
            cursor = uriMap[waypointsTrackUri]
            if (cursor == null) {
                cursor = createWaypointsCursor()
                uriMap[waypointsTrackUri] = cursor
            }
            addWaypointsCursor(cursor, segmentId, waypointId, latitude, longitude, time)
        }

        private fun createWaypointsCursor() = MatrixCursor(arrayOf(Waypoints._ID, Waypoints.SEGMENT, Waypoints.LATITUDE, Waypoints.LONGITUDE, Waypoints.TIME))

        private fun addWaypointsCursor(cursor: MatrixCursor, segmentId: Long, waypointId: Long, latitude: Double, longitude: Double, time: Long) {
            cursor.newRow().add(waypointId).add(segmentId).add(latitude).add(longitude).add(time)
        }
    }
}
