/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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
package nl.sogeti.android.gpstracker.ng.graphs

import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.utils.Waypoint
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit

class GraphsPresenterTest {

    lateinit var sut: GraphsPresenter
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()!!
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var calculator: SummaryCalculator

    val gpxAmsterdam = listOf(Pair(52.377060, 4.898446), Pair(52.376394, 4.897263), Pair(52.376220, 4.902874), Pair(52.374049, 4.899943))
    val start = 1497243484247L
    val wayPoint1 = Waypoint(0, gpxAmsterdam[0].first, gpxAmsterdam[0].second, start + 180000, 0.0, 0.0)
    val wayPoint2 = Waypoint(0, gpxAmsterdam[0].first, gpxAmsterdam[0].second, wayPoint1.time + 90000, 0.0, 0.0)
    val wayPoint3 = Waypoint(0, gpxAmsterdam[0].first, gpxAmsterdam[0].second, wayPoint2.time + 60000, 0.0, 0.0)
    val wayPoint4 = Waypoint(0, gpxAmsterdam[0].first, gpxAmsterdam[0].second, wayPoint3.time + 90000, 0.0, 0.0)

    @Before
    fun setUp() {
        sut = GraphsPresenter()
        sut.calculator = calculator
        `when`(calculator.distance(any(), any(), any())).thenReturn(313.37338f)
    }

    @Test
    fun segmentPoints() {
        // Prepare
        val waypoints = listOf(wayPoint1, wayPoint2)
        // Execute
        val points = sut.calculateSpeedGraphSegment(waypoints, start, 30000)
        // Assert
        assertThat(points.count(), `is`(1))
    }
}
