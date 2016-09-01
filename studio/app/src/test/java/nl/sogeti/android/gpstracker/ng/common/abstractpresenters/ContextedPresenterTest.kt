package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContextedPresenterTest {

    @Mock
    var mockContext: Context? = null

    @Test
    fun start() {
        // Prepare
        val sut = MyContextedPresenter()

        // Execute
        sut.start(mockContext!!)

        // Verify
        assertTrue(sut.didStart)
        assertEquals(sut.context, mockContext)
    }

    @Test
    fun stop() {
        // Prepare
        val sut = MyContextedPresenter()
        sut.start(mockContext!!)

        // Execute
        sut.stop()

        // Verify
        assertTrue(sut.willStop)
        assertNull(sut.context)
    }
}

class MyContextedPresenter : ContextedPresenter() {
    var willStop = false
    var didStart = false

    override fun willStop() {
        willStop = true
    }

    override fun didStart() {
        didStart = true
    }
}
