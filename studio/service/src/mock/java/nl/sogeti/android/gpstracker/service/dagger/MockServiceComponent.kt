package nl.sogeti.android.gpstracker.service.dagger

import dagger.Component

@Component(modules = [(MockServiceModule::class)])
interface MockServiceComponent : ServiceComponent {
}
