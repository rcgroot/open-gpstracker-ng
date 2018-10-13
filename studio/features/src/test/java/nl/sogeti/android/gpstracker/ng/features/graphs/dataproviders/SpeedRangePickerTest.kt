package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import android.content.res.Resources
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedConverter
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
import nl.sogeti.android.opengpstrack.ng.features.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class SpeedRangePickerTest {

    private val resources: Resources = mock {
        on { getString(R.string.mps_to_speed) } doReturn "3.6"
    }
    private val context: Context = mock {
        on { resources } doReturn resources
    }
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()

    @Test
    fun `lower bound convert 9 kph to 0 kph`() {
        val sut = SpeedRangePicker(false)
        val speed = 9F / 3.6F

        val pretty = sut.prettyMinYValue(context, speed)

        assertThat(pretty, `is`(0F))
    }

    @Test
    fun `lower bound convert 18 kph to 10 kph`() {
        val sut = SpeedRangePicker(false)
        val speed = 18F / 3.6F

        val pretty = sut.prettyMinYValue(context, speed)

        assertThat(pretty, `is`(2.777778F))
    }

    @Test
    fun `upper bound convert 9 kph to 10 kph`() {
        val sut = SpeedRangePicker(false)
        val speed = 9F / 3.6F

        val pretty = sut.prettyMaxYValue(context, speed)

        assertThat(pretty, `is`(2.777778F))
    }

    @Test
    fun `upper bound convert 18 kph to 20 kph`() {
        val sut = SpeedRangePicker(false)
        val speed = 18F / 3.6F

        val pretty = sut.prettyMaxYValue(context, speed)

        assertThat(pretty, `is`(5.555556F))
    }

    @Test
    fun `lower bound convert 3_15 min-km to 3_00 min-km`() {
        val sut = SpeedRangePicker(true)
        sut.graphSpeedConverter = GraphSpeedConverter()
        val speed = 3.25F

        val pretty = sut.prettyMinYValue(context, speed)
        assertThat(pretty, `is`(3F))
    }

    @Test
    fun `upper bound convert 3_15 min-km to 4_00 min-km`() {
        val sut = SpeedRangePicker(true)
        sut.graphSpeedConverter = GraphSpeedConverter()
        val speed = 3.25F

        val pretty = sut.prettyMaxYValue(context, speed)
        assertThat(pretty, `is`(4F))
    }

}
