package nl.sogeti.android.gpstracker.ng.utils

import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class TrackUriExtensionTests {

    @get:Rule
    var appComponentRule = MockAppComponentTestRule()

    @Test
    fun testBuildTracksUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = tracksUri()
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        assertThat(uri, notNullValue())
    }

    @Test
    fun testBuildTrackIdUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = trackUri(123L)
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        Mockito.verify(builder).appendEncodedPath("123")
        assertThat(uri, notNullValue())
    }

    @Test
    fun testBuildSegmentsUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = segmentsUri(123L)
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        Mockito.verify(builder).appendEncodedPath("123")
        Mockito.verify(builder).appendPath("segments")
        assertThat(uri, notNullValue())
    }

    @Test
    fun testBuildSegmentIdUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = segmentUri(123L, 543L)
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        Mockito.verify(builder).appendEncodedPath("123")
        Mockito.verify(builder).appendPath("segments")
        Mockito.verify(builder).appendEncodedPath("543")
        assertThat(uri, notNullValue())
    }

    @Test
    fun testBuildWaypointsUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = waypointsUri(123L, 543L)
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        Mockito.verify(builder).appendEncodedPath("123")
        Mockito.verify(builder).appendPath("segments")
        Mockito.verify(builder).appendEncodedPath("543")
        Mockito.verify(builder).appendPath("waypoints")
        assertThat(uri, notNullValue())
    }

    @Test
    fun testBuildTrackWaypointsUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = waypointsUri(123L)
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        Mockito.verify(builder).appendEncodedPath("123")
        Mockito.verify(builder).appendPath("waypoints")
        assertThat(uri, notNullValue())
    }

    @Test
    fun testBuildTrackMetadataUri() {
        // Act
        val builder = GpsTrackerApplication.appComponent.provideUriBuilder()
        val uri = metaDataTrackUri(123L)
        // Assert
        Mockito.verify(builder).scheme("content")
        Mockito.verify(builder).authority("mock-authority")
        Mockito.verify(builder).appendPath("tracks")
        Mockito.verify(builder).appendEncodedPath("123")
        Mockito.verify(builder).appendPath("metadata")
        assertThat(uri, notNullValue())
    }
}