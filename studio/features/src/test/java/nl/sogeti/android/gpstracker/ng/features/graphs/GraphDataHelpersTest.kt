package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class GraphDataHelpersTest {

    @Test
    fun `smooth_simple_list`(){
        // Arrange
        val points = listOf(
                GraphPoint(1F,10F),
                GraphPoint(2F,20F),
                GraphPoint(3F,30F),

                GraphPoint(4F,40F),
                GraphPoint(5F,50F),
                GraphPoint(5F,40F),
                GraphPoint(5F,30F),
                GraphPoint(5F,20F),

                GraphPoint(5F,10F),
                GraphPoint(5F,0F)
        )
        // Act
        val smooth = smoothen(points)
        // Assert
        assertThat(smooth[0].y, `is`(10F))
        assertThat(smooth[1].y, `is`(20F))
        assertThat(smooth[5].y, `is`(180F/5F))
        assertThat(smooth[8].y, `is`(10F))
        assertThat(smooth[9].y, `is`(0F))
    }
}
