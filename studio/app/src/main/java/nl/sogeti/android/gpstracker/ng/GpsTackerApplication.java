package nl.sogeti.android.gpstracker.ng;

import nl.sogeti.android.gpstracker.v2.BuildConfig;
import timber.log.Timber;

/**
 * Start app generic services
 */
public class GpsTackerApplication extends android.app.Application {


    boolean debug = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();

        if (debug) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}