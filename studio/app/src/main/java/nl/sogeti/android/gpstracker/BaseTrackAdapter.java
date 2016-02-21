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
package nl.sogeti.android.gpstracker;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.integration.ServiceManager;

public class BaseTrackAdapter {

    private final BroadcastReceiver receiver = new LoggerStateReceiver();
    private Context context;
    private ServiceManager serviceManager;

    public BaseTrackAdapter() {
        serviceManager = new ServiceManager();
    }

    private static void close(Cursor track) {
        if (track != null) {
            track.close();
        }
    }

    public void start(Context context, boolean withService) {
        this.context = context;
        if (withService) {
            serviceManager.startup(context, new Runnable() {
                @Override
                public void run() {
                    didConnectService();
                }
            });
            IntentFilter filter = new IntentFilter(ServiceConstants.LOGGING_STATE_CHANGED_ACTION);
            context.registerReceiver(receiver, filter);
        }
    }

    public void stop() {
        serviceManager.shutdown(getContext());
        context.unregisterReceiver(receiver);
        context = null;
    }

    public Context getContext() {
        return context;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void didConnectService() {
    }

    public void readTrack(Uri trackUri, ResultHandler handler) {
        ContentResolver resolver = context.getContentResolver();
        Cursor track = null;
        try {
            track = resolver.query(trackUri, new String[]{ContentConstants.Tracks.NAME}, null, null, null);
            if (track != null && track.moveToFirst()) {
                String name = track.getString(0);
                handler.addTrack(name);
            }
        } finally {
            close(track);
        }
        long trackId = ContentUris.parseId(trackUri);
        Uri segmentsUri = Uri.withAppendedPath(trackUri, "segments");
        Cursor segmentsCursor = null;
        try {
            segmentsCursor = resolver.query(segmentsUri, new String[]{ContentConstants.Segments._ID}, null, null, null);
            if (segmentsCursor != null && segmentsCursor.moveToFirst()) {
                do {
                    long segmentId = segmentsCursor.getLong(0);
                    handler.addSegment();
                    Uri waypointsUri = ContentConstants.buildUri(trackId, segmentId);
                    Cursor waypointsCursor = null;
                    try {
                        Pair<String, String[]> selection = handler.getWaypointSelection();
                        if (selection == null) {
                            selection = new Pair<>(null, null);
                        }
                        waypointsCursor = resolver.query(waypointsUri,
                                new String[]{ContentConstants.Waypoints.LATITUDE, ContentConstants.Waypoints.LONGITUDE, ContentConstants.Waypoints.TIME},
                                selection.first,
                                selection.second,
                                null);
                        if (waypointsCursor != null && waypointsCursor.moveToFirst()) {
                            do {
                                LatLng latLng = new LatLng(waypointsCursor.getDouble(0), waypointsCursor.getDouble(1));
                                handler.addWaypoint(latLng, waypointsCursor.getLong(2));
                            } while (waypointsCursor.moveToNext());
                        }
                    } finally {
                        close(waypointsCursor);
                    }
                } while (segmentsCursor.moveToNext());
            }
        } finally {
            close(segmentsCursor);
        }
    }

    public interface ResultHandler {

        void addTrack(String name);

        void addSegment();

        Pair<String, String[]> getWaypointSelection();

        void addWaypoint(LatLng latLng, long millisecondsTime);
    }

    private class LoggerStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            didConnectService();
        }
    }
}