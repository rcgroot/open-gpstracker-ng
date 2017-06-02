package nl.sogeti.android.gpstracker.ng.model

import android.net.Uri
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnit

class TrackSelectionTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var uri: Uri
    @Mock
    lateinit var listener: TrackSelection.Listener
    lateinit var sut: TrackSelection

    @Before
    fun setUp() {
        sut = TrackSelection()
    }

    @Test
    fun getTrackUri() {
        // Arrange
        sut.selectTrack(uri, "")
        // Act
        val trackUri = sut.trackUri
        // Assert
        Assert.assertThat(trackUri, `is`(this.uri))
    }

    @Test
    fun getTrackName() {
        // Arrange
        sut.selectTrack(uri, "mock-name")
        // Act
        val trackName = sut.trackName
        // Assert
        Assert.assertThat(trackName, `is`("mock-name"))
    }

    @Test
    fun addListener() {
        // Act
        sut.addListener(listener)
        // Assert
        sut.selectTrack(uri, "mock-name")
        Mockito.verify(listener).onTrackSelection(uri, "mock-name")
    }

    @Test
    fun removeListener() {
        sut.addListener(listener)
        // Act
        sut.removeListener(listener)
        // Assert
        sut.selectTrack(uri, "mock-name")
        Mockito.verify(listener, never()).onTrackSelection(uri, "mock-name")
    }

    @Test
    fun selectTrack() {
        // Arrange
        sut.addListener(listener)
        // Act
        sut.selectTrack(uri, "mock-name")
        // Assert
        Mockito.verify(listener).onTrackSelection(uri, "mock-name")
    }
}
