package nl.sogeti.android.gpstracker.ng.control

import android.databinding.BindingAdapter
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.ng.common.bindings.CommonBindingAdapters
import nl.sogeti.android.gpstracker.v2.R

class ControlBindingAdapters : CommonBindingAdapters() {

    @BindingAdapter("state")
    fun setState(container: ViewGroup, state: Int) {
        val left = container.getChildAt(0) as FloatingActionButton
        val right = container.getChildAt(1) as FloatingActionButton
        if (state == STATE_STOPPED) {
            hideLeftButton(left, right)
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
        } else if (state == STATE_LOGGING) {
            showAllButtons(left, right)
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            right.setImageResource(R.drawable.ic_pause_black_24dp)
        } else if (state == STATE_PAUSED) {
            showAllButtons(left, right)
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
        } else {
            // state == STATE_UNKNOWN and illegal states
            hideAllButtons(left, right)
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
        }
    }

    private fun showAllButtons(left: FloatingActionButton, right: FloatingActionButton) {
        val animation = showRightButton(right)
        val animate = left.visibility != View.VISIBLE
        if (animate) {
            val ms = animation?.duration ?: 0L
            left.visibility = View.VISIBLE
            left.animate().translationX(0f).setStartDelay(ms)
        }
    }

    private fun hideLeftButton(left: FloatingActionButton, right: FloatingActionButton): ViewPropertyAnimator? {
        var animation: ViewPropertyAnimator? = null
        right.bringToFront()
        if (left.visibility != View.GONE) {
            val distance = right.x - left.x
            animation = left.animate().translationX(distance).withEndAction { left.visibility = View.GONE }
        } else {
            left.visibility = View.GONE
        }

        return animation
    }

    private fun showRightButton(right: FloatingActionButton): ViewPropertyAnimator? {
        var animation: ViewPropertyAnimator? = null
        val animate = right.visibility != View.VISIBLE
        if (animate) {
            right.pivotX = right.width / 2.0F
            right.pivotY = right.height / 2.0F
            right.scaleX = 0.000001F
            right.scaleY = 0.000001F
            right.visibility = View.VISIBLE
            animation = right.animate().scaleX(1.0F).scaleY(1.0F)
        }

        return animation
    }

    private fun hideAllButtons(left: FloatingActionButton, right: FloatingActionButton) {
        left.visibility = View.GONE
        right.visibility = View.GONE
        val animation = hideLeftButton(left, right)
        val animate = right.visibility == View.VISIBLE
        if (animate) {
            val ms = animation?.duration ?: 0L
            right.pivotX = right.width / 2.0F
            right.pivotY = right.height / 2.0F
            right.animate().scaleX(0.000001F).scaleY(0.000001F).withEndAction { right.visibility = View.GONE }.setStartDelay(ms)
        }
    }
}