package nl.sogeti.android.gpstracker.ng.features.databinding

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class FeaturesBindingComponentTest {

    lateinit var sut: FeaturesBindingComponent

    @Before
    fun setUp() {
        sut = FeaturesBindingComponent()
    }

    @Test
    fun getCommonBindingAdapters() {
        // Act
        val adapter = sut.commonBindingAdapters
        // Assert
        assertThat(adapter, `is`(notNullValue()))
    }

    @Test
    fun getRecordingBindingAdapters() {
        // Act
        val adapter = sut.recordingBindingAdapters
        // Assert
        assertThat(adapter, `is`(notNullValue()))
    }

    @Test
    fun getControlBindingAdapters() {
        // Act
        val adapter = sut.controlBindingAdapters
        // Assert
        assertThat(adapter, `is`(notNullValue()))
    }

    @Test
    fun getTracksBindingAdapters() {
        // Act
        val adapter = sut.trackTypesBindingAdapters
        // Assert
        assertThat(adapter, `is`(notNullValue()))
    }

    @Test
    fun getTrackTypesBindingAdapters() {
        // Act
        val adapter = sut.trackTypesBindingAdapters
        // Assert
        assertThat(adapter, `is`(notNullValue()))
    }

}
