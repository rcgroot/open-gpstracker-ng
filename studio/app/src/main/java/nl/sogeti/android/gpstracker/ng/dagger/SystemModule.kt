package nl.sogeti.android.gpstracker.ng.dagger

import android.net.Uri
import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.packagemanager.PackageManagerFactory
import nl.sogeti.android.gpstracker.ng.map.LocationFactory
import nl.sogeti.android.gpstracker.ng.map.LocationFactoryImpl
import nl.sogeti.android.gpstracker.ng.utils.PermissionChecker
import nl.sogeti.android.gpstracker.ng.utils.VersionHelper
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Named

@Module
class SystemModule {

    @Provides
    fun locale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    fun gpsStatusControllerFactory(): GpsStatusControllerFactory {
        return GpsStatusControllerFactory()
    }

    @Provides
    fun uriBuilder() = Uri.Builder()

    @Provides
    @Named("SystemBackgroundExecutor")
    fun executor(): Executor = AsyncTask.THREAD_POOL_EXECUTOR

    @Provides
    fun permissionChecker() = PermissionChecker()

    @Provides
    fun locationFactory(): LocationFactory = LocationFactoryImpl()

    @Provides
    fun packageManagerFactory() = PackageManagerFactory()

    @Provides
    fun versionHelper() = VersionHelper()
}
