/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2015 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.service.linger;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import timber.log.Timber;

/**
 * Based on android.app.IntentService
 * <p/>
 * Created by grootren on 15-11-14.
 */
public abstract class LingerService extends Service {

    private long mDuration;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private final String mName;
    private boolean isFirstRun;
    private ContinueRunnable continueRunnable;

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            repostContinueRunnable();

            if (msg.obj != null && msg.obj instanceof Intent) {
                onHandleIntent((Intent) msg.obj);
            }

        }
    }

    private void repostContinueRunnable() {
        ContinueRunnable runnable = getContinueRunnable();
        mServiceHandler.removeCallbacks(continueRunnable);
        mServiceHandler.postDelayed(runnable, getLingerDuration() * 1000L);
    }

    public ContinueRunnable getContinueRunnable() {
        if (continueRunnable == null) {
            continueRunnable = new ContinueRunnable();
        }
        return continueRunnable;
    }

    private final class ContinueRunnable implements Runnable {
        @Override
        public void run() {
            if (shouldContinue()) {
                mServiceHandler.postDelayed(this, mDuration * 1000L);
            } else {
                stopSelf();
            }
        }
    }

    /**
     * Call this constructor from your default
     *
     * @param name           name used to create a named thread
     * @param lingerDuration number of seconds the service should linger after work has finished
     */
    public LingerService(String name, int lingerDuration) {
        super();
        mName = name;
        mDuration = lingerDuration;
        isFirstRun = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("LingerService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    final public int onStartCommand(Intent intent, int flags, int startId) {
        if (isFirstRun) {
            isFirstRun = false;
            if (intent == null) {
                didContinue();
            } else {
                didCreate();
            }
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        // Start sticky so ungraceful stop can be detected and relayed into a didContinue()
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
        didDestroy();
    }

    public long getLingerDuration() {
        return mDuration;
    }

    public void setLingerDuration(long mDuration) {
        this.mDuration = mDuration;
        if (continueRunnable != null) {
            this.repostContinueRunnable();
        }
        Timber.d("Service check interval changed to " + mDuration);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is invoked on the main thread when it is first created with an intent
     */
    protected abstract void didCreate();

    /**
     * This method is invoked on the main thread when it is recreated after emergency kill
     */
    protected abstract void didContinue();

    /**
     * This method is invoked on the main thread when the service is destroyed
     */
    protected abstract void didDestroy();

    /**
     * This method is invoked on the worker thread and only one Intent is processed at a time.
     *
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    protected abstract void onHandleIntent(Intent intent);

    protected abstract boolean shouldContinue();

}
