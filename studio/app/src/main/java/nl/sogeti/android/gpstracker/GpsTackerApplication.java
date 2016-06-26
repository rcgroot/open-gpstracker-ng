package nl.sogeti.android.gpstracker;

import nl.sogeti.android.gpstracker.v2.BuildConfig;
import timber.log.Timber;

/**
 * Start app generic services
 */
public class GpsTackerApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}