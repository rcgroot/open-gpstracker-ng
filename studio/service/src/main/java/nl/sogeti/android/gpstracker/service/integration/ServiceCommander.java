package nl.sogeti.android.gpstracker.service.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import nl.sogeti.android.gpstracker.service.BuildConfig;
import nl.sogeti.android.gpstracker.service.R;
import nl.sogeti.android.gpstracker.service.util.TrackUriExtensionKt;

import static nl.sogeti.android.gpstracker.service.integration.ServiceConstants.Commands.CONFIG_FOREGROUND;
import static nl.sogeti.android.gpstracker.service.integration.ServiceConstants.LOGGING_CUSTOM;
import static nl.sogeti.android.gpstracker.service.integration.ServiceConstants.LOGGING_FINE;

public class ServiceCommander implements ServiceCommanderInterface {

    private final Context context;

    @Inject
    public ServiceCommander(Context context) {
        this.context = context;
    }

    boolean isPackageInstalled() {
        Intent intent = createServiceIntent();
        ResolveInfo info = context.getPackageManager().resolveService(intent, 0);

        return info != null;
    }

    @NonNull
    Intent createServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(BuildConfig.packageName, "nl.sogeti.android.gpstracker.service.logger.GPSLoggerService"));
        return intent;
    }

    public boolean hasForInitialName(@NonNull Uri trackUri) {
        String name = TrackUriExtensionKt.readName(trackUri);

        return context.getString(R.string.initial_track_name).equals(name);
    }

    public void startGPSLogging() {
        startGPSLogging(context.getString(R.string.initial_track_name));
    }

    public void startGPSLogging(String trackName) {
        startGPSLogging(trackName, LOGGING_FINE);
    }

    public void startGPSLogging(String trackName, int precision) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_START);
        intent.putExtra(ServiceConstants.EXTRA_TRACK_NAME, trackName);
        intent.putExtra(ServiceConstants.Commands.CONFIG_PRECISION, precision);
        startService(intent, true);
    }

    public void pauseGPSLogging() {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_PAUSE);
        startService(intent, false);
    }

    public void resumeGPSLogging() {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_RESUME);
        startService(intent, true);
    }

    public void resumeGPSLogging(int precision, int customInterval, float customDistance) {
        setCustomLoggingPrecision(customInterval, customDistance);
        setLoggingPrecision(precision);
        resumeGPSLogging();
    }

    public void stopGPSLogging() {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_STOP);
        startService(intent, false);
    }

    public void setLoggingPrecision(int mode) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_PRECISION, mode);
        startService(intent, false);
    }

    public void setCustomLoggingPrecision(long seconds, float meters) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_TIME, seconds);
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_DISTANCE, meters);
        intent.putExtra(ServiceConstants.Commands.CONFIG_PRECISION, LOGGING_CUSTOM);
        startService(intent, false);
    }

    private void startService(Intent intent, boolean foreground) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && foreground) {
            intent.putExtra(CONFIG_FOREGROUND, true);
            context.startForegroundService(intent);
        } else {
            intent.putExtra(CONFIG_FOREGROUND, false);
            context.startService(intent);
        }
    }
}
