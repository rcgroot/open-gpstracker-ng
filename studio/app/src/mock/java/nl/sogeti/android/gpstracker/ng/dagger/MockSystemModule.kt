package nl.sogeti.android.gpstracker.ng.dagger

import android.net.Uri
import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.map.LocationFactory
import nl.sogeti.android.gpstracker.ng.util.MockGpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.util.MockLocationFactory
import nl.sogeti.android.gpstracker.ng.util.MockPermissionChecker
import nl.sogeti.android.gpstracker.ng.utils.PermissionChecker
import java.util.*

@Module
class MockSystemModule {

    @Provides
    fun locale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    fun gpsStatusControllerFactory(): GpsStatusControllerFactory {
        return MockGpsStatusControllerFactory()
    }

    @Provides
    fun uriBuilder() = Uri.Builder()

    @Provides
    fun executor() = AsyncTask.THREAD_POOL_EXECUTOR

    @Provides
    fun permissionChecker(): PermissionChecker = MockPermissionChecker()

    @Provides
    fun locationFactory(): LocationFactory = MockLocationFactory()
}