package nl.sogeti.android.gpstracker.service.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import nl.sogeti.android.gpstracker.service.BuildConfig;
import nl.sogeti.android.gpstracker.service.R;
import nl.sogeti.android.gpstracker.service.util.TrackUriExtensionKt;

import static nl.sogeti.android.gpstracker.service.logger.LoggingConstants.FINE_DISTANCE;
import static nl.sogeti.android.gpstracker.service.logger.LoggingConstants.FINE_INTERVAL;

public class ServiceCommander implements ServiceCommanderInterface {

    private final Context context;

    @Inject
    public ServiceCommander(Context context) {
        this.context = context;
    }

    public boolean isPackageInstalled() {
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

    public void startGPSLogging() {
        startGPSLogging(context.getString(R.string.initial_track_name));
    }


    public boolean hasForInitialName(Uri trackUri) {
        String name = TrackUriExtensionKt.readName(trackUri);

        return context.getString(R.string.initial_track_name).equals(name);
    }

    public void startGPSLogging(String trackName) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_START);
        intent.putExtra(ServiceConstants.EXTRA_TRACK_NAME, trackName);
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_TIME, FINE_INTERVAL);
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_DISTANCE, FINE_DISTANCE);
        intent.putExtra(ServiceConstants.Commands.CONFIG_SPEED_SANITY, true);
        context.startService(intent);
    }

    public void startGPSLogging(int precision, int customInterval, float customDistance, String trackName) {
        setCustomLoggingPrecision(customInterval, customDistance);
        setLoggingPrecision(precision);
        startGPSLogging(trackName);
    }

    public void pauseGPSLogging() {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_PAUSE);
        context.startService(intent);
    }

    public void resumeGPSLogging() {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_RESUME);
        context.startService(intent);
    }

    public void resumeGPSLogging(int precision, int customInterval, float customDistance) {
        setCustomLoggingPrecision(customInterval, customDistance);
        setLoggingPrecision(precision);
        resumeGPSLogging();
    }

    public void stopGPSLogging() {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_STOP);
        context.startService(intent);
    }

    public void setLoggingPrecision(int mode) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_PRECISION, mode);
        context.startService(intent);
    }

    public void setCustomLoggingPrecision(long seconds, float meters) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_TIME, seconds);
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_DISTANCE, meters);
        context.startService(intent);
    }

    public void setSanityFilter(boolean filter) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_SPEED_SANITY, filter);
        context.startService(intent);
    }

    public void setStatusMonitor(boolean monitor) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_STATUS_MONITOR, monitor);
        context.startService(intent);
    }

    public void setAutomaticLogging(boolean atBoot, boolean atDocking, boolean atUnDocking, boolean atPowerConnect, boolean atPowerDisconnect) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_START_AT_BOOT, atBoot);
        intent.putExtra(ServiceConstants.Commands.CONFIG_START_AT_DOCK, atDocking);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STOP_AT_UNDOCK, atUnDocking);
        intent.putExtra(ServiceConstants.Commands.CONFIG_START_AT_POWER_CONNECT, atPowerConnect);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STOP_AT_POWER_DISCONNECT, atPowerDisconnect);
        context.startService(intent);
    }

    public void setStreaming(boolean isStreaming, float distance, long time) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_STREAM_BROADCAST, isStreaming);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STREAM_INTERVAL_DISTANCE, distance);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STREAM_INTERVAL_TIME, time);
        context.startService(intent);
    }
}
