package nl.renedegroot.android.gpx

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test

class GPXTest {

    @Test
    fun rootCreation() {
        // Act
        val result = gpx { }
        // Assert
        assertThat(result, `is`(notNullValue()))
        assertThat(result.children.size, `is`(0))
    }

    @Test
    fun metaDataCreation() {
        // Act
        val result = gpx {
            metadata { }
        }
        // Assert
        assertThat(result, `is`(notNullValue()))
        assertThat(result.children.size, `is`(1))
    }

    @Test
    fun waypointsCreation() {
        // Act
        val result =
                gpx {
                    metadata { }
                    waypoint {
                        elevation { +12345L }
                        time { +"10:12" }
                    }
                    waypoint { }
                }
        // Assert
        assertThat(result, `is`(notNullValue()))
        assertThat(result.children.size, `is`(1))
    }
}