package nl.sogeti.android.gpstracker.ng.trackedit

import android.content.Context
import nl.sogeti.android.gpstracker.ng.rules.any
import nl.sogeti.android.gpstracker.ng.map.LocationFactory
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import java.text.SimpleDateFormat
import java.util.*

class NameGeneratorTest {
    private lateinit var sut: NameGenerator

    @get:Rule
    var rule = MockitoJUnit.rule()
    @Mock
    lateinit var format: SimpleDateFormat
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var locationFactory: LocationFactory

    @Before
    fun setUp() {
        `when`(context.getString(R.string.period_morning)).thenReturn("morning")
        `when`(context.getString(R.string.period_afternoon)).thenReturn("afternoon")
        `when`(context.getString(R.string.period_evening)).thenReturn("evening")
        `when`(context.getString(R.string.period_night)).thenReturn("night")
        `when`(context.getString(ArgumentMatchers.eq(R.string.initial_time_track_name), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).then {
            "${it.arguments[1]} ${it.arguments[2]}"
        }
        `when`(context.getString(ArgumentMatchers.eq(R.string.initial_time_location_track_name), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).then {
            "${it.arguments[1]} ${it.arguments[2]} in ${it.arguments[3]}"
        }

        sut = NameGenerator(format, locationFactory)
    }

    @Test
    fun generateSaterdayNight() {
        // Arrange
        val now = mock(Calendar::class.java)
        `when`(now.get(Calendar.HOUR_OF_DAY)).thenReturn(1)
        `when`(format.format(any())).thenReturn("Saturday")
        // Act
        val name = sut.generateName(context, now)
        // Assert
        assertThat(name, `is`("Saturday night"))
    }

    @Test
    fun generateMondayMorning() {
        // Arrange
        val now = mock(Calendar::class.java)
        `when`(now.get(Calendar.HOUR_OF_DAY)).thenReturn(7)
        `when`(format.format(any())).thenReturn("Monday")
        // Act
        val name = sut.generateName(context, now)
        // Assert
        assertThat(name, `is`("Monday morning"))
    }

    @Test
    fun generateWednesdayAfternoon() {
        // Arrange
        val now = mock(Calendar::class.java)
        `when`(now.get(Calendar.HOUR_OF_DAY)).thenReturn(14)
        `when`(format.format(any())).thenReturn("Wednesday")
        // Act
        val name = sut.generateName(context, now)
        // Assert
        assertThat(name, `is`("Wednesday afternoon"))
    }

    @Test
    fun generateSundayEvening() {
        // Arrange
        val now = mock(Calendar::class.java)
        `when`(now.get(Calendar.HOUR_OF_DAY)).thenReturn(21)
        `when`(format.format(any())).thenReturn("Sunday")
        // Act
        val name = sut.generateName(context, now)
        // Assert
        assertThat(name, `is`("Sunday evening"))
    }


    @Test
    fun generateSundayEveningAmsterdam() {
        // Arrange
        val now = mock(Calendar::class.java)
        `when`(now.get(Calendar.HOUR_OF_DAY)).thenReturn(21)
        `when`(format.format(any())).thenReturn("Sunday")
        `when`(locationFactory.getLocationName(context)).thenReturn("Amsterdam")
        // Act
        val name = sut.generateName(context, now)
        // Assert
        assertThat(name, `is`("Sunday evening in Amsterdam"))
    }
}
