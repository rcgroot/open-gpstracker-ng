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
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class SummaryCalculatorTest {

    @Mock
    var context: Context? = null

    var sut: SummaryCalculator? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        `when`(context!!.getString(R.string.format_kilometer)).thenReturn("%.1f KM")
        `when`(context!!.getString(R.string.format_hunderdsmeters)).thenReturn("%.0f M")
        `when`(context!!.getString(R.string.format_meters)).thenReturn("%.1f M")
        sut = SummaryCalculator()
    }

    @Test
    fun testConvertMetersToDistanceFewMeters() {
        // Act
        val distance = sut!!.convertMetersToDistance(context!!, 1.1234566F)
        // Assert
        assertThat(distance, `is`("1.1 M"))
    }

    @Test
    fun testConvertMetersToDistanceBunchOfMeters() {
        // Act
        val distance = sut!!.convertMetersToDistance(context!!, 123.123456F)
        // Assert
        assertThat(distance, `is`("123 M"))
    }


    @Test
    fun testConvertMetersToDistanceManyMeters() {
        // Act
        val distance = sut!!.convertMetersToDistance(context!!, 12345.123456F)
        // Assert
        assertThat(distance, `is`("12.3 KM"))
    }
}