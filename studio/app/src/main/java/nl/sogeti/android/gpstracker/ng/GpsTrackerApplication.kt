package nl.sogeti.android.gpstracker.ng

import nl.sogeti.android.gpstracker.ng.base.common.BaseGpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.dagger.DaggerFeatureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureModule
import nl.sogeti.android.gpstracker.ng.features.dagger.VersionInfoModule
import nl.sogeti.android.gpstracker.ng.features.wear.LoggingReceiver
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration.serviceComponent
import nl.sogeti.android.gpstracker.v2.BuildConfig.*

class GpsTrackerApplication : BaseGpsTrackerApplication() {

    private var stateReceiver: LoggingReceiver? = null // Or replace with signature protection implicit broadcast

    override fun onCreate() {
        super.onCreate()

        FeatureConfiguration.setupDefaultViewBinding()

        stateReceiver = LoggingReceiver()
        stateReceiver?.register(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        stateReceiver?.unregister(this)
    }

    private val version = VERSION_NAME
    private val buildNumber = BUILD_NUMBER.toString()
    private val gitHash = GIT_COMMIT.take(Math.min(7, GIT_COMMIT.length))

    fun initDagger() {
        val featureComponent = DaggerFeatureComponent.builder()
                .appComponent(appComponent)
                .serviceComponent(serviceComponent)
                .featureModule(FeatureModule(applicationContext))
                .versionInfoModule(VersionInfoModule(version, gitHash, buildNumber))
                .build()
        FeatureConfiguration.initBinding(featureComponent)
    }
}
