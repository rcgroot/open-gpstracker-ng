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
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import timber.log.Timber

/**
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks
 */
fun tracksUri(): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .build()
}

/**
 *
 * @param trackId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5
 */
fun trackUri(trackId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .build()
}

/**
 * @param trackId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5/segments
 */
fun segmentsUri(trackId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .appendPath(SEGMENTS)
            .build()
}

/**
 * @param trackId
 * @param segmentId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5/segments/2
 */
fun segmentUri(trackId: Long, segmentId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .appendPath(SEGMENTS)
            .appendEncodedPath(segmentId.toString())
            .build()
}

/**
 * @param trackId
 * @param segmentId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5/segments/2/waypoints
 */
fun waypointsUri(trackId: Long, segmentId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .appendPath(SEGMENTS)
            .appendEncodedPath(segmentId.toString())
            .appendPath(WAYPOINTS)
            .build()
}


/**
 * @param trackId
 * @param segmentId
 * @param waypointId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5/segments/2/waypoints/21
 */
fun waypointUri(trackId: Long, segmentId: Long, waypointId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .appendPath(SEGMENTS)
            .appendEncodedPath(segmentId.toString())
            .appendPath(WAYPOINTS)
            .appendEncodedPath(waypointId.toString())
            .build()
}


/**
 * @param trackId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5/waypoints
 */
fun waypointsUri(trackId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .appendPath(WAYPOINTS)
            .build()
}

/**
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/ID/metadata
 */
fun metaDataTrackUri(id: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(id.toString())
            .appendPath(ContentConstants.MetaData.METADATA)
            .build()
}

/**
 *
 * @param trackId
 * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5
 */
fun sharedTrackUri(trackId: Long): Uri {
    return GpsTrackerApplication.appComponent.provideUriBuilder()
            .scheme("content")
            .authority(GpsTrackerApplication.appComponent.providerShareAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .build()
}


/**
 * Loop through the complete track, its segments, its waypoints and callback the results
 *
 * @param context context through which to access the resources
 * @param handler callback for results
 * @param waypointSelection selection query split in text with ?-placeholders and the parameters.
 */
fun Uri.readTrack(context: Context, handler: ResultHandler, waypointSelection: Pair<String, List<String>>? = null) {
    if (GpsTrackerApplication.appComponent.providerAuthority() != this.authority) {
        return
    }

    val name = this.apply(context, projection = listOf(NAME)) { it.getString(NAME) }
    handler.setTrack(this, name ?: "")
    val segmentsUri = this.append(SEGMENTS)
    segmentsUri.map(context, projection = listOf(_ID)) {
        val segmentId = it.getLong(0)
        handler.addSegment()
        val waypointsUri = segmentsUri.append(segmentId).append(WAYPOINTS)
        waypointsUri.map(context, waypointSelection, listOf(LATITUDE, LONGITUDE, TIME), {
            val lat = it.getDouble(LATITUDE)
            val lon = it.getDouble(LONGITUDE)
            val time = it.getLong(TIME)
            if (lat != null && lon != null && time != null) {
                val waypoint = Waypoint(latitude = lat, longitude = lon, time = time)
                handler.addWaypoint(waypoint)
            }
        })
    }
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
                          selectionPair: Pair<String, List<String>>? = null): T? {
    val selectionArgs = selectionPair?.second?.toTypedArray()
    val selection = selectionPair?.first
    Timber.v("$this with selection $selection on $selectionArgs")
    val segmentsUri = this.append(SEGMENTS)
    val segments = segmentsUri.map(context) { it.getLong(ContentConstants.Segments._ID)!! }
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

fun buildWaypoint(cursor: Cursor): Waypoint {
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

fun Uri.readName(context: Context): String {
    return this.apply(context) { it.getString(ContentConstants.TracksColumns.NAME) } ?: ""
}

fun Uri.updateCreateMetaData(context: Context, key: String, value: String) {
    val values = ContentValues()
    values.put(ContentConstants.MetaDataColumns.KEY, key)
    values.put(ContentConstants.MetaDataColumns.VALUE, value)
    val changed = context.contentResolver.update(this, values, "${ContentConstants.MetaDataColumns.KEY} = ?", arrayOf(key))
    if (changed == 0) {
        context.contentResolver.insert(this, values)
    }
}

interface ResultHandler {

    fun setTrack(uri: Uri, name: String)

    fun addSegment()

    fun addWaypoint(waypoint: Waypoint)
}

data class Waypoint(val id: Long = -1L,
                    val latitude: Double,
                    val longitude: Double,
                    val time: Long,
                    val speed: Double = Double.MIN_VALUE,
                    val altitude: Double = Double.MIN_VALUE) {
    val latLng: LatLng
        get() = LatLng(this.latitude, this.longitude)
}
