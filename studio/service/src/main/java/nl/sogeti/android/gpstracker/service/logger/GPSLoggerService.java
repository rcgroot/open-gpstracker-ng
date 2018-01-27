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
import android.os.RemoteException;

import nl.sogeti.android.gpstracker.integration.IGPSLoggerServiceRemote;
import nl.sogeti.android.gpstracker.service.BuildConfig;
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

    private GPSListener mGPSListener;
    private LoggerNotification mLoggerNotification;
    private IBinder mBinder = new GPSLoggerServiceImplementation();

    public GPSLoggerService() {
        super("GPS Logger", 10);
    }

    static {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
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
        Timber.d("handleCommand(Intent " + intent.getAction() + ")");
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
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_SPEED_SANITY)) {
            persistence.isSpeedChecked(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_SPEED_SANITY, true));
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_STATUS_MONITOR)) {
            persistence.isStatusMonitor(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_STATUS_MONITOR, false));
            mGPSListener.onPreferenceChange();
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_STREAM_BROADCAST)) {
            persistence.getStreamBroadcast(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_STREAM_BROADCAST, false));
            persistence.setBroadcastIntervalMeters(intent.getFloatExtra(ServiceConstants.Commands.CONFIG_STREAM_INTERVAL_DISTANCE, 1L));
            persistence.setBroadcastIntervalMinutes(intent.getLongExtra(ServiceConstants.Commands.CONFIG_STREAM_INTERVAL_TIME, 1L));
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_START_AT_BOOT)) {
            persistence.shouldLogAtBoot(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_START_AT_BOOT, false));
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_START_AT_POWER_CONNECT)) {
            persistence.shouldLogAtPowerConnected(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_START_AT_POWER_CONNECT, false));
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_STOP_AT_POWER_DISCONNECT)) {
            persistence.shouldLogAtPowerDisconnected(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_STOP_AT_POWER_DISCONNECT, false));
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_START_AT_DOCK)) {
            persistence.shouldLogAtDockCar(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_START_AT_DOCK, false));
        }
        if (intent.hasExtra(ServiceConstants.Commands.CONFIG_STOP_AT_UNDOCK)) {
            persistence.shouldLogAtUndockCar(intent.getBooleanExtra(ServiceConstants.Commands.CONFIG_STOP_AT_UNDOCK, false));
        }
        if (intent.hasExtra(ServiceConstants.Commands.COMMAND)) {
            executeCommandIntent(intent);
        }
    }

    private void executeCommandIntent(Intent intent) {
        switch (intent.getIntExtra(ServiceConstants.Commands.COMMAND, -1)) {
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
            setLingerDuration(10L);
        }
    }

    private void initLogging() {
        mLoggerNotification = new LoggerNotification(this);
        mLoggerNotification.stopLogging();
        LoggerPersistence persistence = new LoggerPersistence(this);
        mGPSListener = new GPSListener(this, new ServiceCommander(), persistence, mLoggerNotification, new PowerManager(this));
        mGPSListener.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        initLogging();
        return this.mBinder;
    }

    private class GPSLoggerServiceImplementation extends IGPSLoggerServiceRemote.Stub {
        @Override
        public long getTrackId() throws RemoteException {
            return mGPSListener.getTrackId();
        }

        @Override
        public int loggingState() throws RemoteException {
            return mGPSListener.getLoggingState();
        }

        @Override
        public Uri storeMediaUri(Uri mediaUri) throws RemoteException {
            mGPSListener.storeMediaUri(mediaUri);
            return null;
        }

        @Override
        public boolean isMediaPrepared() throws RemoteException {
            return mGPSListener.isMediaPrepared();
        }

        @Override
        public Uri storeMetaData(String key, String value) throws RemoteException {
            return mGPSListener.storeMetaData(key, value);
        }

        @Override
        public Location getLastWaypoint() throws RemoteException {
            return mGPSListener.getLastWaypoint();
        }

        @Override
        public float getTrackedDistance() throws RemoteException {
            return mGPSListener.getTrackedDistance();
        }
    }
}
