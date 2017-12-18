/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
}
