package nl.sogeti.android.gpstracker.ng

import nl.sogeti.android.gpstracker.ng.base.common.BaseGpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.wear.LoggingReceiver
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration
import nl.sogeti.android.gpstracker.v2.BuildConfig.*

class GpsTrackerApplication : BaseGpsTrackerApplication() {

    private var stateReceiver: LoggingReceiver? = null // Or replace with signature protection implicit broadcast

    override fun onCreate() {
        super.onCreate()

        initModules()
        initLoggingReceiver()
    }

    override fun onTerminate() {
        destroyLoggingReceiver()
        super.onTerminate()
    }

    private fun initModules() {
        val version = VERSION_NAME
        val buildNumber = BUILD_NUMBER.toString()
        val gitHash = GIT_COMMIT.take(Math.min(7, GIT_COMMIT.length))

        ServiceConfiguration.initServiceComponent()
        FeatureConfiguration.initFeatureComponent(this, version, gitHash, buildNumber)
        FeatureConfiguration.setupDefaultViewBinding()
    }

    private fun initLoggingReceiver() {
        stateReceiver = LoggingReceiver()
        stateReceiver?.register(this)
    }

    private fun destroyLoggingReceiver() {
        stateReceiver?.unregister(this)
        stateReceiver = null
    }


}
