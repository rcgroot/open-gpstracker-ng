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
package nl.sogeti.android.gpstracker.ng.recording;

import android.content.ContentUris;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.Pair;
import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.integration.ServiceManager;
import nl.sogeti.android.gpstracker.ng.common.ConnectedServicePresenter;
import nl.sogeti.android.gpstracker.ng.common.ResultHandler;
import nl.sogeti.android.gpstracker.ng.common.TrackContentReaderKt;
import nl.sogeti.android.gpstracker.v2.R;

public class RecordingPresenter extends ConnectedServicePresenter {

    public static final long FIVE_MINUTES_IN_MS = 5L * 60L * 1000L;

    private final RecordingViewModel viewModel;
    private ContentObserver observer;
    private boolean isReading;

    RecordingPresenter(RecordingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void didStart() {
        super.didStart();
        stopObserver();
    }

    @Override
    public void willStop() {
        super.willStop();
        stopObserver();
    }

    private void stopObserver() {
        if (observer != null) {
            getContext().getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }
    }

    private void startObserver(Uri trackUri) {
        stopObserver();
        observer = new TrackObserver();
        getContext().getContentResolver().registerContentObserver(trackUri, true, observer);
    }


    public void didConnectService(ServiceManager service) {
        int loggingState = service.getLoggingState();
        long trackId = service.getTrackId();
        Uri trackUri = null;
        if (trackId != -1) {
            trackUri = ContentUris.withAppendedId(ContentConstants.Tracks.TRACKS_URI, trackId);
        }
        updateRecording(trackUri, loggingState);
    }

    @Override
    public void didChangeLoggingState(Uri trackUri, int loggingState) {
        updateRecording(trackUri, loggingState);
    }

    private void updateRecording(Uri trackUri, int loggingState) {
        Boolean isRecording = loggingState == ServiceConstants.STATE_LOGGING;
        viewModel.isRecording.set(isRecording);
        viewModel.uri.set(trackUri);
        if (trackUri != null) {
            startObserver(trackUri);
            if (!isReading) {
                readTrack(trackUri, viewModel);
            }
        }
    }

    public void readTrack(final Uri trackUri, final RecordingViewModel recordingViewModel) {
        new TrackReader(trackUri, recordingViewModel).execute();
    }

    private class TrackObserver extends ContentObserver {
        public TrackObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (!isReading) {
                didConnectService(getServiceManager());
            }
        }
    }

    private class TrackReader extends AsyncTask<Void, Void, LatLng[]> implements ResultHandler {
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
            recordingViewModel.uri.set(trackUri);
        }

        @Override
        public void addSegment() {
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
            final String sel = ContentConstants.WaypointsColumns.TIME + " > ?";
            final List<String> args = Arrays.asList(Long.toString(System.currentTimeMillis() - FIVE_MINUTES_IN_MS));
            Pair selection = new Pair<>(sel, args);
            TrackContentReaderKt.readTrack(trackUri, getContext(), this, selection);
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
