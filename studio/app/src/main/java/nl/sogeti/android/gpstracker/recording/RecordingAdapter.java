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
package nl.sogeti.android.gpstracker.recording;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import nl.sogeti.android.gpstracker.BaseTrackAdapter;
import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.v2.R;

public class RecordingAdapter extends BaseTrackAdapter {

    private final RecordingViewModel viewModel;
    private ContentObserver observer = new TrackObserver();
    private BroadcastReceiver receiver = new LoggingStateReceiver();
    private boolean isReading;

    RecordingAdapter(RecordingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void start(Context context) {
        super.start(context, true);
        IntentFilter filter = new IntentFilter(ServiceConstants.LOGGING_STATE_CHANGED_ACTION);
        getContext().registerReceiver(receiver, filter);
    }

    public void stop() {
        getContext().unregisterReceiver(receiver);
        getContext().getContentResolver().unregisterContentObserver(observer);
        super.stop();
    }

    @Override
    public void didConnectService() {
        updateRecordingFromService();
    }

    private void updateRecordingFromService() {
        int loggingState = getServiceManager().getLoggingState();
        long trackId = getServiceManager().getTrackId();
        Uri trackUri = null;
        if (trackId != -1) {
            trackUri = ContentUris.withAppendedId(ContentConstants.Tracks.CONTENT_URI, trackId);
        }
        updateRecording(loggingState, trackUri);
    }

    private void updateRecordingFormIntent(Intent intent) {
        int loggingState = intent.getIntExtra(ServiceConstants.EXTRA_LOGGING_STATE, ServiceConstants.STATE_UNKNOWN);
        Uri trackUri = intent.getParcelableExtra(ServiceConstants.EXTRA_TRACK);
        updateRecording(loggingState, trackUri);
    }

    private void updateRecording(int loggingState, Uri trackUri) {
        Boolean isRecording = loggingState == ServiceConstants.STATE_LOGGING;
        viewModel.isRecording.set(isRecording);
        if (trackUri != null) {
            getContext().getContentResolver().registerContentObserver(trackUri, true, observer);
            if (!isReading) {
                readTrack(trackUri, viewModel);
            }
        }
    }

    public void readTrack(final Uri trackUri, final RecordingViewModel recordingViewModel) {
        new TrackReader(trackUri, recordingViewModel).execute();
    }

    private class LoggingStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRecordingFormIntent(intent);
        }
    }

    private class TrackObserver extends ContentObserver {
        public TrackObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (!isReading) {
                updateRecordingFromService();
            }
        }
    }

    private class TrackReader extends AsyncTask<Void, Void, LatLng[]> implements ResultHandler {
        public static final long FIVE_MINUTES_IN_MS = 5L * 60L * 1000L;
        final Uri trackUri;
        final RecordingViewModel recordingViewModel;
        final List<LatLng> collectedWaypoints = new ArrayList<>();
        final List<Long> collectedTimes = new ArrayList<>();
        double speed;

        TrackReader(final Uri trackUri, final RecordingViewModel recordingViewModel) {
            this.trackUri = trackUri;
            this.recordingViewModel = recordingViewModel;
        }

        @Override
        public void addTrack(String name) {
            recordingViewModel.name.set(name);
        }

        @Override
        public void addSegment() {
        }

        @Override
        public Pair<String, String[]> getWaypointSelection() {
            return new Pair<>(ContentConstants.WaypointsColumns.TIME + " > ?", new String[]{Long.toString(System.currentTimeMillis() - FIVE_MINUTES_IN_MS)});
        }

        @Override
        public void addWaypoint(LatLng latLng, long millisecondsTime) {
            collectedWaypoints.add(latLng);
            collectedTimes.add(millisecondsTime);
        }

        @Override
        protected void onPreExecute() {
            isReading = true;
        }

        @Override
        protected LatLng[] doInBackground(Void[] params) {
            readTrack(trackUri, this);
            LatLng[] waypoints = new LatLng[collectedWaypoints.size()];
            double seconds = 0.0;
            double meters = 0.0;
            if (waypoints.length > 0) {
                for (int i = 1; i < waypoints.length; i++) {
                    seconds += collectedTimes.get(i) / 1000 - collectedTimes.get(i - 1) / 1000;
                    LatLng start = collectedWaypoints.get(i);
                    LatLng end = collectedWaypoints.get(i - 1);
                    float[] result = new float[1];
                    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result);
                    meters += result[0];
                }
                speed = meters / 1000 / (seconds / 3600);
            }

            return waypoints;
        }

        @Override
        protected void onPostExecute(LatLng[] segmentedWaypoints) {
            String waypoints = getContext().getResources().getQuantityString(R.plurals.fragment_recording_waypoints, segmentedWaypoints.length, segmentedWaypoints.length);
            recordingViewModel.summary.set(getContext().getString(R.string.fragment_recording_summary, waypoints, speed, "km/h"));
            isReading = false;
        }
    }
}
