package nl.sogeti.android.gpstracker.v2.wear.common

import android.app.Application
import android.os.StrictMode
import nl.sogeti.android.gpstracker.v2.wear.BuildConfig
import timber.log.Timber

class WearApplication : Application() {

    var debug = BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()
        setupDebugTree()
    }

    private fun setupDebugTree() {
        if (debug) {
            Timber.plant(Timber.DebugTree())
            if (StrictMode.ThreadPolicy.Builder().build() != null) {
                StrictMode.setThreadPolicy(
                        StrictMode.ThreadPolicy.Builder()
                                .detectAll()
                                .penaltyLog()
                                .build())
            }
        }
    }
}
