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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class SystemModule {

    @Provides
    fun gpsStatusControllerFactory(application: Context): GpsStatusControllerFactory {
        return GpsStatusControllerFactory(application)
    }

    @Provides
    fun uriBuilder() = Uri.Builder()

    @Provides
    @Singleton
    @Computation
    fun computationExecutor(): Executor = AsyncTask.THREAD_POOL_EXECUTOR

    @Provides
    @Singleton
    @DiskIO
    fun diskExecutor(): Executor = ThreadPoolExecutor(1, 2, 10L, TimeUnit.SECONDS, LinkedBlockingQueue())

    @Provides
    @Singleton
    @NetworkIO
    fun networkExecutor(): Executor = ThreadPoolExecutor(1, 16, 30L, TimeUnit.SECONDS, LinkedBlockingQueue())

    @Provides
    fun packageManager(application: Context): PackageManager = application.packageManager

    @Provides
    fun locationFactory(application: Context): LocationFactory = GpsLocationFactory(application)

    @Provides
    fun contentResolver(application: Context): ContentResolver = application.contentResolver
}
