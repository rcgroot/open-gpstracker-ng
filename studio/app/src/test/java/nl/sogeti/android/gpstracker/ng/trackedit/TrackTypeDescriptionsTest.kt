package nl.sogeti.android.gpstracker.ng.trackedit

import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.Matchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TrackTypeDescriptionsTest {

    lateinit var sut: TrackTypeDescriptions

    @Before
    fun setUp() {
        sut = TrackTypeDescriptions()
    }

    @Test
    fun testSearchingBoatType() {
        // Act
        val trackType = sut.trackTypeForContentType("TYPE_BOAT")
        // Assert
        assertThat(trackType.drawableId, `is`(R.drawable.ic_track_type_boat))
        assertThat(trackType.stringId, `is`(R.string.track_type_boat))
        assertThat(trackType.contentValue, `is`("TYPE_BOAT"))
    }

    @Test
    fun testSearchingNullType() {
        // Act
        val trackType = sut.trackTypeForContentType(null)
        // Assert
        assertThat(trackType.drawableId, `is`(R.drawable.ic_track_type_default))
        assertThat(trackType.stringId, `is`(R.string.track_type_default))
        assertThat(trackType.contentValue, `is`("TYPE_DEFAULT"))
    }
}