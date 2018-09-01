package nl.sogeti.android.gpstracker.ng.features.control

import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.features.databinding.CommonBindingAdapters
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.*
import nl.sogeti.android.opengpstrack.ng.features.R

class ControlBindingAdapters : CommonBindingAdapters() {

    @BindingAdapter("state")
    fun setState(container: ViewGroup, state: Int) {
        val left = container.getChildAt(0) as FloatingActionButton
        val right = container.getChildAt(1) as FloatingActionButton
        cancelAnimations(left, right)
        if (state == STATE_STOPPED) {
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            right.contentDescription = container.context.getString(R.string.control_record)
            showOnlyRightButton(left, right)
        } else if (state == STATE_LOGGING) {
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            left.contentDescription = container.context.getString(R.string.control_stop)
            right.setImageResource(R.drawable.ic_pause_black_24dp)
            right.contentDescription = container.context.getString(R.string.control_pause)
            showAllButtons(left, right)
        } else if (state == STATE_PAUSED) {
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            right.contentDescription = container.context.getString(R.string.control_resume)
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            left.contentDescription = container.context.getString(R.string.control_stop)
            showAllButtons(left, right)
        } else {
            // state == STATE_UNKNOWN and illegal states
            showNoButtons(left, right)
        }
    }

    private fun showNoButtons(left: FloatingActionButton, right: FloatingActionButton) {
        left.animate()
                .alpha(0F)
        right.animate()
                .alpha(0F)
    }

    private fun showAllButtons(left: FloatingActionButton, right: FloatingActionButton) {
        left.animate()
                .alpha(1F)
        right.animate()
                .alpha(1F)
    }

    private fun showOnlyRightButton(left: FloatingActionButton, right: FloatingActionButton) {
        left.animate()
                .alpha(0F)
        right.animate()
                .alpha(1F)
    }

    private fun cancelAnimations(left: FloatingActionButton, right: FloatingActionButton) {
        left.animate().cancel()
        right.animate().cancel()
    }
}

private val View.visible: Boolean
    get() {
        return this.visibility == View.VISIBLE
    }

private val View.invisible: Boolean
    get() {
        return this.visibility != View.VISIBLE
    }
