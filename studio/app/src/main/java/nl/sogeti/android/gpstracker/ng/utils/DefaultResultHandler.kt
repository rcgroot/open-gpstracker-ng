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
package nl.sogeti.android.gpstracker.ng.utils

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class DefaultResultHandler : ResultHandler {
    private val segmentsBuilder = mutableListOf<MutableList<Waypoint>>()
    private var boundsBuilder: LatLngBounds.Builder? = null

    var uri: Uri? = null
    var name: String? = null
    val bounds: LatLngBounds by lazy {
        val builder = boundsBuilder
        if (builder != null) {
            builder.build()
        } else {
            LatLngBounds(LatLng(0.0, 0.0), LatLng(50.0, 5.0))
        }
    }
    val waypoints: List<List<Waypoint>> by lazy {
        segmentsBuilder
        segmentsBuilder.filter {
            it.count() > 1
        }
    }

    override fun setTrack(uri: Uri, name: String) {
        this.uri = uri
        this.name = name
    }

    override fun addSegment() {
        segmentsBuilder.add(mutableListOf<Waypoint>())
    }

    override fun addWaypoint(waypoint: Waypoint) {
        // Add each waypoint to the end of the last list of points (the current segment)
        segmentsBuilder.last().add(waypoint)
        // Build a bounds for the whole track
        boundsBuilder = boundsBuilder ?: LatLngBounds.Builder()
        boundsBuilder?.include(waypoint.latLng)
    }
}
