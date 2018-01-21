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
package nl.sogeti.android.gpstracker.service.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import nl.sogeti.android.gpstracker.integration.IGPSLoggerServiceRemote;
import timber.log.Timber;

/**
 * Class to interact with the service that tracks and logs the locations
 */
public class ServiceManager implements ServiceManagerInterface {

    private final Object mStartLock = new Object();
    private IGPSLoggerServiceRemote mGPSLoggerRemote;
    private boolean mBound;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mServiceConnection;
    private Runnable mOnServiceConnected;
    private final ServiceCommander commander = new ServiceCommander();

    public ServiceManager() {
        mBound = false;
    }

    public Location getLastWaypoint() {
        synchronized (mStartLock) {
            Location lastWaypoint = null;
            try {
                if (mBound) {
                    lastWaypoint = this.mGPSLoggerRemote.getLastWaypoint();
                } else {
                    Timber.w("Remote interface to logging service not found. Started: %s", mBound);
                }
            } catch (RemoteException e) {
                Timber.e(e, "Could get lastWaypoint GPSLoggerService.");
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
                    Timber.w("Remote interface to logging service not found. Started: %s", mBound);
                }
            } catch (RemoteException e) {
                Timber.e(e, "Could get tracked distance from GPSLoggerService.");
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
                    Timber.w("Remote interface to logging service not found. Started: %s", mBound);
                }
            } catch (RemoteException e) {
                Timber.e(e, "Could stat GPSLoggerService.");
            }
            return trackId;
        }
    }

    @Override
    public void startGPSLogging(@NonNull Context context, @Nullable String trackName) {
        commander.startGPSLogging(context, trackName);
    }

    @Override
    public void stopGPSLogging(@NonNull Context context) {
        commander.stopGPSLogging(context);
    }

    @Override
    public void pauseGPSLogging(@NonNull Context context) {
        commander.pauseGPSLogging(context);
    }

    @Override
    public void resumeGPSLogging(@NonNull Context context) {
        commander.resumeGPSLogging(context);
    }

    @Override
    public boolean isPackageInstalled(Context context) {
        return commander.isPackageInstalled(context);
    }

    public int getLoggingState() {
        synchronized (mStartLock) {
            int logging = ServiceConstants.STATE_UNKNOWN;
            try {
                if (mBound) {
                    logging = this.mGPSLoggerRemote.loggingState();
                    //               Timber.d( TAG, "mGPSLoggerRemote tells state to be "+logging );
                } else {
                    Timber.w("Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Timber.e(e, "Could stat GPSLoggerService.");
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
                    Timber.w("Remote interface to logging service not found. Started: " + mBound);
                }
            } catch (RemoteException e) {
                Timber.e(e, "Could stat GPSLoggerService.");
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
                    Timber.e(e, "Could not send data source to GPSLoggerService.");
                }
            } else {
                Timber.e("No GPSLoggerRemote service connected to this manager");
            }
        }
    }

    public void storeMediaUri(Uri mediaUri) {
        synchronized (mStartLock) {
            if (mBound) {
                try {
                    this.mGPSLoggerRemote.storeMediaUri(mediaUri);
                } catch (RemoteException e) {
                    Timber.e(e, "Could not send media to GPSLoggerService.");
                }
            } else {
                Timber.e("No GPSLoggerRemote service connected to this manager");
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
                            ServiceManager.this.mGPSLoggerRemote = IGPSLoggerServiceRemote.Stub.asInterface(service);
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
                if (ContextCompat.checkSelfPermission(context, ServiceConstants.permission.TRACKING_CONTROL) == PackageManager.PERMISSION_GRANTED) {
                    context.bindService(commander.createServiceIntent(), this.mServiceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    try {
                        context.bindService(commander.createServiceIntent(), this.mServiceConnection, Context.BIND_AUTO_CREATE);
                    } catch (SecurityException e) {
                        Timber.e(e, "Did not bind service because required permission is lacking");
                    }
                }
            } else {
                Timber.w("Attempting to connect whilst already connected");
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
                    ServiceManager.this.mGPSLoggerRemote = null;
                    mServiceConnection = null;
                    mBound = false;
                }
            } catch (IllegalArgumentException e) {
                Timber.w(e, "Failed to unbind a service, perhaps the service disappeared?");
            }
        }
    }

    public ServiceCommander getCommander() {
        return commander;
    }
}
