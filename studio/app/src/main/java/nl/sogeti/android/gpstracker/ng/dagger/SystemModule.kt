package nl.sogeti.android.gpstracker.ng.dagger

import android.net.Uri
import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerProvider
import java.util.*

@Module
class SystemModule {

    @Provides
    fun locale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    fun gpsStatusControllerProvider(): GpsStatusControllerProvider {
        return GpsStatusControllerProvider()
    }

    @Provides
    fun uriBuilder() = Uri.Builder()

    @Provides
    fun executor() = AsyncTask.THREAD_POOL_EXECUTOR
}