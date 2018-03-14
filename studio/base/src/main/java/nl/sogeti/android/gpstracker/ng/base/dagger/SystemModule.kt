package nl.sogeti.android.gpstracker.ng.base.dagger

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.base.location.GpsLocationFactory
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
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
    fun packageManager(application: Context): PackageManager = application.packageManager

    @Provides
    fun locationFactory(application: Context): LocationFactory = GpsLocationFactory(application)

    @Provides
    fun contentResolver(application: Context): ContentResolver = application.contentResolver
}
