package nl.sogeti.android.gpstracker.ng.control

import android.databinding.BindingAdapter
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.common.bindings.CommonBindingAdapters
import nl.sogeti.android.gpstracker.v2.R

import nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING
import nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_PAUSED
import nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_STOPPED
import nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_UNKNOWN

class ControlBindingAdapters : CommonBindingAdapters() {

    @BindingAdapter("state")
    fun setState(container: ViewGroup, state: Int) {
        val left = container.getChildAt(0) as FloatingActionButton
        val right = container.getChildAt(1) as FloatingActionButton
        if (state == STATE_UNKNOWN) {
            hideLeftButton(left, right)
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            right.isEnabled = false
        } else if (state == STATE_STOPPED) {
            hideLeftButton(left, right)
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            right.isEnabled = true
        } else if (state == STATE_LOGGING) {
            showLeftButton(left)
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            right.setImageResource(R.drawable.ic_pause_black_24dp)
            right.isEnabled = true
        } else if (state == STATE_PAUSED) {
            showLeftButton(left)
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            right.isEnabled = true
        }
    }


    private fun showLeftButton(left: FloatingActionButton) {
        left.visibility = View.VISIBLE
        left.animate().translationX(0f)
    }

    private fun hideLeftButton(left: FloatingActionButton, right: FloatingActionButton) {
        right.bringToFront()
        if (left.visibility != View.GONE) {
            val distance = right.x - left.x
            left.animate().translationX(distance).withEndAction { left.visibility = View.GONE }.start()
        } else {
            left.visibility = View.GONE
        }
    }
}