package nl.sogeti.android.gpstracker.ng.features.graphs

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.junit.Before
import org.junit.Test

class GraphSpeedConverterTest {

    private lateinit var sut: GraphSpeedConverter

    @Before
    fun setUp() {
        sut = GraphSpeedConverter()
    }

    @Test
    fun roundTrip() {
        listOf(1F, 2F, 3F, 4F, 5F, 6F, 7F).forEach {
            // Act
            val result = sut.yValueToSpeed(sut.speedToYValue(it))
            // Assert
            assertThat(it.toDouble(), fCloseTo(result, .0005F))
        }
    }

    @Test
    fun slow() {
        // Act
        val result = sut.speedToYValue(MPS_FOR_5_KMP)
        // Assert
        assertThat(result.toDouble(), fCloseTo(12F, 0.1F))
    }

    @Test
    fun medium() {
        // Act
        val result = sut.speedToYValue(MPS_FOR_10_KMP)
        // Assert
        assertThat(result.toDouble(), fCloseTo(6F, 0.1F))
    }

    @Test
    fun fast() {
        // Act
        val result = sut.speedToYValue(MPS_FOR_20_KMP)
        // Assert
        assertThat(result.toDouble(), fCloseTo(3F, 0.1F))
    }

    @Test
    fun speedHalfTheSpeed() {
        val reference = sut.speedToYValue(MPS_FOR_10_KMP) // e.g. 6 minute per kilometer
        // Act
        val higherY = sut.speedToYValue(MPS_FOR_5_KMP) // e.g. 12 minute per kilometer
        // Assert
        assertThat(reference * 2, `is`(higherY))
    }

    @Test
    fun speedDoubleTheSpeed() {
        val reference = sut.speedToYValue(MPS_FOR_10_KMP) // e.g. 6 minute per kilometer
        // Act
        val lowerY = sut.speedToYValue(MPS_FOR_20_KMP) // e.g. 3 minute per kilometer
        // Assert
        assertThat(reference / 2, `is`(lowerY))
    }

    @Test
    fun infiniteSpeed() {
        val nearZero = sut.speedToYValue(Float.MAX_VALUE)
        // Assert
        assertThat(nearZero.toDouble(), closeTo(.0, 0.00000005))
    }

    @Test
    fun noSpeed() {
        val nearZero = sut.speedToYValue(0F)
        // Assert
        assertThat(nearZero.toDouble(), closeTo(.0, 0.00000005))
    }

    private fun fCloseTo(result: Float, epsilon: Float) = closeTo(result.toDouble(), epsilon.toDouble())

    companion object {
        const val MPS_FOR_20_KMP = 20F / 3.6F
        const val MPS_FOR_10_KMP = 10F / 3.6F
        const val MPS_FOR_5_KMP = 5F / 3.6F
    }
}
