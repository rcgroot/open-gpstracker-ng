package nl.sogeti.android.gpstracker.ng.features

import android.databinding.DataBindingUtil
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureComponent
import nl.sogeti.android.gpstracker.ng.features.databinding.FeaturesBindingComponent

object FeatureConfiguration {

    lateinit var featureComponent: FeatureComponent

    fun setupDefaultViewBinding() {
        val bindingComponent = FeaturesBindingComponent()
        DataBindingUtil.setDefaultComponent(bindingComponent)
    }
}
