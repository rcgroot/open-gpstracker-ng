package nl.sogeti.android.gpstracker.ng.features.util

import android.content.Context
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.Navigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class ContextedPresenterTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var mockContext: Context

    @Test
    fun start() {
        // Prepare
        val sut = MyContextedPresenter()

        // Execute
        sut.start(mockContext)

        // Verify
        assertTrue(sut.didStart)
        assertEquals(sut.context, mockContext)
    }

    @Test(expected = IllegalStateException::class)
    fun stop() {
        // Prepare
        val sut = MyContextedPresenter()
        sut.start(mockContext)

        // Execute
        sut.stop()

        // Verify
        assertTrue(sut.willStop)
        sut.context
    }

    class MyContextedPresenter : ContextedPresenter<Navigation>() {
        var willStop = false
        var didStart = false

        override fun willStop() {
            willStop = true
        }

        override fun didStart() {
            didStart = true
        }
    }

}
