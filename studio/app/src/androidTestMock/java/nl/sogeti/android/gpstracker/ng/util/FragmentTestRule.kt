package nl.sogeti.android.gpstracker.ng.util

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.Fragment

class FragmentTestRule<out T : Fragment>(fragmentClass: Class<T>, touch: Boolean, launch: Boolean) : ActivityTestRule<TestActivity>(TestActivity::class.java, touch, launch) {

    constructor(fragmentClass: Class<T>) : this(fragmentClass, false, true)

    val fragment: T = fragmentClass.newInstance()

    override fun afterActivityLaunched() {
        super.afterActivityLaunched()
        activity.runOnUiThread({
            activity.supportFragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow()
        })
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
    }

    fun stopActivity() {
        activity.finish()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
    }
}