package nl.sogeti.android.gpstracker.ng.about

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class AboutFragmentTest {

    lateinit var sut: AboutFragment

    @Before
    fun setUp() {
        sut = AboutFragment()
    }

    @Test
    fun getModel() {
        // Act
        val model = sut.model
        // Assert
        assertThat(model.buildNumber, `is`(greaterThan(0)))
        assertThat(model.gitHash, `is`(notNullValue()))
        assertThat(model.url, `is`("file:///android_asset/about.html"))
        assertThat(model.version, `is`("2.0.0-SNAPSHOT"))
    }

}