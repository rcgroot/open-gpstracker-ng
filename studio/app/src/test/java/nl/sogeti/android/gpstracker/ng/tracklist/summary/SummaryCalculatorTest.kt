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
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import java.util.*
import java.util.Calendar.*

class SummaryCalculatorTest {

    private val SIX_SECONDS = 6 * 1000L
    private val FIVE_MINUTES = ((5 * 60) + 4) * 1000L
    private val TWO_HOURS = 2 * 60 * 60 * 1000L
    private val THREE_DAYS = 3 * 24 * 60 * 60 * 1000L

    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var resources: Resources
    @Mock
    lateinit var timeSpanCalculator: TimeSpanCalculator
    val locale = Locale.US
    var referenceDate = Calendar.getInstance()!!
    lateinit var sut: SummaryCalculator

    @Before
    fun setup() {
        val sut = SummaryCalculator()
        sut.timeSpanUtil = timeSpanCalculator
        sut.locale = locale
        this.sut = sut

        `when`(context.getString(R.string.format_speed)).thenReturn("%.0f mock")
        `when`(context.getString(R.string.format_small_meters)).thenReturn("%.1f M")
        `when`(context.getString(R.string.format_small_100_meters)).thenReturn("%.0f M")
        `when`(context.getString(R.string.format_big_kilometer)).thenReturn("%.1f KM")
        `when`(context.getString(R.string.format_big_100_kilometer)).thenReturn("%.0f KM")
        `when`(context.getString(R.string.row_start_default)).thenReturn("--")

        `when`(context.resources).thenReturn(resources)
        `when`(resources.getQuantityString(R.plurals.track_duration_seconds, 1, 1)).thenReturn("1 second")
        `when`(resources.getQuantityString(R.plurals.track_duration_seconds, 6, 6)).thenReturn("6 seconds")
        `when`(resources.getQuantityString(R.plurals.track_duration_minutes, 1, 1)).thenReturn("1 minute")
        `when`(resources.getQuantityString(R.plurals.track_duration_minutes, 5, 5)).thenReturn("5 minutes")
        `when`(resources.getQuantityString(R.plurals.track_duration_hours, 2, 2)).thenReturn("2 hours")
        `when`(resources.getQuantityString(R.plurals.track_duration_hours, 1, 1)).thenReturn("1 hour")
        `when`(resources.getQuantityString(R.plurals.track_duration_days, 1, 1)).thenReturn("1 day")
        `when`(resources.getQuantityString(R.plurals.track_duration_days, 3, 3)).thenReturn("3 days")
        `when`(resources.getString(R.string.mps_to_speed)).thenReturn("3.6")
        `when`(resources.getString(R.string.m_to_big_distance)).thenReturn("1000.0")
        `when`(resources.getString(R.string.m_to_small_distance)).thenReturn("1.0")

        referenceDate = Calendar.getInstance()
        referenceDate.set(YEAR, 2016)
        referenceDate.set(MONTH, 11)
        referenceDate.set(DAY_OF_MONTH, 6)
        referenceDate.set(HOUR_OF_DAY, 11)
        referenceDate.set(MINUTE, 8)
    }

    @Test
    fun testConvertMetersToDistanceFewMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context, 1.1234566F)
        // Assert
        assertThat(distance, `is`("1.1 M"))
    }

    @Test
    fun testConvertMetersToDistanceBunchOfMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context, 123.123456F)
        // Assert
        assertThat(distance, `is`("123 M"))
    }

    @Test
    fun testConvertMetersToDistanceManyMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context, 12345.123456F)
        // Assert
        assertThat(distance, `is`("12.3 KM"))
    }

    @Test
    fun testConvertMetersToDistanceLotsAnLotsOfMeters() {
        // Act
        val distance = sut.convertMetersToDistance(context, 123452.123456F)
        // Assert
        assertThat(distance, `is`("123 KM"))
    }

    @Test
    fun testConvertOneSecond() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, SIX_SECONDS / 6L)
        //
        assertThat(duration, `is`("1 second"))
    }

    @Test
    fun testConvertSeconds() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, SIX_SECONDS)
        //
        assertThat(duration, `is`("6 seconds"))
    }

    @Test
    fun testConvertOneMinute() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, FIVE_MINUTES / 5L + SIX_SECONDS)
        //
        assertThat(duration, `is`("1 minute"))
    }

    @Test
    fun testConvertMinutes() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, FIVE_MINUTES + SIX_SECONDS)
        //
        assertThat(duration, `is`("5 minutes"))
    }

    @Test
    fun testConvertHoursAndMinutes() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, TWO_HOURS + FIVE_MINUTES)
        //
        assertThat(duration, `is`("2 hours 5 minutes"))
    }

    @Test
    fun testConvertOneHour() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, TWO_HOURS / 2L)
        //
        assertThat(duration, `is`("1 hour"))
    }

    @Test
    fun testConvertOnyHours() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, TWO_HOURS)
        //
        assertThat(duration, `is`("2 hours"))
    }


    @Test
    fun testConvertDaysAndHours() {
        // Act
        val duration = sut.convertStartEndToDuration(context, 0, THREE_DAYS + TWO_HOURS + FIVE_MINUTES)
        //
        assertThat(duration, `is`("3 days 2 hours"))
    }

    @Test
    fun testNone() {
        // Arrange
        val timestamp: Long? = null
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
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
        `when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("1 hour ago")
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
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
        `when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("yesterday")
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
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
        `when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("Nov 29, 2016")
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
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
        `when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("Nov 29, 2016")
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
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
        `when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("Dec 6, 2015")
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
        // Assert
        assertThat(timeName, `is`("Dec 6, 2015"))
    }

    @Test
    fun testMPStoOneKMP() {
        // Arrange
        sut.locale = Locale.GERMAN
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 1000.0F, 3600)
        // Assert
        assertThat(speed, `is`("1 mock"))
    }

    @Test
    fun testMPStoTenKMP() {
        // Arrange
        sut.locale = Locale.GERMAN
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 10000.0F, 3600)
        // Assert
        assertThat(speed, `is`("10 mock"))
    }


    @Test
    fun testMPStoThreeKMP() {
        // Arrange
        sut.locale = Locale.GERMAN
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 10000.0F, 10800)
        // Assert
        assertThat(speed, `is`("3 mock"))
    }
}
