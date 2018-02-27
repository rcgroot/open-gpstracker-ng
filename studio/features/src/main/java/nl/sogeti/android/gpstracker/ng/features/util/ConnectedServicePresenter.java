/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.features.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;

import kotlin.jvm.functions.Function1;
import nl.sogeti.android.gpstracker.ng.base.common.abstractpresenters.ContextedPresenter;
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface;
import nl.sogeti.android.gpstracker.service.util.ContentProviderExtensionsKt;
import nl.sogeti.android.gpstracker.service.util.TrackUriExtensionKt;
import timber.log.Timber;

import static nl.sogeti.android.gpstracker.service.integration.ContentConstants.TracksColumns.NAME;

/**
 * Base class for presenters that source data from the IPC / Intent with
 * the original Open GPS Tracker app.
 */
public abstract class ConnectedServicePresenter extends ContextedPresenter {

    @Inject
    public ServiceManagerInterface serviceManager;
    @Inject
    @Named("stateBroadcastAction")
    public String stateBroadcastAction;

    private BroadcastReceiver loggingStateReceiver;

    @Override
    public void didStart() {
        registerReceiver();
        final Context context = getContext();
        serviceManager.startup(context, new Runnable() {
            @Override
            public void run() {
                synchronized (ConnectedServicePresenter.this) {
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    long trackId = serviceManager.getTrackId();
                                    Uri trackUri = null;
                                    String name = null;
                                    if (trackId > 0) {
                                        trackUri = TrackUriExtensionKt.trackUri(trackId);
                                        name = ContentProviderExtensionsKt.apply(trackUri, null, null, new Function1<Cursor, String>() {
                                            @Override
                                            public String invoke(Cursor cursor) {
                                                return ContentProviderExtensionsKt.getString(cursor, NAME);
                                            }
                                        });
                                    }
                                    int loggingState = serviceManager.getLoggingState();
                                    Timber.d("onConnect LoggerState %s %s %d", trackUri, name, loggingState);
                                    didConnectToService(context, trackUri, name, loggingState);
                                }
                            }
                    );
                }
            }
        });
    }

    @Override
    public void willStop() {
        unregisterReceiver();
        serviceManager.shutdown(getContext());
    }

    private void registerReceiver() {
        unregisterReceiver();
        loggingStateReceiver = new LoggerStateReceiver();
        Context context = getContext();
        context.registerReceiver(loggingStateReceiver, new IntentFilter(stateBroadcastAction));
    }

    private void unregisterReceiver() {
        Context context = getContext();
        if (loggingStateReceiver != null) {
            context.unregisterReceiver(loggingStateReceiver);
            loggingStateReceiver = null;
        }
    }

    public void setServiceManager(ServiceManagerInterface serviceManager) {
        this.serviceManager = serviceManager;
    }

    public abstract void didChangeLoggingState(@NonNull Context context, @Nullable Uri trackUri, @Nullable String name, int loggingState);

    public abstract void didConnectToService(@NonNull Context context, @Nullable Uri trackUri, @Nullable String name, int loggingState);

    private class LoggerStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int loggingState = intent.getIntExtra(ServiceConstants.EXTRA_LOGGING_STATE, ServiceConstants.STATE_UNKNOWN);
            Uri trackUri = intent.getParcelableExtra(ServiceConstants.EXTRA_TRACK);
            String name = intent.getStringExtra(ServiceConstants.EXTRA_TRACK_NAME);

            Timber.d("onReceive LoggerStateReceiver %s %s %d", trackUri, name, loggingState);
            didChangeLoggingState(context, trackUri, name, loggingState);
        }
    }
}
