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
package nl.sogeti.android.gpstracker.ng.tracks.summary

import android.content.Context
import android.database.Cursor
import android.location.Location
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R
import java.text.DateFormat
import java.util.*

open class SummaryCalculator {

    fun calculateSummary(context: Context, trackUri: Uri): Summary {
        val waypointsUri = trackUri.append(ContentConstants.Waypoints.WAYPOINTS)

        // Defaults
        val name = trackUri.apply(context, { it.getString(ContentConstants.Tracks.NAME) }) ?: "Unknown"
        var start = context.getString(R.string.row_start_default)
        var duration = context.getString(R.string.row_duraction_default)
        var distance = context.getString(R.string.row_distance_default)
        val timestamp = 0L
        val type = R.drawable.ic_track_type_default

        // Meta-data fields
        val metadataReadOperation: (cursor: Cursor) -> Unit = {
            when (it.getString(ContentConstants.MetaData.KEY)) {
                TrackTypeDescriptions.KEY_META_FIELD_TRACK_TYPE -> TrackTypeDescriptions.convertTypeDescriptionToDrawable(it.getString(ContentConstants.MetaData.VALUE))
                else -> { }
            }
        }
        val trackId = trackUri.lastPathSegment
        metaDataUri().map(context, metadataReadOperation, listOf(ContentConstants.MetaData.KEY, ContentConstants.MetaData.VALUE), Pair("${ContentConstants.MetaData.TRACK} = ?", listOf(trackId.toString())))

        // Calculate
        val startTimestamp = waypointsUri.apply(context, { it.getLong(ContentConstants.Waypoints.TIME) })
        start = convertTimestampToStart(startTimestamp, start)
        val endTimestamp = waypointsUri.apply(context, { it.moveToLast();it.getLong(ContentConstants.Waypoints.TIME) })
        if (startTimestamp != null && endTimestamp != null && startTimestamp < endTimestamp) {
            duration = convertStartEndToDuration(startTimestamp, endTimestamp)
        }
        val operation = { distance: Float?, first: Waypoint, second: Waypoint
            ->
            distance(first, second) + (distance ?: 0.0F)
        }
        val calculated = trackUri.traverseTrack(context, operation)
        if (calculated != null && calculated > 1) {
            distance = convertMetersToDistance(context, calculated)
        }

        // Return value
        val summary = Summary(trackUri, name, type, start, duration, distance, timestamp)

        return summary
    }

    fun distance(first: Waypoint, second: Waypoint): Float {
        val result: FloatArray = floatArrayOf(0.0F)
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, result)

        return result[0]
    }

    //region Converter methods

    internal fun convertMetersToDistance(context: Context, meters: Float): String {
        val distance: String
        if (meters > 1000) {
            distance = context.getString(R.string.format_kilometer).format(meters / 1000F)
        } else if (meters >= 100) {
            distance = context.getString(R.string.format_hunderdsmeters).format(meters)
        } else {
            distance = context.getString(R.string.format_meters).format(meters)
        }
        return distance
    }

    private fun convertTimestampToStart(timestamp: Long?, default: String): String {
        //TODO Human like text
        val start: String
        if (timestamp != null) {
            start = DateFormat.getDateInstance().format(Date(timestamp))
        } else {
            start = default
        }

        return start
    }

    private fun convertStartEndToDuration(startTimestamp: Long, endTimestamp: Long): String {
        val msPerMinute = 1000 * 60
        val msPerHour = msPerMinute * 60
        val msPerDay = msPerHour * 24
        val days = (endTimestamp - startTimestamp) / msPerDay
        val hours = ((endTimestamp - startTimestamp) - (days * msPerDay)) / msPerHour
        val minutes = ((endTimestamp - startTimestamp) - (days * msPerDay) - (hours * msPerHour)) / msPerMinute
        val duration: String
        if (days > 0) {
            duration = "$days days, $hours hours\n$minutes minutes"
        } else if (hours > 0) {
            duration = "$hours hours\n$minutes minutes"
        } else {
            duration = "$minutes minutes"
        }

        return duration
    }
    //endregion
}