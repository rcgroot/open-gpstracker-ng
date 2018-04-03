package nl.sogeti.android.gpstracker.v2.sharedwear.util

import android.content.Context
import android.content.res.Resources
import nl.sogeti.android.gpstracker.v2.sharedwear.R
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import java.util.*

class StatisticsFormatterTest {

    private val SIX_SECONDS = 6 * 1000L
    private val FIVE_MINUTES = ((5 * 60) + 4) * 1000L
    private val TWO_HOURS = 2 * 60 * 60 * 1000L
    private val THREE_DAYS = 3 * 24 * 60 * 60 * 1000L

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var resources: Resources
    @Mock
    lateinit var timeSpanCalculator: TimeSpanCalculator
    @Mock
    lateinit var localeProvider: LocaleProvider
    var referenceDate = Calendar.getInstance()!!
    lateinit var sut: StatisticsFormatter

    @Before
    fun setup() {
        val sut = StatisticsFormatter(localeProvider, timeSpanCalculator)
        this.sut = sut

        Mockito.`when`(context.getString(R.string.format_speed)).thenReturn("%.0f mock")
        Mockito.`when`(context.getString(R.string.format_runners_speed)).thenReturn("%1\$.0f:%2\$02.0f %3\$s")
        Mockito.`when`(context.getString(R.string.format_small_meters)).thenReturn("%.1f M")
        Mockito.`when`(context.getString(R.string.format_small_100_meters)).thenReturn("%.0f M")
        Mockito.`when`(context.getString(R.string.format_big_kilometer)).thenReturn("%.1f KM")
        Mockito.`when`(context.getString(R.string.format_big_100_kilometer)).thenReturn("%.0f KM")
        Mockito.`when`(context.getString(R.string.empty_dash)).thenReturn("-")

        Mockito.`when`(context.resources).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_seconds, 1, 1)).thenReturn("1 second")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_seconds, 6, 6)).thenReturn("6 seconds")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_minutes, 1, 1)).thenReturn("1 minute")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_minutes, 5, 5)).thenReturn("5 minutes")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_hours, 2, 2)).thenReturn("2 hours")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_hours, 1, 1)).thenReturn("1 hour")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_days, 1, 1)).thenReturn("1 day")
        Mockito.`when`(resources.getQuantityString(R.plurals.track_duration_days, 3, 3)).thenReturn("3 days")
        Mockito.`when`(resources.getString(R.string.mps_to_speed)).thenReturn("3.6")
        Mockito.`when`(resources.getString(R.string.spm_to_speed)).thenReturn("0.06")
        Mockito.`when`(resources.getString(R.string.m_to_big_distance)).thenReturn("1000.0")
        Mockito.`when`(resources.getString(R.string.m_to_small_distance)).thenReturn("1.0")
        Mockito.`when`(resources.getString(R.string.speed_runners_unit)).thenReturn("min")

        referenceDate = Calendar.getInstance()
        referenceDate.set(Calendar.YEAR, 2016)
        referenceDate.set(Calendar.MONTH, 11)
        referenceDate.set(Calendar.DAY_OF_MONTH, 6)
        referenceDate.set(Calendar.HOUR_OF_DAY, 11)
        referenceDate.set(Calendar.MINUTE, 8)
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
        val duration = sut.convertSpanDescriptiveDuration(context, SIX_SECONDS / 6L)
        //
        assertThat(duration, `is`("1 second"))
    }

    @Test
    fun testConvertSeconds() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, SIX_SECONDS)
        //
        assertThat(duration, `is`("6 seconds"))
    }

    @Test
    fun testConvertOneMinute() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, FIVE_MINUTES / 5L + SIX_SECONDS)
        //
        assertThat(duration, `is`("1 minute"))
    }

    @Test
    fun testConvertMinutes() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, FIVE_MINUTES + SIX_SECONDS)
        //
        assertThat(duration, `is`("5 minutes"))
    }

    @Test
    fun testConvertHoursAndMinutes() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, TWO_HOURS + FIVE_MINUTES)
        //
        assertThat(duration, `is`("2 hours 5 minutes"))
    }

    @Test
    fun testConvertOneHour() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, TWO_HOURS / 2L)
        //
        assertThat(duration, `is`("1 hour"))
    }

    @Test
    fun testConvertOnyHours() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, TWO_HOURS)
        //
        assertThat(duration, `is`("2 hours"))
    }


    @Test
    fun testConvertDaysAndHours() {
        // Act
        val duration = sut.convertSpanDescriptiveDuration(context, THREE_DAYS + TWO_HOURS + FIVE_MINUTES)
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
        assertThat(timeName, `is`("-"))
    }

    @Test
    fun testToday() {
        // Arrange
        val calendar = referenceDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 45)
        val timestamp = calendar.timeInMillis
        Mockito.`when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("1 hour ago")
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
        Mockito.`when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("yesterday")
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
        Mockito.`when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("Nov 29, 2016")
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
        Mockito.`when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("Nov 29, 2016")
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
        Mockito.`when`(timeSpanCalculator.getRelativeTimeSpanString(timestamp)).thenReturn("Dec 6, 2015")
        // Act
        val timeName = sut.convertTimestampToStart(context, timestamp)
        // Assert
        assertThat(timeName, `is`("Dec 6, 2015"))
    }

    @Test
    fun testMPStoOneKMP() {
        // Arrange
        `when`(localeProvider.getLocale()).thenReturn(Locale.GERMAN)
        sut = StatisticsFormatter(localeProvider, timeSpanCalculator)
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 1000.0F / 3600, false)
        // Assert
        assertThat(speed, `is`("1 mock"))
    }

    @Test
    fun testMPStoTenKMP() {
        // Arrange
        `when`(localeProvider.getLocale()).thenReturn(Locale.GERMAN)
        sut = StatisticsFormatter(localeProvider, timeSpanCalculator)
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 10000.0F / 3600, false)
        // Assert
        assertThat(speed, `is`("10 mock"))
    }

    @Test
    fun testMPStoThreeKMP() {
        // Arrange
        sut = StatisticsFormatter(localeProvider, timeSpanCalculator)
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 10000.0F / 10800, false)
        // Assert
        assertThat(speed, `is`("3 mock"))
    }

    @Test
    fun `convert 10 kmp to 6 minutes per kilometer`() {
        // Arrange
        sut = StatisticsFormatter(localeProvider, timeSpanCalculator)
        // Act
        val speed = sut.convertMeterPerSecondsToSpeed(context, 10000.0F / 3600, true)
        // Assert
        assertThat(speed, `is`("6:00 min"))
    }
}
