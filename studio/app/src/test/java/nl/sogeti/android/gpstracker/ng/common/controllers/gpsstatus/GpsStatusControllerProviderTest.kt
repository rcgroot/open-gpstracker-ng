package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.content.Context
import android.os.Build
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class GpsStatusControllerProviderTest {

    lateinit var sut: GpsStatusControllerProvider
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var listener: GpsStatusController.Listener

    @Before
    fun setUp() {
        sut = GpsStatusControllerProvider()
    }

    @Test
    fun createNougatController() {
        // Act
        val controller = sut.createGpsStatusListenerProvider(context, listener, Build.VERSION_CODES.N)
        // Assert
        assertThat(controller, `is`(instanceOf(GnnsStatusControllerImpl::class.java)))
    }

    @Test
    fun createPreNougatController() {
        // Act
        val controller = sut.createGpsStatusListenerProvider(context, listener, Build.VERSION_CODES.M)
        // Assert
        assertThat(controller, `is`(instanceOf(GpsStatusControllerImpl::class.java)))
    }

    @Test
    fun createDefaultController() {
        // Act
        val controller = sut.createGpsStatusListenerProvider(context, listener)
        // Assert
        assertThat(controller, `is`(instanceOf(GpsStatusControllerImpl::class.java)))
    }
}