package nl.sogeti.android.gpstracker.service.logger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.IOException;

import nl.sogeti.android.gpstracker.service.R;
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.service.util.TrackUriExtensionKt;
import timber.log.Timber;

/**
 * Manages the different notification task needed when running the logger service
 */
public class LoggerNotification {
    private static final int ID_DISABLED = R.string.service_connectiondisabled;
    private static final int ID_STATUS = R.string.service_gpsstatus;
    private static final int ID_GPS_PROBLEM = R.string.service_gpsproblem;
    private static final int SMALL_ICON = R.drawable.ic_maps_indicator_current_position;
    private final Service service;

    int numberOfSatellites = 0;

    private NotificationManagerCompat notificationManager;
    private boolean isShowingDisabled = false;

    public LoggerNotification(Service service) {
        this.service = service;
        notificationManager = NotificationManagerCompat.from(service);
    }

    public void startLogging(int mPrecision, int mLoggingState, boolean mStatusMonitor, long mTrackId) {
        notificationManager.cancel(ID_STATUS);

        Notification notification = buildLogging(mPrecision, mLoggingState, mStatusMonitor, mTrackId);
        service.startForeground(ID_STATUS, notification);
    }

    public void updateLogging(int mPrecision, int mLoggingState, boolean mStatusMonitor, long mTrackId) {
        Notification notification = buildLogging(mPrecision, mLoggingState, mStatusMonitor, mTrackId);
        notificationManager.notify(ID_STATUS, notification);
    }

    public void stopLogging() {
        notificationManager.cancel(ID_STATUS);
        service.stopForeground(true);
    }

    private Notification buildLogging(int precision, int state, boolean monitor, long trackId) {
        Resources resources = service.getResources();
        CharSequence contentTitle = resources.getString(R.string.service_title);
        String precisionText = resources.getStringArray(R.array.precision_choices)[precision];
        String stateText = resources.getStringArray(R.array.state_choices)[state - 1];
        CharSequence contentText;
        switch (precision) {
            case (ServiceConstants.LOGGING_GLOBAL):
                contentText = resources.getString(R.string.service_networkstatus, stateText, precisionText);
                break;
            default:
                if (monitor) {
                    contentText = resources.getString(R.string.service_gpsstatus, stateText, precisionText,
                            numberOfSatellites);
                } else {
                    contentText = resources.getString(R.string.service_gpsnostatus, stateText, precisionText);
                }
                break;
        }
        Uri uri = TrackUriExtensionKt.trackUri(trackId);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent contentIntent = PendingIntent.getActivity(service, 0, notificationIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(service)
                        .setSmallIcon(SMALL_ICON)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(contentIntent)
                        .setOngoing(true);
        PendingIntent pendingIntent;
        if (state == ServiceConstants.STATE_LOGGING) {
            CharSequence pause = resources.getString(R.string.logcontrol_pause);
            Intent intent = new Intent(service, GPSLoggerService.class);
            intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_PAUSE);
            pendingIntent = PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_pause_24dp, pause, pendingIntent);
        } else if (state == ServiceConstants.STATE_PAUSED) {
            CharSequence resume = resources.getString(R.string.logcontrol_resume);
            Intent intent = new Intent(service, GPSLoggerService.class);
            intent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_RESUME);
            pendingIntent = PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_play_arrow_24dp, resume, pendingIntent);
        }

        return builder.build();
    }

    void startPoorSignal(long trackId) {
        Resources resources = service.getResources();
        CharSequence contentText = resources.getString(R.string.service_gpsproblem);
        CharSequence contentTitle = resources.getString(R.string.service_title);

        Uri uri = TrackUriExtensionKt.trackUri(trackId);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent contentIntent = PendingIntent.getActivity(service, 0, notificationIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(service)
                        .setSmallIcon(SMALL_ICON)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        notificationManager.notify(ID_GPS_PROBLEM, builder.build());
    }

    public void stopPoorSignal() {
        notificationManager.cancel(ID_GPS_PROBLEM);
    }

    void startDisabledProvider(int resId, long trackId) {
        isShowingDisabled = true;

        CharSequence contentTitle = service.getResources().getString(R.string.service_title);
        CharSequence contentText = service.getResources().getString(resId);
        CharSequence tickerText = service.getResources().getString(resId);

        Uri uri = TrackUriExtensionKt.trackUri(trackId);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent contentIntent = PendingIntent.getActivity(service, 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(service)
                        .setAutoCancel(true)
                        .setTicker(tickerText)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(contentIntent);

        notificationManager.notify(
                ID_DISABLED,
                mBuilder.build());
    }

    void stopDisabledProvider(int resId) {
        notificationManager.cancel(ID_DISABLED);
        isShowingDisabled = false;

        CharSequence text = service.getString(resId);
        Toast toast = Toast.makeText(service, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public boolean isShowingDisabled() {
        return isShowingDisabled;
    }

    void soundGpsSignalAlarm() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        MediaPlayer mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(service, alert);
            final AudioManager audioManager = (AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mMediaPlayer.setLooping(false);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Problem setting data source for mediaplayer");
        } catch (SecurityException e) {
            Timber.e(e, "Problem setting data source for mediaplayer");
        } catch (IllegalStateException e) {
            Timber.e(e, "Problem with mediaplayer");
        } catch (IOException e) {
            Timber.e(e, "Problem with mediaplayer");
        }
    }
}
