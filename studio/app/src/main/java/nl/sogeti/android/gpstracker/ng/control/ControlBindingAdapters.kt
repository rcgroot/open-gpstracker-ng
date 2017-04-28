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
        if (left.visible) {
            val leftAnimation = moveLeftUnderRight(left, right)
            moveRightOutView(right).setDuration(leftAnimation.duration)
        }
        else {
            moveRightOutView(right)
        }
    }

    private fun showAllButtons(left: FloatingActionButton, right: FloatingActionButton) {
        if (right.visible && left.invisible) {
            moveLeftToOriginalLocation(left)
        }
        else if (right.invisible && left.invisible) {
            val rightAnimation = moveRightInView(right)
            moveLeftToOriginalLocation(left).setStartDelay(rightAnimation.duration)
        }
    }

    private fun showOnlyRightButton(left: FloatingActionButton, right: FloatingActionButton) {
        if (left.visible && right.visible) {
            moveLeftUnderRight(left, right)
        }
        else if(left.invisible) {
            moveRightInView(right)
        }
    }

    /**
     * Pre-condition: Invisible Right
     * Post-condition: Visible Right
     */
    private fun moveRightOutView(right: FloatingActionButton): ViewPropertyAnimator {
        right.pivotX = right.width / 2.0F
        right.pivotY = right.height / 2.0F
        val animation = right.animate().scaleX(0.000001F).scaleY(0.000001F).withEndAction { right.visibility = View.GONE }

        return animation
    }

    /**
     * Pre-condition: Invisible Right
     * Post-condition: Visible Right
     */
    private fun moveRightInView(right: FloatingActionButton): ViewPropertyAnimator {
        right.pivotX = right.width / 2.0F
        right.pivotY = right.height / 2.0F
        right.scaleX = 0.000001F
        right.scaleY = 0.000001F
        right.visibility = View.VISIBLE
        val animation = right.animate().scaleX(1.0F).scaleY(1.0F)

        return animation
    }

    /**
     * Pre-condition: Visible right over left
     * Post-condition: Visible left
     */
    private fun moveLeftToOriginalLocation(left: FloatingActionButton): ViewPropertyAnimator {
        left.visibility = View.VISIBLE
        val animation = left.animate().translationX(0f)

        return animation
    }

    /**
     * Pre-condition: Visible right
     * Post-condition: Gone left
     */
    private fun moveLeftUnderRight(left: FloatingActionButton, right: FloatingActionButton): ViewPropertyAnimator {
        right.bringToFront()
        val distance = right.x - left.x
        val animation = left.animate().translationX(distance).withEndAction { left.visibility = View.GONE }

        return animation
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
