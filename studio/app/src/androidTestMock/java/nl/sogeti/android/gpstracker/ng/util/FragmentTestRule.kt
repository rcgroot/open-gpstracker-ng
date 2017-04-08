package nl.sogeti.android.gpstracker.ng.util

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import timber.log.Timber
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class FragmentTestRule<out T : Fragment>(fragmentClass: Class<T>, touch: Boolean, launch: Boolean) : ActivityTestRule<TestActivity>(TestActivity::class.java, touch, launch) {

    constructor(fragmentClass: Class<T>) : this(fragmentClass, false, true)

    val fragment: T = fragmentClass.newInstance()
    var arguments: Bundle? = null

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        // On Espresso 2.2.2 (at least) avoid the USE_CHOREOGRAPHER when using data binding
        // http://stackoverflow.com/questions/40703567/how-do-i-make-espresso-wait-until-data-binding-has-updated-the-view-with-the-dat/42807742#42807742
        // https://code.google.com/p/android/issues/detail?id=220247
        setFinalStatic(ViewDataBinding::class.java, "USE_CHOREOGRAPHER", false)
    }

    fun setFinalStatic(clazz: Class<*>, fieldName: String, newValue: Any) {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        var modifiersField: Field? = null
        try {
            modifiersField = Field::class.java.getDeclaredField("accessFlags")
        } catch (exception: NoSuchFieldException) {
            try {
                modifiersField = Field::class.java.getDeclaredField("modifiers")
            } catch (exception: NoSuchFieldException) {
                Timber.e("Failed to change access flags / modifiers from ${clazz.canonicalName}")
            }
        }
        modifiersField?.isAccessible = true
        modifiersField?.setInt(field, field.modifiers and Modifier.FINAL.inv())
        field.set(null, newValue)
    }

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