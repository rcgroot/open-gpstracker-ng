package nl.sogeti.android.gpstracker.service.dagger

import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.mock.MockPermissionChecker
import nl.sogeti.android.gpstracker.service.integration.ContentConstants
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.integration.ServiceManager
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.service.mock.MockServiceManager
import nl.sogeti.android.gpstracker.utils.PermissionChecker
import javax.inject.Named

@Module
class MockServiceModule {

    @Provides
    fun serviceManagerInterface(): ServiceManagerInterface = MockServiceManager()

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
