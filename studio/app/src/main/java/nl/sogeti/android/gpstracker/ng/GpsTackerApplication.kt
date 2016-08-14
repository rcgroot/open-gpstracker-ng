package nl.sogeti.android.gpstracker.ng


import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil

import nl.sogeti.android.gpstracker.ng.binders.MyBindingComponent
import nl.sogeti.android.gpstracker.v2.BuildConfig
import timber.log.Timber


/**
 * Start app generic services
 */
class GpsTackerApplication : android.app.Application() {


    internal var debug = BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()

        val bindingComponent = MyBindingComponent()
        DataBindingUtil.setDefaultComponent(bindingComponent)

        if (debug) {
            Timber.plant(Timber.DebugTree())
        }
    }


}