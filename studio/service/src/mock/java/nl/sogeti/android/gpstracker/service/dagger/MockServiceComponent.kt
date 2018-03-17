package nl.sogeti.android.gpstracker.service.dagger

import dagger.Component
import nl.sogeti.android.gpstracker.ng.base.dagger.AppComponent

@Component(modules = [(MockServiceModule::class)],
        dependencies = [AppComponent::class])
interface MockServiceComponent : ServiceComponent
