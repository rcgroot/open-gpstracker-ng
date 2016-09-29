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
package nl.sogeti.android.gpstracker.ng.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns._ID
import com.google.android.gms.maps.model.LatLng
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.integration.ContentConstants.Segments.SEGMENTS
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.NAME
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints.WAYPOINTS
import nl.sogeti.android.gpstracker.integration.ContentConstants.WaypointsColumns.*
import nl.sogeti.android.gpstracker.v2.BuildConfig
import timber.log.Timber

fun trackUri(): Uri {
    val trackUri = Uri.Builder()
            .scheme("content")
            .authority(BuildConfig.CONFIG_AUTHORITY)
            .appendPath(ContentConstants.Tracks.TRACKS)
            .build()
    return trackUri
}

fun trackUri(id:Long): Uri {
    val trackUri = Uri.Builder()
            .scheme("content")
            .authority(BuildConfig.CONFIG_AUTHORITY)
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(id.toString())
            .build()
    return trackUri
}


fun  metaDataUri(): Uri {
    val trackUri = Uri.Builder()
            .scheme("content")
            .authority(BuildConfig.CONFIG_AUTHORITY)
            .appendPath(ContentConstants.MetaData.METADATA)
            .build()
    return trackUri
}

/**
 * Loop through the complete track, its segments, its waypoints and callback the results
 *
 * @param context context through which to access the resources
 * @param handler callback for results
 * @param waypointSelection selection query split in text with ?-placeholders and the parameters.
 */
fun Uri.readTrack(context: Context, handler: ResultHandler, waypointSelection: Pair <String, List<String>>? = null) {
    if (!BuildConfig.CONFIG_AUTHORITY.equals(this.authority)) {
        return
    }

    val name = this.apply(context, { it.getString(NAME) }, listOf(NAME))
    handler.addTrack(name ?: "")
    val segmentsUri = this.append(SEGMENTS)
    segmentsUri.map(context, {
        val segmentId = it.getLong(0)
        handler.addSegment()
        val waypointsUri = segmentsUri.append(segmentId).append(WAYPOINTS)
        waypointsUri.map(context, {
            val latLng = LatLng(it.getDouble(0), it.getDouble(1))
            handler.addWaypoint(latLng, it.getLong(2))
        }, listOf(LATITUDE, LONGITUDE, TIME), waypointSelection)

    }, listOf(_ID))
}

/**
 * Build up a total of type T by applying a operation to
 * each waypoint pair along the track.
 *
 * @param context context through which to access the resources
 * @param operation increase the total T with each waypoint pair
 * @param selectionPair waypoint selection query split in text with ?-placeholders and the parameters
 */
fun <T> Uri.traverseTrack(context: Context,
                          operation: (T?, Waypoint, Waypoint) -> T,
                          selectionPair: Pair <String, List<String>>? = null): T? {
    val selectionArgs = selectionPair?.second?.toTypedArray()
    val selection = selectionPair?.first
    Timber.v("$this with selection $selection on $selectionArgs")
    val segmentsUri = this.append(SEGMENTS)
    val segments = segmentsUri.map(context, { it.getLong(ContentConstants.Segments._ID)!! })
    var result: T? = null
    for (segmentId in segments) {
        val waypointsUri = segmentsUri.append(segmentId).append(WAYPOINTS)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(waypointsUri, null, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                var first = buildWaypoint(cursor)
                var second: Waypoint
                while (cursor.moveToNext()) {
                    second = buildWaypoint(cursor)
                    result = operation(result, first, second)
                    first = second
                }
            } else {
                Timber.v("Uri $waypointsUri apply operation didn't have results")
            }
        } finally {
            cursor?.close()
        }
    }

    return result
}

private fun buildWaypoint(cursor: Cursor): Waypoint {
    return Waypoint(id = cursor.getLong(_ID) ?: -1,
            latitude = cursor.getDouble(LATITUDE) ?: 0.0,
            longitude = cursor.getDouble(LONGITUDE) ?: 0.0,
            time = cursor.getLong(TIME) ?: 0,
            speed = cursor.getDouble(SPEED) ?: 0.0,
            altitude = cursor.getDouble(ALTITUDE) ?: 0.0)
}

fun Uri.updateName(context: Context, name: String) {
    val values = ContentValues()
    values.put(ContentConstants.TracksColumns.NAME, name)
    context.contentResolver.update(this, values, null, null)
}

interface ResultHandler {

    fun addTrack(name: String)

    fun addSegment()

    fun addWaypoint(latLng: LatLng, millisecondsTime: Long)
}

data class Waypoint(val id: Long,
                    val latitude: Double,
                    val longitude: Double,
                    val time: Long,
                    val speed: Double,
                    val altitude: Double) {

}
