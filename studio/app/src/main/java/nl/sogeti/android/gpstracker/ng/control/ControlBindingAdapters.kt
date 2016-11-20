package nl.sogeti.android.gpstracker.ng.control

import android.databinding.BindingAdapter
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.ng.common.bindings.CommonBindingAdapters
import nl.sogeti.android.gpstracker.v2.R
import timber.log.Timber

class ControlBindingAdapters : CommonBindingAdapters() {

    @BindingAdapter("state")
    fun setState(container: ViewGroup, state: Int) {
        val left = container.getChildAt(0) as FloatingActionButton
        val right = container.getChildAt(1) as FloatingActionButton
        if (state == STATE_STOPPED) {
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            showOnlyRightButton(left, right)
        } else if (state == STATE_LOGGING) {
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            right.setImageResource(R.drawable.ic_pause_black_24dp)
            showAllButtons(left, right)
        } else if (state == STATE_PAUSED) {
            right.setImageResource(R.drawable.ic_navigation_black_24dp)
            left.setImageResource(R.drawable.ic_stop_black_24dp)
            showAllButtons(left, right)
        } else {
            // state == STATE_UNKNOWN and illegal states
            showNoButtons(left, right)
        }
    }

    private fun showNoButtons(left: FloatingActionButton, right: FloatingActionButton) {
        cancelAnimations(left, right)
        if (left.isVisible) {
            val leftAnimation = moveLeftUnderRight(left, right)
            moveRightOutView(left, right).setDuration(leftAnimation.duration)
        }
        else {
            moveRightOutView(left, right)
        }
    }

    private fun showAllButtons(left: FloatingActionButton, right: FloatingActionButton) {
        cancelAnimations(left, right)
        if (right.isVisible && left.isInvisible) {
            moveLeftToOriginalLocation(left, right)
        }
        else if (right.isInvisible && left.isInvisible) {
            val rightAnimation = moveRightInView(right)
            moveLeftToOriginalLocation(left, right).setStartDelay(rightAnimation.duration)
        }
        else {
            Timber.e("Unanticipated state left:${left.isVisible} right:${right.isVisible}")
        }
    }

    private fun showOnlyRightButton(left: FloatingActionButton, right: FloatingActionButton) {
        cancelAnimations(left, right)
        if (left.isVisible && right.isVisible) {
            moveLeftUnderRight(left, right)
        }
        else if(left.isInvisible && right.isInvisible) {
            moveRightInView(right)
        }
        else {
            Timber.e("Unanticipated state left:${left.isVisible} right:${right.isVisible}")
        }
    }

    private fun cancelAnimations(left: FloatingActionButton, right: FloatingActionButton) {
        left.animation?.cancel()
        right.animation?.cancel()
    }


    /**
     * Pre-condition: Invisible Right
     * Post-condition: Visible Right
     */
    private fun moveRightOutView(left: FloatingActionButton, right: FloatingActionButton): ViewPropertyAnimator {
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
    private fun moveLeftToOriginalLocation(left: FloatingActionButton, right: FloatingActionButton): ViewPropertyAnimator {
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
}

private val View.isVisible: Boolean
    get() {
        return this.visibility == View.VISIBLE
    }

private val View.isInvisible: Boolean
    get() {
        return this.visibility != View.VISIBLE
    }
