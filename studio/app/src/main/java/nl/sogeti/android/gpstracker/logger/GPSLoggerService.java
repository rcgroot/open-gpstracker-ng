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
package nl.sogeti.android.gpstracker.logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import nl.sogeti.android.gpstracker.R;
import nl.sogeti.android.gpstracker.db.GPStracking.Media;
import nl.sogeti.android.gpstracker.db.GPStracking.MetaData;
import nl.sogeti.android.gpstracker.db.GPStracking.Tracks;
import nl.sogeti.android.gpstracker.db.GPStracking.Waypoints;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * A system service as controlling the background logging of gps locations.
 *
 * @version $Id$
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 */
public class GPSLoggerService extends Service implements LocationListener
{
   private static final float FINE_DISTANCE = 5F;
   private static final long  FINE_INTERVAL = 1000l;
   private static final float FINE_ACCURACY = 20f;

   private static final float NORMAL_DISTANCE = 10F;
   private static final long  NORMAL_INTERVAL = 15000l;
   private static final float NORMAL_ACCURACY = 30f;

   private static final float COARSE_DISTANCE = 25F;
   private static final long  COARSE_INTERVAL = 30000l;
   private static final float COARSE_ACCURACY = 75f;

   private static final float GLOBAL_DISTANCE = 500F;
   private static final long  GLOBAL_INTERVAL = 300000l;
   private static final float GLOBAL_ACCURACY = 1000f;

   /**
    * <code>MAX_REASONABLE_SPEED</code> is about 324 kilometer per hour or 201
    * mile per hour.
    */
   private static final int MAX_REASONABLE_SPEED = 90;

   /**
    * <code>MAX_REASONABLE_ALTITUDECHANGE</code> between the last few waypoints
    * and a new one the difference should be less then 200 meter.
    */
   private static final int MAX_REASONABLE_ALTITUDECHANGE = 200;

   private static final Boolean DEBUG = false;
   private static final boolean VERBOSE = false;
   private static final String TAG = "OGT.GPSLoggerService";

   private static final String SERVICESTATE_DISTANCE = "SERVICESTATE_DISTANCE";
   private static final String SERVICESTATE_STATE = "SERVICESTATE_STATE";
   private static final String SERVICESTATE_PRECISION = "SERVICESTATE_PRECISION";
   private static final String SERVICESTATE_SEGMENTID = "SERVICESTATE_SEGMENTID";
   private static final String SERVICESTATE_TRACKID = "SERVICESTATE_TRACKID";

   private static final int ADDGPSSTATUSLISTENER = 0;
   private static final int REQUEST_FINEGPS_LOCATIONUPDATES = 1;
   private static final int REQUEST_NORMALGPS_LOCATIONUPDATES = 2;
   private static final int REQUEST_COARSEGPS_LOCATIONUPDATES = 3;
   private static final int REQUEST_GLOBALNETWORK_LOCATIONUPDATES = 4;
   private static final int REQUEST_CUSTOMGPS_LOCATIONUPDATES = 5;
   private static final int STOPLOOPER = 6;
   private static final int GPSPROBLEM = 7;

   private static final int NOTIFICATION_UNAVAILABLE = R.string.service_name -1;
   private static final int NOTIFICATION_STATUS = R.string.service_name - 2;

   /**
    * DUP from android.app.Service.START_STICKY
    */
   private static final int START_STICKY = 1;

   public static final String COMMAND = "nl.sogeti.android.gpstracker.extra.COMMAND";
   public static final int EXTRA_COMMAND_START = 0;
   public static final int EXTRA_COMMAND_PAUSE = 1;
   public static final int EXTRA_COMMAND_RESUME = 2;
   public static final int EXTRA_COMMAND_STOP = 3;

   private LocationManager mLocationManager;
   private NotificationManager mNoticationManager;
   private PowerManager.WakeLock mWakeLock;
   private Handler mHandler;

   /**
    * If speeds should be checked to sane values
    */
   private boolean mSpeedSanityCheck;

   private long mTrackId = -1;
   private long mSegmentId = -1;
   private long mWaypointId = -1;
   private int mPrecision;
   private int mLoggingState = Constants.STOPPED;
   private boolean mStartNextSegment;

   private String mSources;

   private Location mPreviousLocation;
   private float mDistance;
   private Notification mNotification;

   private Vector<Location> mWeakLocations;
   private Queue<Double> mAltitudes;

   /**
    * <code>mAcceptableAccuracy</code> indicates the maximum acceptable accuracy
    * of a waypoint in meters.
    */
   private float mMaxAcceptableAccuracy = 20;
   private int mSatellites = 0;

   private boolean mShowingGpsDisabled;

   /**
    * Should the GPS Status monitor update the notification bar
    */
   private boolean mStatusMonitor;

   /**
    * Time thread to runs tasks that check whether the GPS listener has received
    * enough to consider the GPS system alive.
    */
   private Timer mHeartbeatTimer;

   /**
    * Listens to changes in preference to precision and sanity checks
    */
   private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener()
   {

      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
      {
         if (key.equals(Constants.PRECISION) || key.equals(Constants.LOGGING_DISTANCE) || key.equals(Constants.LOGGING_INTERVAL))
         {
            sendRequestLocationUpdatesMessage();
            crashProtectState();
            updateNotification();
            broadCastLoggingState();
         }
         else if (key.equals(Constants.SPEEDSANITYCHECK))
         {
            mSpeedSanityCheck = sharedPreferences.getBoolean(Constants.SPEEDSANITYCHECK, true);
         }
         else if (key.equals(Constants.STATUS_MONITOR))
         {
            mLocationManager.removeGpsStatusListener(mStatusListener);
            sendRequestStatusUpdateMessage();
            updateNotification();
         }
      }
   };


   @Override
   public void onLocationChanged(Location location)
   {
      if (VERBOSE)
      {
         Log.v(TAG, "onLocationChanged( Location " + location + " )");
      }
      ;
      // Might be claiming GPS disabled but when we were paused this changed and this location proves so
      if (mShowingGpsDisabled)
      {
         notifyOnEnabledProviderNotification(R.string.service_gpsenabled);
      }
      Location filteredLocation = locationFilter(location);
      if (filteredLocation != null)
      {
         if (mStartNextSegment)
         {
            mStartNextSegment = false;
            // Obey the start segment if the previous location is unknown or far away
            if (mPreviousLocation == null || filteredLocation.distanceTo(mPreviousLocation) > 4 * mMaxAcceptableAccuracy)
            {
               startNewSegment();
            }
         }
         else if( mPreviousLocation != null )
         {
            mDistance += mPreviousLocation.distanceTo(filteredLocation);
         }
         storeLocation(filteredLocation);
         mPreviousLocation = location;
      }
   }
   @Override
   public void onProviderDisabled(String provider)
   {
      if (DEBUG)
      {
         Log.d(TAG, "onProviderDisabled( String " + provider + " )");
      }
      ;
      if (mPrecision != Constants.LOGGING_GLOBAL && provider.equals(LocationManager.GPS_PROVIDER))
      {
         notifyOnDisabledProvider(R.string.service_gpsdisabled);
      }
      else if (mPrecision == Constants.LOGGING_GLOBAL && provider.equals(LocationManager.NETWORK_PROVIDER))
      {
         notifyOnDisabledProvider(R.string.service_datadisabled);
      }

   }

   @Override
   public void onProviderEnabled(String provider)
   {
      if (DEBUG)
      {
         Log.d(TAG, "onProviderEnabled( String " + provider + " )");
      }
      ;
      if (mPrecision != Constants.LOGGING_GLOBAL && provider.equals(LocationManager.GPS_PROVIDER))
      {
         notifyOnEnabledProviderNotification(R.string.service_gpsenabled);
         mStartNextSegment = true;
      }
      else if (mPrecision == Constants.LOGGING_GLOBAL && provider.equals(LocationManager.NETWORK_PROVIDER))
      {
         notifyOnEnabledProviderNotification(R.string.service_dataenabled);
      }
   }

   @Override
   public void onStatusChanged(String provider, int status, Bundle extras)
   {
      if (DEBUG)
      {
         Log.d(TAG, "onStatusChanged( String " + provider + ", int " + status + ", Bundle " + extras + " )");
      }
      ;
      if (status == LocationProvider.OUT_OF_SERVICE)
      {
         Log.e(TAG, String.format("Provider %s changed to status %d", provider, status));
      }
   }

   /**
    * Listens to GPS status changes
    */
   private Listener mStatusListener = new Listener()
   {
      @Override
      public synchronized void onGpsStatusChanged(int event)
      {
         switch (event)
         {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
               if (mStatusMonitor)
               {
                  GpsStatus status = mLocationManager.getGpsStatus(null);
                  mSatellites = 0;
                  Iterable<GpsSatellite> list = status.getSatellites();
                  for (GpsSatellite satellite : list)
                  {
                     if (satellite.usedInFix())
                     {
                        mSatellites++;
                     }
                  }
                  updateNotification();
               }
               break;
            case GpsStatus.GPS_EVENT_STOPPED:
               break;
            case GpsStatus.GPS_EVENT_STARTED:
               break;
            default:
               break;
         }
      }
   };
   private IBinder mBinder = new IGPSLoggerServiceRemote.Stub()
   {
      @Override
      public int loggingState() throws RemoteException
      {
         return mLoggingState;
      }

      @Override
      public long startLogging() throws RemoteException
      {
         GPSLoggerService.this.startLogging();
         return mTrackId;
      }

      @Override
      public void pauseLogging() throws RemoteException
      {
         GPSLoggerService.this.pauseLogging();
      }

      @Override
      public long resumeLogging() throws RemoteException
      {
         GPSLoggerService.this.resumeLogging();
         return mSegmentId;
      }

      @Override
      public void stopLogging() throws RemoteException
      {
         GPSLoggerService.this.stopLogging();
      }

      @Override
      public Uri storeMediaUri(Uri mediaUri) throws RemoteException
      {
         GPSLoggerService.this.storeMediaUri(mediaUri);
         return null;
      }

      @Override
      public boolean isMediaPrepared() throws RemoteException
      {
         return GPSLoggerService.this.isMediaPrepared();
      }

      @Override
      public void storeDerivedDataSource(String sourceName) throws RemoteException
      {
         GPSLoggerService.this.storeDerivedDataSource(sourceName);
      }

      @Override
      public Location getLastWaypoint() throws RemoteException
      {
         return GPSLoggerService.this.getLastWaypoint();
      }

      @Override
      public float getTrackedDistance() throws RemoteException
      {
         return GPSLoggerService.this.getTrackedDistance();
      }
   };

   /**
    * Task that will be run periodically during active logging to verify that
    * the logging really happens and that the GPS hasn't silently stopped.
    */
   private TimerTask mHeartbeat = null;

   /**
    * Task to determine if the GPS is alive
    */
   class Heartbeat extends TimerTask
   {

      private String mProvider;

      public Heartbeat(String provider)
      {
         mProvider = provider;
      }

      @Override
      public void run()
      {
         if (isLogging())
         {
            // Collect the last location from the last logged location or a more recent from the last weak location
            Location checkLocation = mPreviousLocation;
            synchronized (mWeakLocations)
            {
               if (!mWeakLocations.isEmpty())
               {
                  if (checkLocation == null)
                  {
                     checkLocation = mWeakLocations.lastElement();
                  }
                  else
                  {
                     Location weakLocation = mWeakLocations.lastElement();
                     checkLocation = weakLocation.getTime() > checkLocation.getTime() ? weakLocation : checkLocation;
                  }
               }
            }
            // Is the last known GPS location something nearby we are not told?
            Location managerLocation = mLocationManager.getLastKnownLocation(mProvider);
            if (managerLocation != null && checkLocation != null)
            {
               if (checkLocation.distanceTo(managerLocation) < 2 * mMaxAcceptableAccuracy)
               {
                  checkLocation = managerLocation.getTime() > checkLocation.getTime() ? managerLocation : checkLocation;
               }
            }

            if (checkLocation == null || checkLocation.getTime() + mCheckPeriod < new Date().getTime())
            {
               Log.w(TAG, "GPS system failed to produce a location during logging: " + checkLocation);
               mLoggingState = Constants.PAUSED;
               resumeLogging();

               if (mStatusMonitor)
               {
                  soundGpsSignalAlarm();
               }

            }
         }
      }
   };

   /**
    * Number of milliseconds that a functioning GPS system needs to provide a
    * location. Calculated to be either 120 seconds or 4 times the requested
    * period, whichever is larger.
    */
   private long mCheckPeriod;

   private class GPSLoggerServiceThread extends Thread
   {
      public Semaphore ready = new Semaphore(0);

      GPSLoggerServiceThread()
      {
         this.setName("GPSLoggerServiceThread");
      }

      @Override
      public void run()
      {
         Looper.prepare();
         mHandler = new Handler()
         {
            @Override
            public void handleMessage(Message msg)
            {
               _handleMessage(msg);
            }
         };
         ready.release(); // Signal the looper and handler are created 
         Looper.loop();
      }
   }

   /**
    * Called by the system when the service is first created. Do not call this
    * method directly. Be sure to call super.onCreate().
    */
   @Override
   public void onCreate()
   {
      super.onCreate();
      if (DEBUG)
      {
         Log.d(TAG, "onCreate()");
      }
      ;

      GPSLoggerServiceThread looper = new GPSLoggerServiceThread();
      looper.start();
      try
      {
         looper.ready.acquire();
      }
      catch (InterruptedException e)
      {
         Log.e(TAG, "Interrupted during wait for the GPSLoggerServiceThread to start, prepare for trouble!", e);
      }
      mHeartbeatTimer = new Timer("heartbeat", true);

      mWeakLocations = new Vector<Location>(3);
      mAltitudes = new LinkedList<Double>();
      mLoggingState = Constants.STOPPED;
      mStartNextSegment = false;
      mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
      mNoticationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
      stopNotification();

      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
      mSpeedSanityCheck = sharedPreferences.getBoolean(Constants.SPEEDSANITYCHECK, true);
      boolean startImmidiatly = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.LOGATSTARTUP, false);

      crashRestoreState();
      if (startImmidiatly && mLoggingState == Constants.STOPPED)
      {
         startLogging();
         ContentValues values = new ContentValues();
         values.put(Tracks.NAME, "Recorded at startup");
         getContentResolver().update(ContentUris.withAppendedId(Tracks.CONTENT_URI, mTrackId), values, null, null);
      }
      else
      {
         broadCastLoggingState();
      }
   }

   /**
    * This is the old onStart method that will be called on the pre-2.0
    * 
    * @see Service#onStart(Intent, int) platform. On
    *      2.0 or later we override onStartCommand() so this method will not be
    *      called.
    */
   @Override
   public void onStart(Intent intent, int startId)
   {
      handleCommand(intent);
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId)
   {
      handleCommand(intent);
      // We want this service to continue running until it is explicitly
      // stopped, so return sticky.
      return START_STICKY;
   }

   private void handleCommand(Intent intent)
   {
      if (DEBUG)
      {
         Log.d(TAG, "handleCommand(Intent " + intent + ")");
      }
      ;
      if (intent != null && intent.hasExtra(COMMAND))
      {
         switch (intent.getIntExtra(COMMAND, -1))
         {
            case EXTRA_COMMAND_START:
               startLogging();
               break;
            case EXTRA_COMMAND_PAUSE:
               pauseLogging();
               break;
            case EXTRA_COMMAND_RESUME:
               resumeLogging();
               break;
            case EXTRA_COMMAND_STOP:
               stopLogging();
               break;
            default:
               break;
         }
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see Service#onDestroy()
    */
   @Override
   public void onDestroy()
   {
      if (DEBUG)
      {
         Log.d(TAG, "onDestroy()");
      }
      ;
      super.onDestroy();

      if (isLogging())
      {
         Log.w(TAG, "Destroyin an activly logging service");
      }
      mHeartbeatTimer.cancel();
      mHeartbeatTimer.purge();
      if (this.mWakeLock != null)
      {
         this.mWakeLock.release();
         this.mWakeLock = null;
      }
      PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
      mLocationManager.removeGpsStatusListener(mStatusListener);
      stopListening();
      mNoticationManager.cancel(NOTIFICATION_STATUS);

      Message msg = Message.obtain();
      msg.what = STOPLOOPER;
      mHandler.sendMessage(msg);
   }

   private void crashProtectState()
   {
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      Editor editor = preferences.edit();
      editor.putLong(SERVICESTATE_TRACKID, mTrackId);
      editor.putLong(SERVICESTATE_SEGMENTID, mSegmentId);
      editor.putInt(SERVICESTATE_PRECISION, mPrecision);
      editor.putInt(SERVICESTATE_STATE, mLoggingState);
      editor.putFloat(SERVICESTATE_DISTANCE, mDistance);
      editor.commit();
      if (DEBUG)
      {
         Log.d(TAG, "crashProtectState()");
      }
      ;
   }

   private synchronized void crashRestoreState()
   {
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      long previousState = preferences.getInt(SERVICESTATE_STATE, Constants.STOPPED);
      if (previousState == Constants.LOGGING || previousState == Constants.PAUSED)
      {
         Log.w(TAG, "Recovering from a crash or kill and restoring state.");
         startNotification();

         mTrackId = preferences.getLong(SERVICESTATE_TRACKID, -1);
         mSegmentId = preferences.getLong(SERVICESTATE_SEGMENTID, -1);
         mPrecision = preferences.getInt(SERVICESTATE_PRECISION, -1);
         mDistance = preferences.getFloat(SERVICESTATE_DISTANCE, 0F);
         if (previousState == Constants.LOGGING)
         {
            mLoggingState = Constants.PAUSED;
            resumeLogging();
         }
         else if (previousState == Constants.PAUSED)
         {
            mLoggingState = Constants.LOGGING;
            pauseLogging();
         }
      }
   }

   @Override
   public IBinder onBind(Intent intent)
   {
      return this.mBinder;
   }

   protected boolean isLogging()
   {
      return this.mLoggingState == Constants.LOGGING;
   }

   /**
    * Provides the cached last stored waypoint it current logging is active alse
    * null.
    * 
    * @return last waypoint location or null
    */
   protected Location getLastWaypoint()
   {
      Location myLastWaypoint = null;
      if (isLogging())
      {
         myLastWaypoint = mPreviousLocation;
      }
      return myLastWaypoint;
   }
   
   public float getTrackedDistance()
   {
      float distance = 0F;
      if (isLogging())
      {
         distance = mDistance;
      }
      return distance;
   }

   protected boolean isMediaPrepared()
   {
      return !(mTrackId < 0 || mSegmentId < 0 || mWaypointId < 0);
   }

   public synchronized void startLogging()
   {
      if (DEBUG)
      {
         Log.d(TAG, "startLogging()");
      }
      ;
      if (this.mLoggingState == Constants.STOPPED)
      {
         startNewTrack();
         sendRequestLocationUpdatesMessage();
         sendRequestStatusUpdateMessage();
         this.mLoggingState = Constants.LOGGING;
         updateWakeLock();
         startNotification();
         crashProtectState();
         broadCastLoggingState();
      }
   }

   public synchronized void pauseLogging()
   {
      if (DEBUG)
      {
         Log.d(TAG, "pauseLogging()");
      }
      ;
      if (this.mLoggingState == Constants.LOGGING)
      {
         mLocationManager.removeGpsStatusListener(mStatusListener);
         stopListening();
         mLoggingState = Constants.PAUSED;
         mPreviousLocation = null;
         updateWakeLock();
         updateNotification();
         mSatellites = 0;
         updateNotification();
         crashProtectState();
         broadCastLoggingState();
      }
   }

   public synchronized void resumeLogging()
   {
      if (DEBUG)
      {
         Log.d(TAG, "resumeLogging()");
      }
      ;
      if (this.mLoggingState == Constants.PAUSED)
      {
         if (mPrecision != Constants.LOGGING_GLOBAL)
         {
            mStartNextSegment = true;
         }
         sendRequestLocationUpdatesMessage();
         sendRequestStatusUpdateMessage();

         this.mLoggingState = Constants.LOGGING;
         updateWakeLock();
         updateNotification();
         crashProtectState();
         broadCastLoggingState();
      }
   }

   public synchronized void stopLogging()
   {
      if (DEBUG)
      {
         Log.d(TAG, "stopLogging()");
      }
      ;
      mLoggingState = Constants.STOPPED;
      crashProtectState();

      updateWakeLock();

      PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);

      mLocationManager.removeGpsStatusListener(mStatusListener);
      stopListening();
      stopNotification();

      broadCastLoggingState();
   }

   private void startListening(String provider, long intervaltime, float distance)
   {
      mLocationManager.removeUpdates(this);
      mLocationManager.requestLocationUpdates(provider, intervaltime, distance, this);
      mCheckPeriod = Math.max(12 * intervaltime, 120 * 1000);
      if (mHeartbeat != null)
      {
         mHeartbeat.cancel();
         mHeartbeat = null;
      }
      mHeartbeat = new Heartbeat(provider);
      mHeartbeatTimer.schedule(mHeartbeat, mCheckPeriod, mCheckPeriod);
   }

   private void stopListening()
   {
      if (mHeartbeat != null)
      {
         mHeartbeat.cancel();
         mHeartbeat = null;
      }
      mLocationManager.removeUpdates(this);
   }

   public void storeDerivedDataSource(String sourceName)
   {
      Uri trackMetaDataUri = Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId + "/metadata");

      if (mTrackId >= 0)
      {
         if (mSources == null)
         {
            Cursor metaData = null;
            String source = null;
            try
            {
               metaData = this.getContentResolver().query(trackMetaDataUri, new String[] { MetaData.VALUE }, MetaData.KEY + " = ? ",
                     new String[] { Constants.DATASOURCES_KEY }, null);
               if (metaData.moveToFirst())
               {
                  source = metaData.getString(0);
               }
            }
            finally
            {
               if (metaData != null)
               {
                  metaData.close();
               }
            }
            if (source != null)
            {
               mSources = source;
            }
            else
            {
               mSources = sourceName;
               ContentValues args = new ContentValues();
               args.put(MetaData.KEY, Constants.DATASOURCES_KEY);
               args.put(MetaData.VALUE, mSources);
               this.getContentResolver().insert(trackMetaDataUri, args);
            }
         }

         if (!mSources.contains(sourceName))
         {
            mSources += "," + sourceName;
            ContentValues args = new ContentValues();
            args.put(MetaData.VALUE, mSources);
            this.getContentResolver().update(trackMetaDataUri, args, MetaData.KEY + " = ? ", new String[] { Constants.DATASOURCES_KEY });
         }
      }
   }

   private void startNotification()
   {
      mNoticationManager.cancel(NOTIFICATION_STATUS);

      int icon = R.drawable.ic_maps_indicator_current_position;
      CharSequence tickerText = getResources().getString(R.string.service_start);
      long when = System.currentTimeMillis();

      mNotification = new Notification(icon, tickerText, when);
      mNotification.flags |= Notification.FLAG_ONGOING_EVENT;

      updateNotification();

      if (Build.VERSION.SDK_INT >= 5)
      {
         startForegroundReflected(NOTIFICATION_STATUS, mNotification);
      }
      else
      {
         mNoticationManager.notify(NOTIFICATION_STATUS, mNotification);
      }
   }

   private void updateNotification()
   {
      CharSequence contentTitle = getResources().getString(R.string.service_name);

      String precision = getResources().getStringArray(R.array.precision_choices)[mPrecision];
      String state = getResources().getStringArray(R.array.state_choices)[mLoggingState - 1];
      CharSequence contentText;
      switch (mPrecision)
      {
         case (Constants.LOGGING_GLOBAL):
            contentText = getResources().getString(R.string.service_networkstatus, state, precision);
            break;
         default:
            if (mStatusMonitor)
            {
               contentText = getResources().getString(R.string.service_gpsstatus, state, precision, mSatellites);
            }
            else
            {
               contentText = getResources().getString(R.string.service_gpsnostatus, state, precision);
            }
            break;
      }
      Intent notificationIntent = new Intent(this, Constants.LOGGER_ACTIVITY_CLASS);
      notificationIntent.setData(ContentUris.withAppendedId(Tracks.CONTENT_URI, mTrackId));
      mNotification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
      mNotification.setLatestEventInfo(this, contentTitle, contentText, mNotification.contentIntent);
      mNoticationManager.notify(NOTIFICATION_STATUS, mNotification);
   }

   private void stopNotification()
   {
      if (Build.VERSION.SDK_INT >= 5)
      {
         stopForegroundReflected(true);
      }
      else
      {
         mNoticationManager.cancel(NOTIFICATION_STATUS);
      }
   }

   private void notifyOnEnabledProviderNotification(int resId)
   {
      mNoticationManager.cancel(NOTIFICATION_UNAVAILABLE);
      mShowingGpsDisabled = false;
      CharSequence text = this.getString(resId);
      Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
      toast.show();
   }

   private void notifyOnPoorSignal(int resId)
   {
      int icon = R.drawable.ic_maps_indicator_current_position;
      CharSequence tickerText = getResources().getString(resId);
      long when = System.currentTimeMillis();
      Notification signalNotification = new Notification(icon, tickerText, when);
      CharSequence contentTitle = getResources().getString(R.string.service_name);
      Intent notificationIntent = new Intent(this, Constants.LOGGER_ACTIVITY_CLASS);
      PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
      signalNotification.setLatestEventInfo(this, contentTitle, tickerText, contentIntent);
      signalNotification.flags |= Notification.FLAG_AUTO_CANCEL;

      mNoticationManager.notify(resId, signalNotification);
   }

   private void notifyOnDisabledProvider(int resId)
   {
      int icon = R.drawable.ic_maps_indicator_current_position;
      CharSequence tickerText = getResources().getString(resId);
      long when = System.currentTimeMillis();
      Notification gpsNotification = new Notification(icon, tickerText, when);
      gpsNotification.flags |= Notification.FLAG_AUTO_CANCEL;

      CharSequence contentTitle = getResources().getString(R.string.service_name);
      CharSequence contentText = getResources().getString(resId);
      Intent notificationIntent = new Intent(this, Constants.LOGGER_ACTIVITY_CLASS);
      notificationIntent.setData(ContentUris.withAppendedId(Tracks.CONTENT_URI, mTrackId));
      PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
      gpsNotification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);

      mNoticationManager.notify(NOTIFICATION_UNAVAILABLE, gpsNotification);
      mShowingGpsDisabled = true;
   }

   /**
    * Send a system broadcast to notify a change in the logging or precision
    */
   private void broadCastLoggingState()
   {
      Intent broadcast = new Intent(Constants.LOGGING_STATE_CHANGED_ACTION);
      broadcast.putExtra(Constants.EXTRA_LOGGING_PRECISION, mPrecision);
      broadcast.putExtra(Constants.EXTRA_LOGGING_STATE, mLoggingState);
      this.getApplicationContext().sendBroadcast(broadcast);
   }

   private void sendRequestStatusUpdateMessage()
   {
      mStatusMonitor = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.STATUS_MONITOR, false);
      Message msg = Message.obtain();
      msg.what = ADDGPSSTATUSLISTENER;
      mHandler.sendMessage(msg);
   }

   private void sendRequestLocationUpdatesMessage()
   {
      stopListening();
      mPrecision = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PRECISION, "2")).intValue();
      Message msg = Message.obtain();
      switch (mPrecision)
      {
         case (Constants.LOGGING_FINE): // Fine
            msg.what = REQUEST_FINEGPS_LOCATIONUPDATES;
            mHandler.sendMessage(msg);
            break;
         case (Constants.LOGGING_NORMAL): // Normal
            msg.what = REQUEST_NORMALGPS_LOCATIONUPDATES;
            mHandler.sendMessage(msg);
            break;
         case (Constants.LOGGING_COARSE): // Coarse
            msg.what = REQUEST_COARSEGPS_LOCATIONUPDATES;
            mHandler.sendMessage(msg);
            break;
         case (Constants.LOGGING_GLOBAL): // Global
            msg.what = REQUEST_GLOBALNETWORK_LOCATIONUPDATES;
            mHandler.sendMessage(msg);
            break;
         case (Constants.LOGGING_CUSTOM): // Global
            msg.what = REQUEST_CUSTOMGPS_LOCATIONUPDATES;
            mHandler.sendMessage(msg);
            break;
         default:
            Log.e(TAG, "Unknown precision " + mPrecision);
            break;
      }
   }

   /**
    * Message handler method to do the work off-loaded by mHandler to
    * GPSLoggerServiceThread
    * 
    * @param msg
    */
   private void _handleMessage(Message msg)
   {
      if (DEBUG)
      {
         Log.d(TAG, "_handleMessage( Message " + msg + " )");
      }
      ;
      long intervaltime = 0;
      float distance = 0;
      switch (msg.what)
      {
         case ADDGPSSTATUSLISTENER:
            this.mLocationManager.addGpsStatusListener(mStatusListener);
            break;
         case REQUEST_FINEGPS_LOCATIONUPDATES:
            mMaxAcceptableAccuracy = FINE_ACCURACY;
            intervaltime = FINE_INTERVAL;
            distance = FINE_DISTANCE;
            startListening(LocationManager.GPS_PROVIDER, intervaltime, distance);
            break;
         case REQUEST_NORMALGPS_LOCATIONUPDATES:
            mMaxAcceptableAccuracy = NORMAL_ACCURACY;
            intervaltime = NORMAL_INTERVAL;
            distance = NORMAL_DISTANCE;
            startListening(LocationManager.GPS_PROVIDER, intervaltime, distance);
            break;
         case REQUEST_COARSEGPS_LOCATIONUPDATES:
            mMaxAcceptableAccuracy = COARSE_ACCURACY;
            intervaltime = COARSE_INTERVAL;
            distance = COARSE_DISTANCE;
            startListening(LocationManager.GPS_PROVIDER, intervaltime, distance);
            break;
         case REQUEST_GLOBALNETWORK_LOCATIONUPDATES:
            mMaxAcceptableAccuracy = GLOBAL_ACCURACY;
            intervaltime = GLOBAL_INTERVAL;
            distance = GLOBAL_DISTANCE;
            startListening(LocationManager.NETWORK_PROVIDER, intervaltime, distance);
            if (!isNetworkConnected())
            {
               notifyOnDisabledProvider(R.string.service_connectiondisabled);
            }
            break;
         case REQUEST_CUSTOMGPS_LOCATIONUPDATES:
            intervaltime = 60 * 1000 * Long.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.LOGGING_INTERVAL, "15000"));
            distance = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.LOGGING_DISTANCE, "10"));
            mMaxAcceptableAccuracy = Math.max(10f, Math.min(distance, 50f));
            startListening(LocationManager.GPS_PROVIDER, intervaltime, distance);
            break;
         case STOPLOOPER:
            mLocationManager.removeGpsStatusListener(mStatusListener);
            stopListening();
            Looper.myLooper().quit();
            break;
         case GPSPROBLEM:
            notifyOnPoorSignal(R.string.service_gpsproblem);
            break;
      }
   }

   private void updateWakeLock()
   {
      if (this.mLoggingState == Constants.LOGGING)
      {
         PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

         PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
         if (this.mWakeLock != null)
         {
            this.mWakeLock.release();
            this.mWakeLock = null;
         }
         this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
         this.mWakeLock.acquire();
      }
      else
      {
         if (this.mWakeLock != null)
         {
            this.mWakeLock.release();
            this.mWakeLock = null;
         }
      }
   }

   /**
    * Some GPS waypoints received are of to low a quality for tracking use. Here
    * we filter those out.
    * 
    * @param proposedLocation
    * @return either the (cleaned) original or null when unacceptable
    */
   public Location locationFilter(Location proposedLocation)
   {
      // Do no include log wrong 0.0 lat 0.0 long, skip to next value in while-loop
      if (proposedLocation != null && (proposedLocation.getLatitude() == 0.0d || proposedLocation.getLongitude() == 0.0d))
      {
         Log.w(TAG, "A wrong location was received, 0.0 latitude and 0.0 longitude... ");
         proposedLocation = null;
      }

      // Do not log a waypoint which is more inaccurate then is configured to be acceptable
      if (proposedLocation != null && proposedLocation.getAccuracy() > mMaxAcceptableAccuracy)
      {
         Log.w(TAG, String.format("A weak location was received, lots of inaccuracy... (%f is more then max %f)", proposedLocation.getAccuracy(),
               mMaxAcceptableAccuracy));
         proposedLocation = addBadLocation(proposedLocation);
      }

      // Do not log a waypoint which might be on any side of the previous waypoint
      if (proposedLocation != null && mPreviousLocation != null && proposedLocation.getAccuracy() > mPreviousLocation.distanceTo(proposedLocation))
      {
         Log.w(TAG,
               String.format("A weak location was received, not quite clear from the previous waypoint... (%f more then max %f)",
                     proposedLocation.getAccuracy(), mPreviousLocation.distanceTo(proposedLocation)));
         proposedLocation = addBadLocation(proposedLocation);
      }

      // Speed checks, check if the proposed location could be reached from the previous one in sane speed
      // Common to jump on network logging and sometimes jumps on Samsung Galaxy S type of devices
      if (mSpeedSanityCheck && proposedLocation != null && mPreviousLocation != null)
      {
         // To avoid near instant teleportation on network location or glitches cause continent hopping
         float meters = proposedLocation.distanceTo(mPreviousLocation);
         long seconds = (proposedLocation.getTime() - mPreviousLocation.getTime()) / 1000L;
         float speed = meters / seconds;
         if (speed > MAX_REASONABLE_SPEED)
         {
            Log.w(TAG, "A strange location was received, a really high speed of " + speed + " m/s, prob wrong...");
            proposedLocation = addBadLocation(proposedLocation);
            // Might be a messed up Samsung Galaxy S GPS, reset the logging
            if (speed > 2 * MAX_REASONABLE_SPEED && mPrecision != Constants.LOGGING_GLOBAL)
            {
               Log.w(TAG, "A strange location was received on GPS, reset the GPS listeners");
               stopListening();
               mLocationManager.removeGpsStatusListener(mStatusListener);
               mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
               sendRequestStatusUpdateMessage();
               sendRequestLocationUpdatesMessage();
            }
         }
      }

      // Remove speed if not sane
      if (mSpeedSanityCheck && proposedLocation != null && proposedLocation.getSpeed() > MAX_REASONABLE_SPEED)
      {
         Log.w(TAG, "A strange speed, a really high speed, prob wrong...");
         proposedLocation.removeSpeed();
      }

      // Remove altitude if not sane
      if (mSpeedSanityCheck && proposedLocation != null && proposedLocation.hasAltitude())
      {
         if (!addSaneAltitude(proposedLocation.getAltitude()))
         {
            Log.w(TAG, "A strange altitude, a really big difference, prob wrong...");
            proposedLocation.removeAltitude();
         }
      }
      // Older bad locations will not be needed
      if (proposedLocation != null)
      {
         mWeakLocations.clear();
      }
      return proposedLocation;
   }

   /**
    * Store a bad location, when to many bad locations are stored the the
    * storage is cleared and the least bad one is returned
    * 
    * @param location bad location
    * @return null when the bad location is stored or the least bad one if the
    *         storage was full
    */
   private Location addBadLocation(Location location)
   {
      mWeakLocations.add(location);
      if (mWeakLocations.size() < 3)
      {
         location = null;
      }
      else
      {
         Location best = mWeakLocations.lastElement();
         for (Location whimp : mWeakLocations)
         {
            if (whimp.hasAccuracy() && best.hasAccuracy() && whimp.getAccuracy() < best.getAccuracy())
            {
               best = whimp;
            }
            else
            {
               if (whimp.hasAccuracy() && !best.hasAccuracy())
               {
                  best = whimp;
               }
            }
         }
         synchronized (mWeakLocations)
         {
            mWeakLocations.clear();
         }
         location = best;
      }
      return location;
   }

   /**
    * Builds a bit of knowledge about altitudes to expect and return if the
    * added value is deemed sane.
    * 
    * @param altitude
    * @return whether the altitude is considered sane
    */
   private boolean addSaneAltitude(double altitude)
   {
      boolean sane = true;
      double avg = 0;
      int elements = 0;
      // Even insane altitude shifts increases alter perception
      mAltitudes.add(altitude);
      if (mAltitudes.size() > 3)
      {
         mAltitudes.poll();
      }
      for (Double alt : mAltitudes)
      {
         avg += alt;
         elements++;
      }
      avg = avg / elements;
      sane = Math.abs(altitude - avg) < MAX_REASONABLE_ALTITUDECHANGE;

      return sane;
   }

   /**
    * Trigged by events that start a new track
    */
   private void startNewTrack()
   {
      mDistance = 0;
      Uri newTrack = this.getContentResolver().insert(Tracks.CONTENT_URI, new ContentValues(0));
      mTrackId = Long.valueOf(newTrack.getLastPathSegment()).longValue();
      startNewSegment();
   }

   /**
    * Trigged by events that start a new segment
    */
   private void startNewSegment()
   {
      this.mPreviousLocation = null;
      Uri newSegment = this.getContentResolver().insert(Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId + "/segments"), new ContentValues(0));
      mSegmentId = Long.valueOf(newSegment.getLastPathSegment()).longValue();
      crashProtectState();
   }

   protected void storeMediaUri(Uri mediaUri)
   {
      if (isMediaPrepared())
      {
         Uri mediaInsertUri = Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId + "/segments/" + mSegmentId + "/waypoints/" + mWaypointId + "/media");
         ContentValues args = new ContentValues();
         args.put(Media.URI, mediaUri.toString());
         this.getContentResolver().insert(mediaInsertUri, args);
      }
      else
      {
         Log.e(TAG, "No logging done under which to store the track");
      }
   }

   /**
    * Use the ContentResolver mechanism to store a received location
    * 
    * @param location
    */
   public void storeLocation(Location location)
   {
      if (!isLogging())
      {
         Log.e(TAG, String.format("Not logging but storing location %s, prepare to fail", location.toString()));
      }
      ContentValues args = new ContentValues();

      args.put(Waypoints.LATITUDE, Double.valueOf(location.getLatitude()));
      args.put(Waypoints.LONGITUDE, Double.valueOf(location.getLongitude()));
      args.put(Waypoints.SPEED, Float.valueOf(location.getSpeed()));
      args.put(Waypoints.TIME, Long.valueOf(System.currentTimeMillis()));
      if (location.hasAccuracy())
      {
         args.put(Waypoints.ACCURACY, Float.valueOf(location.getAccuracy()));
      }
      if (location.hasAltitude())
      {
         args.put(Waypoints.ALTITUDE, Double.valueOf(location.getAltitude()));

      }
      if (location.hasBearing())
      {
         args.put(Waypoints.BEARING, Float.valueOf(location.getBearing()));
      }

      Uri waypointInsertUri = Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId + "/segments/" + mSegmentId + "/waypoints");
      Uri inserted = this.getContentResolver().insert(waypointInsertUri, args);
      mWaypointId = Long.parseLong(inserted.getLastPathSegment());
   }

   private boolean isNetworkConnected()
   {
      ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo info = connMgr.getActiveNetworkInfo();

      return (info != null && info.isConnected());
   }

   private void soundGpsSignalAlarm()
   {
      Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      if (alert == null)
      {
         alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         if (alert == null)
         {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
         }
      }
      MediaPlayer mMediaPlayer = new MediaPlayer();
      try
      {
         mMediaPlayer.setDataSource(GPSLoggerService.this, alert);
         final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
         {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
         }
      }
      catch (IllegalArgumentException e)
      {
         Log.e(TAG, "Problem setting data source for mediaplayer", e);
      }
      catch (SecurityException e)
      {
         Log.e(TAG, "Problem setting data source for mediaplayer", e);
      }
      catch (IllegalStateException e)
      {
         Log.e(TAG, "Problem with mediaplayer", e);
      }
      catch (IOException e)
      {
         Log.e(TAG, "Problem with mediaplayer", e);
      }
      Message msg = Message.obtain();
      msg.what = GPSPROBLEM;
      mHandler.sendMessage(msg);
   }

   @SuppressWarnings("rawtypes")
   private void startForegroundReflected(int id, Notification notification)
   {

      Method mStartForeground;
      Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };

      Object[] mStartForegroundArgs = new Object[2];
      mStartForegroundArgs[0] = Integer.valueOf(id);
      mStartForegroundArgs[1] = notification;
      try
      {
         mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
         mStartForeground.invoke(this, mStartForegroundArgs);
      }
      catch (NoSuchMethodException e)
      {
         Log.e(TAG, "Failed starting foreground notification using reflection", e);
      }
      catch (IllegalArgumentException e)
      {
         Log.e(TAG, "Failed starting foreground notification using reflection", e);
      }
      catch (IllegalAccessException e)
      {
         Log.e(TAG, "Failed starting foreground notification using reflection", e);
      }
      catch (InvocationTargetException e)
      {
         Log.e(TAG, "Failed starting foreground notification using reflection", e);
      }

   }

   @SuppressWarnings("rawtypes")
   private void stopForegroundReflected(boolean b)
   {
      Class[] mStopForegroundSignature = new Class[] { boolean.class };

      Method mStopForeground;
      Object[] mStopForegroundArgs = new Object[1];
      mStopForegroundArgs[0] = Boolean.TRUE;
      try
      {
         mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
         mStopForeground.invoke(this, mStopForegroundArgs);
      }
      catch (NoSuchMethodException e)
      {
         Log.e(TAG, "Failed stopping foreground notification using reflection", e);
      }
      catch (IllegalArgumentException e)
      {
         Log.e(TAG, "Failed stopping foreground notification using reflection", e);
      }
      catch (IllegalAccessException e)
      {
         Log.e(TAG, "Failed stopping foreground notification using reflection", e);
      }
      catch (InvocationTargetException e)
      {
         Log.e(TAG, "Failed stopping foreground notification using reflection", e);
      }

   }
}