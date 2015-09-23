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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Class to interact with the service that tracks and logs the locations
 * 
 * @version $Id$
 * @author rene (c) Jan 18, 2009, Sogeti B.V.
 */
public class GPSLoggerServiceManager
{
   private static final String TAG = "OGT.GPSLoggerServiceManager";
   private static final String REMOTE_EXCEPTION = "REMOTE_EXCEPTION";
   private IGPSLoggerServiceRemote mGPSLoggerRemote;
   public final Object mStartLock = new Object();
   private boolean mBound = false;
   /**
    * Class for interacting with the main interface of the service.
    */
   private ServiceConnection mServiceConnection;
   private Runnable mOnServiceConnected; 
   public GPSLoggerServiceManager(Context ctx)
   {
      ctx.startService(new Intent(Constants.SERVICENAME));
   }

   public Location getLastWaypoint()
   {
      synchronized (mStartLock)
      {
         Location lastWaypoint = null;
         try
         {
            if( mBound )
            {
               lastWaypoint = this.mGPSLoggerRemote.getLastWaypoint();
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mBound );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could get lastWaypoint GPSLoggerService.", e );
         }
         return lastWaypoint;
      }
   }
   

   public float getTrackedDistance()
   {
      synchronized (mStartLock)
      {
         float distance = 0F;
         try
         {
            if( mBound )
            {
               distance = this.mGPSLoggerRemote.getTrackedDistance();
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mBound );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could get tracked distance from GPSLoggerService.", e );
         }
         return distance;
      }
   }
   
   public int getLoggingState()
   {
      synchronized (mStartLock)
      {
         int logging = Constants.UNKNOWN;
         try
         {
            if( mBound )
            {
               logging = this.mGPSLoggerRemote.loggingState();
               //               Log.d( TAG, "mGPSLoggerRemote tells state to be "+logging );
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mBound );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could stat GPSLoggerService.", e );
         }
         return logging;
      }
   }
   
   public boolean isMediaPrepared()
   {
      synchronized (mStartLock)
      {
         boolean prepared = false;
         try
         {
            if( mBound )
            {
               prepared = this.mGPSLoggerRemote.isMediaPrepared();
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mBound );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could stat GPSLoggerService.", e );
         }
         return prepared;
      }
   }

   public long startGPSLogging( String name )
   {
      synchronized (mStartLock)
      {
         if( mBound )
         {
            try
            {
               return this.mGPSLoggerRemote.startLogging();
            }
            catch (RemoteException e)
            {
               Log.e( TAG, "Could not start GPSLoggerService.", e );
            }
         }
         return -1;
      }
   }

   public void pauseGPSLogging()
   {
      synchronized (mStartLock)
      {
         if( mBound )
         {
            try
            {
               this.mGPSLoggerRemote.pauseLogging();
            }
            catch (RemoteException e)
            {
               Log.e( TAG, "Could not start GPSLoggerService.", e );
            }
         }
      }
   }

   public long resumeGPSLogging()
   {
      synchronized (mStartLock)
      {
         if( mBound )
         {
            try
            {
               return this.mGPSLoggerRemote.resumeLogging();
            }
            catch (RemoteException e)
            {
               Log.e( TAG, "Could not start GPSLoggerService.", e );
            }
         }
         return -1;
      }
   }

   public void stopGPSLogging()
   {
      synchronized (mStartLock)
      {
         if( mBound )
         {
            try
            {
               this.mGPSLoggerRemote.stopLogging();
            }
            catch (RemoteException e)
            {
               Log.e( GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not stop GPSLoggerService.", e );
            }
         }
         else
         {
            Log.e( TAG, "No GPSLoggerRemote service connected to this manager" );
         }
      }
   }
   
   public void storeDerivedDataSource( String datasource )
   {
      synchronized (mStartLock)
      {
         if( mBound )
         {
            try
            {
               this.mGPSLoggerRemote.storeDerivedDataSource( datasource );
            }
            catch (RemoteException e)
            {
               Log.e( GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not send datasource to GPSLoggerService.", e );
            }
         }
         else
         {
            Log.e( TAG, "No GPSLoggerRemote service connected to this manager" );
         }
      }
   }

   public void storeMediaUri( Uri mediaUri )
   {
      synchronized (mStartLock)
      {
         if( mBound )
         {
            try
            {
               this.mGPSLoggerRemote.storeMediaUri( mediaUri );
            }
            catch (RemoteException e)
            {
               Log.e( GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not send media to GPSLoggerService.", e );
            }
         }
         else
         {
            Log.e( TAG, "No GPSLoggerRemote service connected to this manager" );
         }
      }
   }
   
   /**
    * Means by which an Activity lifecycle aware object hints about binding and unbinding
    * 
    * @param onServiceConnected Run on main thread after the service is bound
    */
   public void startup( Context context, final Runnable onServiceConnected )
   {
//      Log.d( TAG, "connectToGPSLoggerService()" );
      synchronized (mStartLock)
      {
         if( !mBound )
         {
            mOnServiceConnected = onServiceConnected;
            mServiceConnection = new ServiceConnection()
            {
               @Override
               public void onServiceConnected( ComponentName className, IBinder service )
               {
                  synchronized (mStartLock)
                  {
//                     Log.d( TAG, "onServiceConnected() "+ Thread.currentThread().getId() );
                     GPSLoggerServiceManager.this.mGPSLoggerRemote = IGPSLoggerServiceRemote.Stub.asInterface( service );
                     mBound = true;
                  }
                  if( mOnServiceConnected != null )
                  {
                     mOnServiceConnected.run();
                     mOnServiceConnected = null;
                  }
               }
               @Override
               public void onServiceDisconnected( ComponentName className )
               {
                  synchronized (mStartLock)
                  {
//                     Log.d( TAG, "onServiceDisconnected()"+ Thread.currentThread().getId() );
                     mBound = false;
                  }
               }
            };
            context.bindService( new Intent( Constants.SERVICENAME ), this.mServiceConnection, Context.BIND_AUTO_CREATE );
         }
         else
         {
            Log.w( TAG, "Attempting to connect whilst already connected" );
         }
      }
   }

   /**
    * Means by which an Activity lifecycle aware object hints about binding and unbinding
    */
   public void shutdown(Context context)
   {
//      Log.d( TAG, "disconnectFromGPSLoggerService()" );
      synchronized (mStartLock)
      {
         try
         {
            if( mBound )
            {
//               Log.d( TAG, "unbindService()"+this.mServiceConnection );
               context.unbindService( this.mServiceConnection );
               GPSLoggerServiceManager.this.mGPSLoggerRemote = null;
               mServiceConnection = null;
               mBound = false;
            }
         }
         catch (IllegalArgumentException e)
         {
            Log.w( TAG, "Failed to unbind a service, prehaps the service disapearded?", e );
         }
      }
   }
}