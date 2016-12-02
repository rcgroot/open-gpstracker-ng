package nl.sogeti.android.gpstracker.ng.trackedit

import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.Matchers.`is`
import org.junit.Assert.*
import org.junit.Test

class TrackTypeDescriptionsTest {

    @Test
    fun testSearchingBoatType() {
        // Act
        val trackType = TrackTypeDescriptions.trackTypeForContentType("TYPE_BOAT")
        // Assert
        assertThat(trackType.drawableId, `is`(R.drawable.ic_track_type_boat))
        assertThat(trackType.stringId, `is`(R.string.track_type_boat))
        assertThat(trackType.contentValue, `is`("TYPE_BOAT"))
    }

    @Test
    fun testSearchingNullType() {
        // Act
        val trackType = TrackTypeDescriptions.trackTypeForContentType(null)
        // Assert
        assertThat(trackType.drawableId, `is`(R.drawable.ic_track_type_default))
        assertThat(trackType.stringId, `is`(R.string.track_type_default))
        assertThat(trackType.contentValue, `is`("TYPE_DEFAULT"))
    }
}