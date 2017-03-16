package nl.sogeti.android.gpstracker.ng.dagger

import android.net.Uri
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.common.controllers.GpsStatusControllerProvider
import nl.sogeti.android.gpstracker.ng.util.MockGpsStatusControllerProvider
import java.util.*

@Module
class MockSystemModule {

    @Provides
    fun locale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    fun gpsStatusControllerProvider(): GpsStatusControllerProvider {
        return MockGpsStatusControllerProvider()
    }

    @Provides
    fun uriBuilder() = Uri.Builder()

    @Provides
    fun executor() = AsyncTask.THREAD_POOL_EXECUTOR
}