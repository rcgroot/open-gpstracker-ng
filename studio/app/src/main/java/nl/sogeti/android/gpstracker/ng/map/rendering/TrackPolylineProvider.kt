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
package nl.sogeti.android.gpstracker.ng.map.rendering

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class TrackPolylineProvider(val waypoints: List<List<LatLng>>) {

    val lineOptions by lazy { drawPolylines() }

    private val LINE_SEGMENTS = 150

    private fun drawPolylines(): List<PolylineOptions> {
        val points = waypoints.filter { it.size >= 2 }
        val distribution = distribute(points)
        val lineOptions = mutableListOf<PolylineOptions>()
        for (i in points.indices) {
            val options = PolylineOptions()
                    .width(5f)
                    .color(Color.RED)
            fillLine(points[i], options, distribution[i])
            lineOptions.add(options)
        }

        return lineOptions
    }

    private fun distribute(points: List<List<LatLng>>): List<Int> {
        val distribution = mutableListOf<Int>()
        val total = points.fold(0, { count, list -> count + list.size })
        points.forEach { distribution.add((LINE_SEGMENTS * it.size) / total) }

        return distribution
    }

    private fun fillLine(points: List<LatLng>, options: PolylineOptions, goal: Int) {
        options.add(points.first())
        if (goal > 0) {
            val initialStep = points.size / goal
            val step = Math.max(1, initialStep)
            for (i in step..points.size - 1 step step) {
                options.add(points[i])
            }
        }
        options.add(points.last())
    }
}
