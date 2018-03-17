package nl.sogeti.android.gpstracker.service.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.mock.MockPermissionChecker
import nl.sogeti.android.gpstracker.service.integration.*
import nl.sogeti.android.gpstracker.service.mock.MockServiceManager
import nl.sogeti.android.gpstracker.utils.PermissionChecker
import javax.inject.Named

@Module
class MockServiceModule {

    @Provides
    fun serviceManagerInterface(context: Context): ServiceManagerInterface = MockServiceManager(context)


    @Provides
    fun serviceCommanderInterface(application: Context): ServiceCommanderInterface = MockServiceManager(application)

    @Provides
    @Named("providerAuthority")
    fun providerAuthority() =
            ContentConstants.GPS_TRACKS_AUTHORITY

    @Provides
    @Named("stateBroadcastAction")
    fun stateBroadcastAction() =
            ServiceConstants.ACTION_BROADCAST_LOGGING_STATE

    @Provides
    fun permissionChecker(): PermissionChecker = MockPermissionChecker()
}
