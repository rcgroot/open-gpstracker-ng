package nl.sogeti.android.gpstracker.v2.wear

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper

/**
 * A stripped replacement for HolderFragment found in ArchComponents
 */
class HolderFragment : Fragment() {

    var holding: Holdable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onDestroy() {
        holding?.onCleared()
        super.onDestroy()
    }

    companion object {

        const val TAG = "HolderFragment"

        fun <T : Holdable> of(activity: Activity, type: Class<T>): T {
            val holdable: T
            var holderFragment = activity.fragmentManager.findFragmentByTag(HolderFragment.TAG)
            if (holderFragment == null) {
                holderFragment = HolderFragment()
                holdable = type.getConstructor(Context::class.java).newInstance(activity.applicationContext)
                holderFragment.holding = holdable
                activity.fragmentManager.beginTransaction()
                        .add(holderFragment, HolderFragment.TAG)
                        .commit()
            } else {
                holdable = (holderFragment as HolderFragment).holding as T
            }

            return holdable
        }
    }

    interface Holdable {
        @CallSuper
        fun onCleared() {
        }
    }
}
