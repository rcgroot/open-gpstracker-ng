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
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.support.annotation.StringRes
import android.text.format.DateFormat
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
    @Inject
    lateinit var trackTypeDescriptions: TrackTypeDescriptions

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun calculateSummary(context: Context, trackUri: Uri): Summary {
        // Calculate
        val handler = DefaultResultHandler()
        trackUri.readTrack(context, handler)
        val startTimestamp = handler.waypoints.firstOrNull()?.firstOrNull()?.time ?: 0L
        val endTimestamp = handler.waypoints.lastOrNull()?.lastOrNull()?.time ?: 0L

        data class Data(val meter: Float, val time: Long)

        fun reduce(waypoints: List<Waypoint>): Data {
            var meters = 0.0F
            var time = 0L
            val outArray = floatArrayOf(0.0F)
            for (i in 0..waypoints.lastIndex - 1) {
                meters += distance(waypoints[i], waypoints[i + 1], outArray)
                time += waypoints[i + 1].time - waypoints[i].time
            }

            return Data(meters, time)
        }

        val sum = handler.waypoints.map { reduce(it) }.fold(Data(0.0F, 0)) { first, second ->
            Data(first.meter + second.meter, first.time + second.time)
        }
        // Text values
        val name = trackUri.apply(context) { it.getString(ContentConstants.Tracks.NAME) } ?: "Unknown"
        val trackType = trackTypeDescriptions.loadTrackType(context, trackUri)

        // Return value
        val summary = Summary(trackUri = trackUri, name = name, type = trackType.drawableId,
                startTimestamp = startTimestamp, stopTimestamp = endTimestamp,
                trackedPeriod = sum.time, distance = sum.meter, bounds = handler.bounds, waypoints = handler.waypoints)

        return summary
    }

    fun distance(first: Waypoint, second: Waypoint, outArray: FloatArray): Float {
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, outArray)

        return outArray[0]
    }

    //region Converter methods

    fun convertMetersToDistance(context: Context, meters: Float): String {
        val distance: String
        if (meters >= 100000) {
            val convert = context.resources.getFloat(R.string.m_to_big_distance)
            distance = context.getString(R.string.format_big_100_kilometer).format(locale, meters / convert)
        } else if (meters >= 1000) {
            val convert = context.resources.getFloat(R.string.m_to_big_distance)
            distance = context.getString(R.string.format_big_kilometer).format(locale, meters / convert)
        } else if (meters >= 100) {
            val convert = context.resources.getFloat(R.string.m_to_small_distance)
            distance = context.getString(R.string.format_small_100_meters).format(locale, meters / convert)
        } else {
            val convert = context.resources.getFloat(R.string.m_to_small_distance)
            distance = context.getString(R.string.format_small_meters).format(locale, meters / convert)
        }
        return distance
    }

    fun convertTimestampToStart(context: Context, timestamp: Long?): String {
        val start: CharSequence
        if (timestamp == null || timestamp == 0L) {
            start = context.getString(R.string.row_start_default)
        } else {
            start = timeSpanUtil.getRelativeTimeSpanString(timestamp)
        }

        return start.toString()
    }

    fun convertStartEndToDuration(context: Context, startTimestamp: Long, endTimestamp: Long): String {
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

    fun convertMeterPerSecondsToSpeed(context: Context, meters: Float, seconds: Long): String {
        val conversion = context.resources.getFloat(R.string.mps_to_speed)
        val unit = context.resources.getString(R.string.speed_unit)
        val kph = meters / seconds * conversion

        return context.getString(R.string.format_speed).format(locale, kph, unit)
    }

    fun convertTimestampToDate(context: Context, startTimestamp: Long): String {
        val date = Date(startTimestamp)
        return DateFormat.getDateFormat(context).format(date)
    }

    fun convertTimestampToTime(context: Context, startTimestamp: Long): String {
        val date = Date(startTimestamp)
        return DateFormat.getTimeFormat(context).format(date)
    }

    //endregion
}

private fun Resources.getFloat(@StringRes resourceId: Int): Float {
    val stringValue = this.getString(resourceId)
    return stringValue.toFloat()
}
