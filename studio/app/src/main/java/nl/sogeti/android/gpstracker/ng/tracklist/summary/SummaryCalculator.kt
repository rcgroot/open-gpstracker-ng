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
package nl.sogeti.android.gpstracker.ng.tracklist.summary

import android.content.Context
import android.location.Location
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R
import java.util.*
import javax.inject.Inject

class SummaryCalculator {

    @Inject
    lateinit var timeSpanUtil: TimeSpanCalculator
    @Inject
    lateinit var locale: Locale

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun calculateSummary(context: Context, trackUri: Uri): Summary {
        val waypointsUri = trackUri.append(ContentConstants.Waypoints.WAYPOINTS)
        val trackId: Long = trackUri.lastPathSegment.toLong()

        // Defaults
        val name = trackUri.apply(context, { it.getString(ContentConstants.Tracks.NAME) }) ?: "Unknown"
        var duration = context.getString(R.string.row_duraction_default)
        var distance = context.getString(R.string.row_distance_default)
        val timestamp = 0L
        val trackType = TrackTypeDescriptions.loadTrackTypeFromContext(trackId, context)

        // Calculate
        val startTimestamp = waypointsUri.apply(context, { it.getLong(ContentConstants.Waypoints.TIME) })
        val start = convertTimestampToStart(context, startTimestamp)
        val endTimestamp = waypointsUri.apply(context, { it.moveToLast();it.getLong(ContentConstants.Waypoints.TIME) })
        if (startTimestamp != null && endTimestamp != null && startTimestamp < endTimestamp) {
            duration = convertStartEndToDuration(context, startTimestamp, endTimestamp)
        }
        val operation = { distance: Float?, first: Waypoint, second: Waypoint
            ->
            distance(first, second) + (distance ?: 0.0F)
        }
        val calculated = trackUri.traverseTrack(context, operation)
        if (calculated != null && calculated > 1) {
            distance = convertMetersToDistance(context, calculated)
        }
        val handler = DefaultResultHandler()
        trackUri.readTrack(context, handler)

        // Return value
        val summary = Summary(trackUri, name, trackType.drawableId, start, duration, distance, timestamp, handler.bound, handler.waypoints, handler.waypointCount)

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
        if (meters >= 100000) {
            distance = context.getString(R.string.format_100_kilometer).format(locale, meters / 1000F)
        } else if (meters >= 1000) {
            distance = context.getString(R.string.format_kilometer).format(locale, meters / 1000F)
        } else if (meters >= 100) {
            distance = context.getString(R.string.format_100_meters).format(locale, meters)
        } else {
            distance = context.getString(R.string.format_meters).format(locale, meters)
        }
        return distance
    }

    internal fun convertTimestampToStart(context: Context, timestamp: Long?): String {
        val start: CharSequence
        if (timestamp == null) {
            start = context.getString(R.string.row_start_default)
        } else {
            start = timeSpanUtil.getRelativeTimeSpanString(timestamp)
        }

        return start.toString()
    }

    internal fun convertStartEndToDuration(context: Context, startTimestamp: Long, endTimestamp: Long): String {
        val msPerMinute = 1000L * 60L
        val msPerHour = msPerMinute * 60L
        val msPerDay = msPerHour * 24L
        val msPerSecond = 1000L
        val msDuration = endTimestamp - startTimestamp
        val days = (msDuration / msPerDay).toInt()
        val hours = ((msDuration - (days * msPerDay)) / msPerHour).toInt()
        val minutes = ((msDuration - (days * msPerDay) - (hours * msPerHour)) / msPerMinute).toInt()
        val seconds = ((msDuration - (days * msPerDay) - (hours * msPerHour) - (minutes * msPerMinute)) / msPerSecond).toInt()
        var duration: String
        if (days > 0) {
            duration = context.resources.getQuantityString(R.plurals.track_duration_days, days, days)
            if (hours > 0) {
                duration += " "
                duration += context.resources.getQuantityString(R.plurals.track_duration_hours, hours, hours)
            }
        } else if (hours > 0) {
            duration = context.resources.getQuantityString(R.plurals.track_duration_hours, hours, hours)
            if (minutes > 0) {
                duration += " "
                duration += context.resources.getQuantityString(R.plurals.track_duration_minutes, minutes, minutes)
            }
        } else if (minutes > 0) {
            duration = context.resources.getQuantityString(R.plurals.track_duration_minutes, minutes, minutes)
        } else {
            duration = context.resources.getQuantityString(R.plurals.track_duration_seconds, seconds, seconds)
        }

        return duration
    }
    //endregion
}