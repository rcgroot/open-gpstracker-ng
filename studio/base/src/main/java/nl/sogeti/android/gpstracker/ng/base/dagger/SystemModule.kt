package nl.sogeti.android.gpstracker.ng.base.dagger

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactoryImpl
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.packagemanager.PackageManagerFactory
import java.util.concurrent.Executor
import javax.inject.Named

@Module
class SystemModule {

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
    fun packageManagerFactory() = PackageManagerFactory()

    @Provides
    fun locationFactory(): LocationFactory = LocationFactoryImpl()

    @Provides
    fun contentResolver(application: Context): ContentResolver = application.contentResolver
}
