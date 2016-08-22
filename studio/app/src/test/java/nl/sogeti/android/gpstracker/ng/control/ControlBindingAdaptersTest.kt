package nl.sogeti.android.gpstracker.ng.control

import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import com.android.annotations.NonNull
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.stubbing.Answer

@RunWith(MockitoJUnitRunner::class)
class ControlBindingAdaptersTest {

    private var sut = ControlBindingAdapters()
    @Mock
    var mockContainer: ViewGroup? = null
    @Mock
    var mockLeftButton: FloatingActionButton? = null
    @Mock
    var mockRightButton: FloatingActionButton? = null

    @Before
    fun setup() {
        sut = ControlBindingAdapters()
        setupButtonViewGroup()
    }

    @Test
    fun setStateUnknown() {
        // Execute
        sut.setState(mockContainer!!, STATE_UNKNOWN)

        // Verify
        verify<FloatingActionButton>(mockLeftButton).visibility = View.GONE
        verify<FloatingActionButton>(mockRightButton).isEnabled = false
    }

    @Test
    fun setStateStopped() {
        // Execute
        sut.setState(mockContainer!!, STATE_STOPPED)

        // Verify
        verify<FloatingActionButton>(mockLeftButton).visibility = View.GONE
        verify<FloatingActionButton>(mockRightButton).isEnabled = true
    }

    @Test
    fun setStatePause() {
        // Execute
        sut.setState(mockContainer!!, STATE_PAUSED)

        // Verify
        verify<FloatingActionButton>(mockLeftButton).visibility = View.VISIBLE
        verify<FloatingActionButton>(mockRightButton).isEnabled = true
    }

    @Test
    fun setStateLogging() {
        // Execute
        sut.setState(mockContainer!!, STATE_LOGGING)

        // Verify
        verify<FloatingActionButton>(mockLeftButton).visibility = View.VISIBLE
        verify<FloatingActionButton>(mockRightButton).isEnabled = true
    }
    /* Helpers */

    @NonNull
    private fun setupButtonViewGroup() {
        val mockAnimator = mock<ViewPropertyAnimator>(ViewPropertyAnimator::class.java)
        `when`<ViewPropertyAnimator>(mockAnimator.translationX(anyFloat())).thenReturn(mockAnimator)
        `when`(mockLeftButton?.animate()).thenReturn(mockAnimator)

        val runnable = arrayOfNulls<Runnable>(1)
        `when`<ViewPropertyAnimator>(mockAnimator.withEndAction(any<Runnable>(Runnable::class.java))).thenAnswer({
            runnable[0] = it.arguments[0] as Runnable
            mockAnimator
        })
        doAnswer({
            runnable[0]?.run()
        }).`when`(mockAnimator).start()

        `when`(mockContainer?.getChildAt(0)).thenReturn(mockLeftButton)
        `when`(mockContainer?.getChildAt(1)).thenReturn(mockRightButton)
    }
}