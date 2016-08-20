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
package nl.sogeti.android.gpstracker.ng.common

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns._ID
import com.google.android.gms.maps.model.LatLng
import nl.sogeti.android.gpstracker.integration.ContentConstants.AUTHORITY
import nl.sogeti.android.gpstracker.integration.ContentConstants.Segments.SEGMENTS
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.NAME
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints.WAYPOINTS
import nl.sogeti.android.gpstracker.integration.ContentConstants.WaypointsColumns.*
import nl.sogeti.android.gpstracker.ng.utils.append
import nl.sogeti.android.gpstracker.ng.utils.apply
import nl.sogeti.android.gpstracker.ng.utils.getString
import nl.sogeti.android.gpstracker.ng.utils.map

fun Uri.readTrack(context: Context, handler: ResultHandler, waypointSelection: Pair <String, List<String>>? = null) {
    if (!AUTHORITY.equals(this.authority)) {
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

interface ResultHandler {

    fun addTrack(name: String)

    fun addSegment()

    fun addWaypoint(latLng: LatLng, millisecondsTime: Long)
}
