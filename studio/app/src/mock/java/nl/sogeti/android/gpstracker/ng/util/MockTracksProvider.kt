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
import nl.sogeti.android.gpstracker.ng.utils.segmentsUri
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import nl.sogeti.android.gpstracker.ng.utils.tracksUri
import nl.sogeti.android.gpstracker.ng.utils.waypointsUri
import timber.log.Timber
import java.util.*

class MockTracksProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        loadFiveRecentWaypoints(1)
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

    fun loadFiveRecentWaypoints(trackId: Long) {
        globalState.uriMap[tracksUri()] = createTracksCursor(listOf(trackId))
        val trackUri = trackUri(trackId)
        val segmentsUri = segmentsUri(trackId)
        globalState.uriMap[trackUri] = createTrackCursor(trackId)
        val segmentIds = listOf(trackId * 10 + 1L)
        globalState.uriMap[segmentsUri] = createSegmentsCursor(trackId, segmentIds)
        for (segmentId in segmentIds) {
            val waypointsUri = waypointsUri(trackId, segmentId)
            val waypointIds = listOf(segmentId * 10 + 1L, segmentId * 10 + 2L, segmentId * 10 + 3L, segmentId * 10 + 4L)
            globalState.uriMap[waypointsUri] = createWaypointsCursor(segmentId, waypointIds)
        }
    }

    private fun createTracksCursor(trackIds: List<Long>): MatrixCursor {
        val cursor = MatrixCursor(arrayOf(Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME))
        for (trackId in trackIds) {
            cursor.newRow().add(trackId).add("track $trackId").add(Date().time)
        }

        return cursor
    }

    private fun createTrackCursor(trackId: Long): MatrixCursor {
        val cursor = MatrixCursor(arrayOf(Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME))
        cursor.newRow().add(trackId).add("track $trackId").add(Date().time)

        return cursor
    }

    private fun createSegmentsCursor(trackId: Long, segmentIds: List<Long>): MatrixCursor {
        val cursor = MatrixCursor(arrayOf(Segments._ID, Segments.TRACK))
        for (segmentId in segmentIds) {
            cursor.newRow().add(segmentId).add(trackId)
        }

        return cursor
    }

    private fun createWaypointsCursor(segmentId: Long, waypointIds: List<Long>): MatrixCursor {
        val cursor = MatrixCursor(arrayOf(Waypoints._ID, Waypoints.SEGMENT, Waypoints.LATITUDE, Waypoints.LONGITUDE, Waypoints.TIME))
        val now = Date().time
        for (i in waypointIds.indices) {
            cursor.newRow().add(waypointIds[i]).add(segmentId).add(gpxAmsterdam[i].first).add(gpxAmsterdam[i].second).add(now - (waypointIds.size - i) * 60000)
        }

        return cursor
    }

    fun reset() {
        globalState.uriMap.clear()
    }

    companion object globalState {
        val uriMap = mutableMapOf<Uri, MatrixCursor>()
        // Some random picked points in Amsterdam, NL
        val gpxAmsterdam = listOf(Pair(52.377060, 4.898446), Pair(52.376394, 4.897263), Pair(52.376220, 4.902874), Pair(52.374049, 4.899943))
    }
}
