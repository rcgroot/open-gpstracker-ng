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
package nl.sogeti.android.gpstracker.ng.features.graphs

import android.databinding.ObservableField
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph

class GraphsViewModel {
    val trackUri = ObservableField<Uri?>()
    val waypoints = ObservableField<String>("-")
    val speedAtTimeData = ObservableField<List<GraphPoint>>(emptyList())
    val speedValueDescription = ObservableField<LineGraph.ValueDescriptor>(object : LineGraph.ValueDescriptor {})

    val startDate = ObservableField<Long>(0L)
    val startTime = ObservableField<Long>(0L)
    val time = ObservableField<Long>(0L)
    val paused = ObservableField<Long>(0)

    val total = ObservableField<Long>(0L)
    val distance = ObservableField<String>("-")
    val speed = ObservableField<String>("-")
}
