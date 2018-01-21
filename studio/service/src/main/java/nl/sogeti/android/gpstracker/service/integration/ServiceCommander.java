package nl.sogeti.android.gpstracker.service.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

public class ServiceCommander {

    public boolean isPackageInstalled(Context context) {
        Intent intent = createServiceIntent();
        ResolveInfo info = context.getPackageManager().resolveService(intent, 0);

        return info != null;
    }

    public void startGPSLogging(Context context, String trackName) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_START);
        intent.putExtra(ServiceConstants.EXTRA_TRACK_NAME, trackName);
        context.startService(intent);
    }

    @NonNull
    Intent createServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("nl.sogeti.android.gpstracker", "nl.sogeti.android.gpstracker.service.logger.GPSLoggerService"));
        return intent;
    }

    public void startGPSLogging(Context context, int precision, int customInterval, float customDistance, String trackName) {
        setCustomLoggingPrecision(context, customInterval, customDistance);
        setLoggingPrecision(context, precision);
        startGPSLogging(context, trackName);
    }

    public void pauseGPSLogging(Context context) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_PAUSE);
        context.startService(intent);
    }

    public void resumeGPSLogging(Context context) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_RESUME);
        context.startService(intent);
    }

    public void resumeGPSLogging(Context context, int precision, int customInterval, float customDistance) {
        setCustomLoggingPrecision(context, customInterval, customDistance);
        setLoggingPrecision(context, precision);
        resumeGPSLogging(context);
    }

    public void stopGPSLogging(Context context) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_STOP);
        context.startService(intent);
    }

    public void setLoggingPrecision(Context context, int mode) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_PRECISION, mode);
        context.startService(intent);
    }

    public void setCustomLoggingPrecision(Context context, long seconds, float meters) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_TIME, seconds);
        intent.putExtra(ServiceConstants.Commands.CONFIG_INTERVAL_DISTANCE, meters);
        context.startService(intent);
    }

    public void setSanityFilter(Context context, boolean filter) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_SPEED_SANITY, filter);
        context.startService(intent);
    }

    public void setStatusMonitor(Context context, boolean monitor) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_STATUS_MONITOR, monitor);
        context.startService(intent);
    }

    public void setAutomaticLogging(Context context, boolean atBoot, boolean atDocking, boolean atUnDocking, boolean atPowerConnect, boolean atPowerDisconnect) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_START_AT_BOOT, atBoot);
        intent.putExtra(ServiceConstants.Commands.CONFIG_START_AT_DOCK, atDocking);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STOP_AT_UNDOCK, atUnDocking);
        intent.putExtra(ServiceConstants.Commands.CONFIG_START_AT_POWER_CONNECT, atPowerConnect);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STOP_AT_POWER_DISCONNECT, atPowerDisconnect);
        context.startService(intent);
    }

    public void setStreaming(Context context, boolean isStreaming, float distance, long time) {
        Intent intent = createServiceIntent();
        intent.putExtra(ServiceConstants.Commands.CONFIG_STREAM_BROADCAST, isStreaming);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STREAM_INTERVAL_DISTANCE, distance);
        intent.putExtra(ServiceConstants.Commands.CONFIG_STREAM_INTERVAL_TIME, time);
        context.startService(intent);
    }
}
