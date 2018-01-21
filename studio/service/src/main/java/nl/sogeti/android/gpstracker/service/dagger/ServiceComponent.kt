package nl.sogeti.android.gpstracker.service.dagger

import android.net.Uri
import dagger.Component
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface
import javax.inject.Named

@Component(modules = [(ServiceModule::class)])
interface ServiceComponent {

    @Named("providerAuthority")
    fun providerAuthority(): String

    @Named("stateBroadcastAction")
    fun stateBroadcastAction(): String

    fun provideUriBuilder(): Uri.Builder

    fun serviceManagerInterface(): ServiceManagerInterface

}
