package nl.sogeti.android.gpstracker.ng

import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration.appComponent
import nl.sogeti.android.gpstracker.ng.base.common.BaseGpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.base.dagger.AppModule
import nl.sogeti.android.gpstracker.ng.base.dagger.DaggerAppComponent
import nl.sogeti.android.gpstracker.ng.base.dagger.SystemModule
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration.featureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.DaggerFeatureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureModule
import nl.sogeti.android.gpstracker.ng.features.dagger.VersionInfoModule
import nl.sogeti.android.gpstracker.ng.features.wear.LoggingReceiver
import nl.sogeti.android.gpstracker.service.dagger.DaggerServiceComponent
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration.serviceComponent
import nl.sogeti.android.gpstracker.service.dagger.ServiceModule
import nl.sogeti.android.gpstracker.v2.BuildConfig.*

open class GpsTrackerApplication : BaseGpsTrackerApplication() {

    private var stateReceiver: LoggingReceiver? = null // Or replace with signature protection implicit broadcast

    override fun onCreate() {
        super.onCreate()

        setupModules()
        setupDataBinding()
        setupLoggingReceiver()
    }

    private fun setupDataBinding() {
        FeatureConfiguration.setupDefaultViewBinding()
    }

    override fun onTerminate() {
        destroyLoggingReceiver()
        super.onTerminate()
    }

    open fun setupModules() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .systemModule(SystemModule())
                .build()
        serviceComponent = DaggerServiceComponent.builder()
                .serviceModule(ServiceModule())
                .build()
        featureComponent = DaggerFeatureComponent.builder()
                .appComponent(BaseConfiguration.appComponent)
                .serviceComponent(serviceComponent)
                .versionInfoModule(VersionInfoModule(version(), gitHash(), buildNumber()))
                .featureModule(FeatureModule(this))
                .build()
    }

    internal fun version() = VERSION_NAME

    internal fun buildNumber() = BUILD_NUMBER.toString()

    internal fun gitHash() = GIT_COMMIT.take(Math.min(7, GIT_COMMIT.length))

    private fun setupLoggingReceiver() {
        stateReceiver = LoggingReceiver()
        stateReceiver?.register(this)
    }

    private fun destroyLoggingReceiver() {
        stateReceiver?.unregister(this)
        stateReceiver = null
    }


}
