/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) Apr 24, 2011 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.service.logger;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;

import nl.sogeti.android.gpstracker.integration.IGPSLoggerServiceRemote;
import nl.sogeti.android.gpstracker.service.integration.ServiceCommander;
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.service.linger.LingerService;
import nl.sogeti.android.gpstracker.service.util.TrackUriExtensionKt;
import timber.log.Timber;

/**
 * A system service as controlling the background logging of gps locations.
 *
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public class GPSLoggerService extends LingerService {

    public static final int LINGER_DURATION = 10;
    private GPSListener mGPSListener;
    private LoggerNotification mLoggerNotification;
    private IBinder mBinder = new GPSLoggerServiceImplementation();

    public GPSLoggerService() {
        super("GPS Logger", LINGER_DURATION);
    }

    @Override
    protected void didCreate() {
        Timber.d("didCreate()");
        initLogging();
    }

    @Override
    protected void didContinue() {
        Timber.d("didCreate()");
        initLogging();
    }

    @Override
    protected void didDestroy() {
        Timber.d("didDestroy()");
        if (mGPSListener.isLogging()) {
            Timber.w("Destroying an actively logging service");
        }
        mGPSListener.removeGpsStatusListener();
        mGPSListener.stopListening();
        if (mGPSListener.getLoggingState() != ServiceConstants.STATE_PAUSED) {
            mLoggerNotification.stopLogging();
        }
        mGPSListener.onDestroy();
        mLoggerNotification = null;
    }

    @Override
    protected boolean shouldContinue() {
        boolean isLogging = mGPSListener.isLogging();
        Timber.d("shouldContinue() " + isLogging);

        if (isLogging) {
            mGPSListener.verifyLoggingState();
        }
        return isLogging;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("onHandleIntent(Intent " + intent.getExtras().keySet() + ")");
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_FOREGROUND)) {
            boolean foreground = intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_FOREGROUND, false);
            mLoggerNotification.showForeground(this, foreground);
        }
        LoggerPersistence persistence = new LoggerPersistence(this);
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_PRECISION)) {
            int precision = intent.getIntExtra(ServiceConstants.Commands.CONFIG_PRECISION, ServiceConstants.LOGGING_NORMAL);
            persistence.setPrecision(precision);
            mGPSListener.onPreferenceChange();
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_INTERVAL_DISTANCE)) {
            float interval = intent.getFloatExtra(ServiceConstants.Commands.CONFIG_INTERVAL_DISTANCE, LoggingConstants.NORMAL_DISTANCE);
            persistence.setCustomLocationIntervalMetres(interval);
            mGPSListener.onPreferenceChange();
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_INTERVAL_TIME)) {
            long interval = intent.getLongExtra(ServiceConstants.Commands.CONFIG_INTERVAL_TIME, 1L);
            persistence.setCustomLocationIntervalSeconds(interval);
            mGPSListener.onPreferenceChange();
        }
        if (intent.hasExtra(ServiceConstants.Commands.COMMAND)) {
            persistence.isStatusMonitor(false);
            persistence.isSpeedChecked(true);
            mGPSListener.onPreferenceChange();
            executeCommandIntent(intent);
        }
    }

    private void executeCommandIntent(@NonNull Intent intent) {
        int command = intent.getIntExtra(ServiceConstants.Commands.COMMAND, -1);
        Timber.d("executeCommandIntent(Intent " + command + ")");
        switch (command) {
            case ServiceConstants.Commands.EXTRA_COMMAND_RESTORE:
                mGPSListener.crashRestoreState();
                break;
            case ServiceConstants.Commands.EXTRA_COMMAND_START:
                String trackName = null;
                if (intent.hasExtra(ServiceConstants.EXTRA_TRACK_NAME)) {
                    trackName = intent.getExtras().getString(ServiceConstants.EXTRA_TRACK_NAME);
                }
                mGPSListener.startLogging(trackName);
                if (trackName == null) {
                    // Start a naming of the track
                    Uri uri = TrackUriExtensionKt.trackUri(mGPSListener.getTrackId());
                    Intent namingIntent = new Intent(ServiceConstants.NAMING_ACTION, uri);
                    namingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    namingIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivity(namingIntent);
                }
                break;
            case ServiceConstants.Commands.EXTRA_COMMAND_PAUSE:
                mGPSListener.pauseLogging();
                break;
            case ServiceConstants.Commands.EXTRA_COMMAND_RESUME:
                mGPSListener.resumeLogging();
                break;
            case ServiceConstants.Commands.EXTRA_COMMAND_STOP:
                mGPSListener.stopLogging();
                break;
            default:
                break;
        }

        if (mGPSListener.isLogging()) {
            setLingerDuration(mGPSListener.getCheckPeriod() / 1000L);
        } else {
            setLingerDuration(LINGER_DURATION);
        }
    }

    private void initLogging() {
        mLoggerNotification = new LoggerNotification(this);
        mLoggerNotification.stopLogging();
        LoggerPersistence persistence = new LoggerPersistence(this);
        mGPSListener = new GPSListener(this, new ServiceCommander(this), persistence, mLoggerNotification, new PowerManager(this));
        mGPSListener.onCreate();
        Intent restoreIntent = new Intent();
        restoreIntent.putExtra(ServiceConstants.Commands.COMMAND, ServiceConstants.Commands.EXTRA_COMMAND_RESTORE);
        sendMessage(restoreIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        initLogging();
        return this.mBinder;
    }

    private class GPSLoggerServiceImplementation extends IGPSLoggerServiceRemote.Stub {
        @Override
        public long getTrackId() {
            return mGPSListener.getTrackId();
        }

        @Override
        public int loggingState() {
            return mGPSListener.getLoggingState();
        }

        @Override
        public Uri storeMediaUri(Uri mediaUri) {
            mGPSListener.storeMediaUri(mediaUri);
            return null;
        }

        @Override
        public boolean isMediaPrepared() {
            return mGPSListener.isMediaPrepared();
        }

        @Override
        public Uri storeMetaData(String key, String value) {
            return mGPSListener.storeMetaData(key, value);
        }

        @Override
        public Location getLastWaypoint() {
            return mGPSListener.getLastWaypoint();
        }

        @Override
        public float getTrackedDistance() {
            return mGPSListener.getTrackedDistance();
        }
    }
}
