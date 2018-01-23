package nl.sogeti.android.gpstracker.ng.features

import android.content.Context
import android.databinding.DataBindingUtil
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.features.dagger.DaggerFeatureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureModule
import nl.sogeti.android.gpstracker.ng.features.dagger.VersionInfoModule
import nl.sogeti.android.gpstracker.ng.features.databinding.FeaturesBindingComponent
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration

object FeatureConfiguration {

    lateinit var featureComponent: FeatureComponent

    fun initFeatureComponent(context: Context, version: String, commit: String, build: String) {
        featureComponent = DaggerFeatureComponent.builder()
                .appComponent(BaseConfiguration.appComponent)
                .serviceComponent(ServiceConfiguration.serviceComponent)
                .versionInfoModule(VersionInfoModule(version, commit, build))
                .featureModule(FeatureModule(context))
                .build()
    }

    fun setupDefaultViewBinding() {
        val bindingComponent = FeaturesBindingComponent()
        DataBindingUtil.setDefaultComponent(bindingComponent)
    }
}
