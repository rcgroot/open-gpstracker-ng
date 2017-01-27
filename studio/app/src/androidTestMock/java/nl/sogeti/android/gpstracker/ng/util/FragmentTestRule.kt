package nl.sogeti.android.gpstracker.ng.util

import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment

class FragmentTestRule<out T : Fragment>(fragmentClass: Class<T>, touch: Boolean, launch: Boolean) : ActivityTestRule<TestActivity>(TestActivity::class.java, touch, launch) {

    constructor(fragmentClass: Class<T>) : this(fragmentClass, false, true)

    val fragment: T = fragmentClass.newInstance()
    var arguments: Bundle? = null

    override fun afterActivityLaunched() {
        super.afterActivityLaunched()

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        fragment.arguments = arguments
        activity.runOnUiThread({
            if (fragment is DialogFragment) {
                fragment.show(activity.supportFragmentManager, "DialogTAG")
            } else {
                activity.supportFragmentManager
                        .beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commitNowAllowingStateLoss()
            }
        })
        instrumentation.waitForIdleSync()
    }

    fun launchFragment(arguments: Bundle?) {
        this.arguments = arguments
        launchActivity(null)
    }

    fun finishFragment() {
        arguments = null
        activity.finish()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
    }
}