package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders.condense
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class GraphDataHelpersTest {

    @Test
    fun `condens empty list`() {
        val list = emptyList<Int>()

        val result = list.condense({ x, y -> x == y }, { it.sum() })

        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun `condens unique list`() {
        val list = listOf(1, 2, 3, 4, 5, 6)

        val result = list.condense({ x, y -> x == y }, { it.sum() })

        assertThat(result, `is`(list))
    }

    @Test
    fun `condens basic list`() {
        val list = listOf(1, 1, 2, 2, 3, 4, 5, 5, 5, 6)

        val result = list.condense({ x, y -> x == y }, { it.sum() })

        assertThat(result, `is`(listOf(2, 4, 3, 4, 15, 6)))
    }

    @Test
    fun `condens repeating list`() {
        val list = listOf(1, 1, 2, 2, 3, 4, 5, 2, 2, 2, 5, 5, 6)

        val result = list.condense({ x, y -> x == y }, { it.sum() })

        assertThat(result, `is`(listOf(2, 4, 3, 4, 5, 6, 10, 6)))
    }
}
