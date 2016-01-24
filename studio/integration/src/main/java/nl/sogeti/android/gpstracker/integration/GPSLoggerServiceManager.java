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
package nl.sogeti.android.gpstracker.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import nl.sogeti.android.log.Log;

/**
 * Class to interact with the service that tracks and logs the locations
 *
 * @author rene (c) Jan 18, 2009, Sogeti B.V.
 * @version $Id$
 */
public class GPSLoggerServiceManager {

    private static final String REMOTE_EXCEPTION = "REMOTE_EXCEPTION";
    public final Object mStartLock = new Object();
    private IGPSLoggerServiceRemote mGPSLoggerRemote;
    private boolean mBound;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mServiceConnection;
    private Runnable mOnServiceConnected;

    public GPSLoggerServiceManager() {
        mBound = false;
    }

    public static void startGPSLogging(Context context, String trackName) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.COMMAND, ExternalConstants.Commands.EXTRA_COMMAND_START);
        intent.putExtra(ExternalConstants.EXTRA_TRACK_NAME, trackName);
        context.startService(intent);
    }

    @NonNull
    private static Intent createServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("nl.sogeti.android.gpstracker", "nl.sogeti.android.gpstracker.service.logger.GPSLoggerService"));
        return intent;
    }

    public static void startGPSLogging(Context context, int precision, int customInterval, float customDistance, String trackName) {
        setCustomLoggingPrecision(context, customInterval, customDistance);
        setLoggingPrecision(context, precision);
        startGPSLogging(context, trackName);
    }

    public static void pauseGPSLogging(Context context) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.COMMAND, ExternalConstants.Commands.EXTRA_COMMAND_PAUSE);
        context.startService(intent);
    }

    public static void resumeGPSLogging(Context context) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.COMMAND, ExternalConstants.Commands.EXTRA_COMMAND_RESUME);
        context.startService(intent);
    }

    public static void resumeGPSLogging(Context context, int precision, int customInterval, float customDistance) {
        setCustomLoggingPrecision(context, customInterval, customDistance);
        setLoggingPrecision(context, precision);
        resumeGPSLogging(context);
    }

    public static void stopGPSLogging(Context context) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.COMMAND, ExternalConstants.Commands.EXTRA_COMMAND_STOP);
        context.startService(intent);
    }

    public static void setLoggingPrecision(Context context, int mode) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.CONFIG_PRECISION, mode);
        context.startService(intent);
    }

    public static void setCustomLoggingPrecision(Context context, long seconds, float meters) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.CONFIG_INTERVAL_TIME, seconds);
        intent.putExtra(ExternalConstants.Commands.CONFIG_INTERVAL_DISTANCE, meters);
        context.startService(intent);
    }

    public static void setSanityFilter(Context context, boolean filter) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.CONFIG_SPEED_SANITY, filter);
        context.startService(intent);
    }

    public static void setStatusMonitor(Context context, boolean monitor) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.CONFIG_STATUS_MONITOR, monitor);
        context.startService(intent);
    }

    public static void setAutomaticLogging(Context context, boolean atBoot, boolean atDocking, boolean atUnDocking, boolean atPowerConnect, boolean atPowerDisconnect) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.CONFIG_START_AT_BOOT, atBoot);
        intent.putExtra(ExternalConstants.Commands.CONFIG_START_AT_DOCK, atDocking);
        intent.putExtra(ExternalConstants.Commands.CONFIG_STOP_AT_UNDOCK, atUnDocking);
        intent.putExtra(ExternalConstants.Commands.CONFIG_START_AT_POWER_CONNECT, atPowerConnect);
        intent.putExtra(ExternalConstants.Commands.CONFIG_STOP_AT_POWER_DISCONNECT, atPowerDisconnect);
        context.startService(intent);
    }

    public static void setStreaming(Context context, boolean isStreaming, float distance, long time) {
        Intent intent = createServiceIntent();
        intent.putExtra(ExternalConstants.Commands.CONFIG_STREAM_BROADCAST, isStreaming);
        intent.putExtra(ExternalConstants.Commands.CONFIG_STREAM_INTERVAL_DISTANCE, distance);
        intent.putExtra(ExternalConstants.Commands.CONFIG_STREAM_INTERVAL_TIME, time);
        context.startService(intent);
    }

    public Location getLastWaypoint() {
        synchronized (mStartLock) {
            Location lastWaypoint = null;
            try {
                if (mBound) {
                    lastWaypoint = this.mGPSLoggerRemote.getLastWaypoint();
                } else {
                    Log.w(this, "Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Log.e(this, "Could get lastWaypoint GPSLoggerService.", e);
            }
            return lastWaypoint;
        }
    }

    public float getTrackedDistance() {
        synchronized (mStartLock) {
            float distance = 0F;
            try {
                if (mBound) {
                    distance = this.mGPSLoggerRemote.getTrackedDistance();
                } else {
                    Log.w(this, "Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Log.e(this, "Could get tracked distance from GPSLoggerService.", e);
            }
            return distance;
        }
    }

    public long getTrackId() {
        synchronized (mStartLock) {
            long trackId = -1;
            try {
                if (mBound) {
                    trackId = this.mGPSLoggerRemote.getTrackId();
                } else {
                    Log.w(this, "Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Log.e(this, "Could stat GPSLoggerService.", e);
            }
            return trackId;
        }
    }

    public int getLoggingState() {
        synchronized (mStartLock) {
            int logging = ExternalConstants.STATE_UNKNOWN;
            try {
                if (mBound) {
                    logging = this.mGPSLoggerRemote.loggingState();
                    //               Log.d( TAG, "mGPSLoggerRemote tells state to be "+logging );
                } else {
                    Log.w(this, "Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Log.e(this, "Could stat GPSLoggerService.", e);
            }
            return logging;
        }
    }

    public boolean isMediaPrepared() {
        synchronized (mStartLock) {
            boolean prepared = false;
            try {
                if (mBound) {
                    prepared = this.mGPSLoggerRemote.isMediaPrepared();
                } else {
                    Log.w(this, "Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Log.e(this, "Could stat GPSLoggerService.", e);
            }
            return prepared;
        }
    }

    public void storeMetaData(String key, String value) {
        synchronized (mStartLock) {
            if (mBound) {
                try {
                    this.mGPSLoggerRemote.storeMetaData(key, value);
                } catch (RemoteException e) {
                    Log.e(GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not send datasource to GPSLoggerService.", e);
                }
            } else {
                Log.e(this, "No GPSLoggerRemote service connected to this manager");
            }
        }
    }

    public void storeMediaUri(Uri mediaUri) {
        synchronized (mStartLock) {
            if (mBound) {
                try {
                    this.mGPSLoggerRemote.storeMediaUri(mediaUri);
                } catch (RemoteException e) {
                    Log.e(GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not send media to GPSLoggerService.", e);
                }
            } else {
                Log.e(this, "No GPSLoggerRemote service connected to this manager");
            }
        }
    }

    /**
     * Means by which an Activity lifecycle aware object hints about binding and unbinding
     *
     * @param onServiceConnected Run on main thread after the service is bound
     */
    public void startup(Context context, final Runnable onServiceConnected) {
        synchronized (mStartLock) {
            if (!mBound) {
                mOnServiceConnected = onServiceConnected;
                mServiceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName className, IBinder service) {
                        synchronized (mStartLock) {
                            GPSLoggerServiceManager.this.mGPSLoggerRemote = IGPSLoggerServiceRemote.Stub.asInterface(service);
                            mBound = true;
                        }
                        if (mOnServiceConnected != null) {
                            mOnServiceConnected.run();
                            mOnServiceConnected = null;
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName className) {
                        synchronized (mStartLock) {
                            mBound = false;
                        }
                    }
                };
                if (ContextCompat.checkSelfPermission(context, ExternalConstants.permission.TRACKING_CONTROL) == PackageManager.PERMISSION_GRANTED) {
                    context.bindService(createServiceIntent(), this.mServiceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    Log.e(this, "Did not bind service because required permission is lacking");
                }
            } else {
                Log.w(this, "Attempting to connect whilst already connected");
            }
        }
    }

    /**
     * Means by which an Activity lifecycle aware object hints about binding and unbinding
     */
    public void shutdown(Context context) {
        synchronized (mStartLock) {
            try {
                if (mBound) {
                    context.unbindService(this.mServiceConnection);
                    GPSLoggerServiceManager.this.mGPSLoggerRemote = null;
                    mServiceConnection = null;
                    mBound = false;
                }
            } catch (IllegalArgumentException e) {
                Log.w(this, "Failed to unbind a service, prehaps the service disapearded?", e);
            }
        }
    }
}