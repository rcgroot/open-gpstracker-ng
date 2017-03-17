package nl.sogeti.android.gpstracker.ng.common.controllers.content

import android.content.Context
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class ContentControllerProviderTest {

    lateinit var sut: ContentControllerProvider
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var listener: ContentController.Listener

    @Before
    fun setUp() {
        sut = ContentControllerProvider()
    }

    @Test
    fun createContentControllerProvider() {
        // Act
        val controller = sut.createContentControllerProvider(context, listener)
        // Assert
        assertThat(controller, Matchers.`is`(Matchers.instanceOf(ContentController::class.java)))
    }

}