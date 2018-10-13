package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito

class SpeedRangePickerTest {

    val contex: Context = Mockito.mock(Context::class.java)

    @Test
    fun testPrettyMinValueSame() {
        val sut = SpeedRangePicker(false)

        val pretty = sut.prettyMinYValue(contex, 36F)

        assertThat(pretty, `is`(10F))
    }

}
