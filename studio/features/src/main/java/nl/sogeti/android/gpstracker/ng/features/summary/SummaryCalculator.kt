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
package nl.sogeti.android.gpstracker.ng.features.summary

import android.content.Context
import android.location.Location
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.features.util.DefaultResultHandler
import nl.sogeti.android.gpstracker.service.integration.ContentConstants
import nl.sogeti.android.gpstracker.service.util.Waypoint
import nl.sogeti.android.gpstracker.service.util.readTrack
import nl.sogeti.android.gpstracker.utils.contentprovider.getString
import nl.sogeti.android.gpstracker.utils.contentprovider.runQuery
import javax.inject.Inject

class SummaryCalculator {

    @Inject
    lateinit var trackTypeDescriptions: TrackTypeDescriptions
    @Inject
    lateinit var context: Context

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    fun calculateSummary(trackUri: Uri): Summary {
        // Calculate
        val handler = DefaultResultHandler()
        trackUri.readTrack(handler)
        val startTimestamp = handler.waypoints.firstOrNull()?.firstOrNull()?.time ?: 0L
        val endTimestamp = handler.waypoints.lastOrNull()?.lastOrNull()?.time ?: 0L

        val outArray = floatArrayOf(0.0F)
        var totalDistance = 0F
        val deltas = handler.waypoints.map {
            it.mapIndexed { index, rhs ->
                val lhs = if (index > 0) it[index - 1] else rhs
                val meters = distance(lhs, rhs, outArray)
                totalDistance += meters
                val milliseconds = rhs.time - lhs.time
                Summary.Delta(rhs.time, totalDistance, meters, milliseconds)
            }
        }
        var trackedPeriod = 0L
        deltas.forEach {
            it.forEach {
                trackedPeriod += it.deltaMilliseconds
            }
        }

        // Text values
        val name = trackUri.runQuery(context.contentResolver) { it.getString(ContentConstants.Tracks.NAME) }
                ?: "Unknown"
        val trackType = trackTypeDescriptions.loadTrackType(trackUri)

        // Return value

        return Summary(trackUri = trackUri,
                deltas = deltas,
                name = name,
                type = trackType.drawableId,
                startTimestamp = startTimestamp,
                stopTimestamp = endTimestamp,
                trackedPeriod = trackedPeriod,
                distance = totalDistance,
                bounds = handler.bounds,
                waypoints = handler.waypoints)
    }

    fun distance(first: Waypoint, second: Waypoint, outArray: FloatArray): Float {
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, outArray)

        return outArray[0]
    }
}
