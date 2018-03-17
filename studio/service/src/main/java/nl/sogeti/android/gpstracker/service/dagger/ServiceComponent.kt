package nl.sogeti.android.gpstracker.service.dagger

import dagger.Component
import nl.sogeti.android.gpstracker.ng.base.dagger.AppComponent
import nl.sogeti.android.gpstracker.service.integration.ServiceCommanderInterface
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.service.util.PermissionRequester
import javax.inject.Named

@Component(modules = [(ServiceModule::class)],
        dependencies = [AppComponent::class])
interface ServiceComponent {

    @Named("providerAuthority")
    fun providerAuthority(): String

    @Named("stateBroadcastAction")
    fun stateBroadcastAction(): String

    fun serviceManagerInterface(): ServiceManagerInterface

    fun serviceCommanderInterface(): ServiceCommanderInterface

    fun inject(injectable: PermissionRequester)

}
