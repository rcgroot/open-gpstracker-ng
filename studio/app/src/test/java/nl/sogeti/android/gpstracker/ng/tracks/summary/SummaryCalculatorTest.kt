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
import android.content.res.Resources
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import java.util.Calendar.*


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SummaryCalculatorTest {

    private val SIZE_SECONDS = 6 * 1000L
    private val FIVE_MINUTES = ((5 * 60) + 4) * 1000L
    private val TWO_HOURS = 2 * 60 * 60 * 1000L
    private val THREE_DAYS = 3 * 24 * 60 * 60 * 1000L

    @Mock
    var context: Context? = null
    @Mock
    var resources: Resources? = null
    var referenceDate = Calendar.getInstance()!!
    var sut = SummaryCalculator()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        `when`(context!!.getString(R.string.format_meters)).thenReturn("%.1f M")
        `when`(context!!.getString(R.string.format_100_meters)).thenReturn("%.0f M")
        `when`(context!!.getString(R.string.format_kilometer)).thenReturn("%.1f KM")
        `when`(context!!.getString(R.string.format_100_kilometer)).thenReturn("%.0f KM")
        `when`(context!!.getString(R.string.row_start_default)).thenReturn("--")

        `when`(context!!.resources).thenReturn(resources)


        `when`(resources!!.getQuantityString(R.plurals.track_duration_minutes, 1, 1)).thenReturn("1 minute")
        `when`(resources!!.getQuantityString(R.plurals.track_duration_hours, 1, 1)).thenReturn("1 hour")
        `when`(resources!!.getQuantityString(R.plurals.track_duration_days, 1, 1)).thenReturn("1 day")
        `when`(resources!!.getQuantityString(R.plurals.track_duration_minutes, 5, 5)).thenReturn("5 minutes")
        `when`(resources!!.getQuantityString(R.plurals.track_duration_hours, 2, 2)).thenReturn("2 hours")
        `when`(resources!!.getQuantityString(R.plurals.track_duration_days, 3, 3)).thenReturn("3 days")

        val sut = SummaryCalculator()
        referenceDate = Calendar.getInstance()
        referenceDate.set(YEAR, 2016)
        referenceDate.set(MONTH, 11)
        referenceDate.set(DAY_OF_MONTH, 6)
        referenceDate.set(HOUR_OF_DAY, 11)
        referenceDate.set(MINUTE, 8)
        sut.referenceTime = referenceDate
        this.sut = sut
    }

    @Test
    fun testConvertMetersToDistanceFewMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context!!, 1.1234566F)
        // Assert
        assertThat(distance, `is`("1.1 M"))
    }

    @Test
    fun testConvertMetersToDistanceBunchOfMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context!!, 123.123456F)
        // Assert
        assertThat(distance, `is`("123 M"))
    }

    @Test
    fun testConvertMetersToDistanceManyMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context!!, 12345.123456F)
        // Assert
        assertThat(distance, `is`("12.3 KM"))
    }

    @Test
    fun testConvertMetersToDistanceLotsAnLotsOfMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context!!, 123452.123456F)
        // Assert
        assertThat(distance, `is`("123 KM"))
    }

    @Test
    fun testConvertMinutes() {
        // Act
        val duration = sut.convertStartEndToDuration(context!!, 0, FIVE_MINUTES + SIZE_SECONDS)
        //
        assertThat(duration, `is`("5 minutes"))
    }

    @Test
    fun testConvertHoursAndMinutes() {
        // Act
        val duration = sut.convertStartEndToDuration(context!!, 0, TWO_HOURS + FIVE_MINUTES)
        //
        assertThat(duration, `is`("2 hours 5 minutes"))
    }

    @Test
    fun testConvertOneHour() {
        // Act
        val duration = sut.convertStartEndToDuration(context!!, 0, TWO_HOURS / 2L)
        //
        assertThat(duration, `is`("1 hour"))
    }

    @Test
    fun testConvertOnyHours() {
        // Act
        val duration = sut.convertStartEndToDuration(context!!, 0, TWO_HOURS)
        //
        assertThat(duration, `is`("2 hours"))
    }


    @Test
    fun testConvertDaysAndHours() {
        // Act
        val duration = sut.convertStartEndToDuration(context!!, 0, THREE_DAYS + TWO_HOURS + FIVE_MINUTES)
        //
        assertThat(duration, `is`("3 days 2 hours"))
    }

    @Test
    fun testNone() {
        // Arrange
        val timestamp: Long? = null
        // Act
        val timeName = sut.convertTimestampToStart(context!!, timestamp)
        // Assert
        assertThat(timeName, `is`("--"))
    }

    @Test
    fun testToday() {
        // Arrange
        val calendar = referenceDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 45)
        val timestamp = calendar.timeInMillis
        // Act
        val timeName = sut.convertTimestampToStart(context!!, timestamp)
        // Assert
        assertThat(timeName, `is`("1 hour ago"))
    }


    @Test
    fun testYesterday() {
        // Arrange
        val calendar = referenceDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 45)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val timestamp = calendar.timeInMillis
        // Act
        val timeName = sut.convertTimestampToStart(context!!, timestamp)
        // Assert
        assertThat(timeName, `is`("yesterday"))
    }

    @Test
    fun testOnWeekAgo() {
        // Arrange
        val calendar = referenceDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 45)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val timestamp = calendar.timeInMillis
        // Act
        val timeName = sut.convertTimestampToStart(context!!, timestamp)
        // Assert
        assertThat(timeName, `is`("Nov 29, 2016"))
    }

    @Test
    fun testMoreThenAWeekAgo() {
        // Arrange
        val calendar = referenceDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 45)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val timestamp = calendar.timeInMillis
        // Act
        val timeName = sut.convertTimestampToStart(context!!, timestamp)
        // Assert
        assertThat(timeName, `is`("Nov 29, 2016"))
    }

    @Test
    fun testMoreThenAYearAgo() {
        // Arrange
        val calendar = referenceDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 45)
        calendar.add(Calendar.YEAR, -1)
        val timestamp = calendar.timeInMillis
        // Act
        val timeName = sut.convertTimestampToStart(context!!, timestamp)
        // Assert
        assertThat(timeName, `is`("Dec 6, 2015"))
    }
}