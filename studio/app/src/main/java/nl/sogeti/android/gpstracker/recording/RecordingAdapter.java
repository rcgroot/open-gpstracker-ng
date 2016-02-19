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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.Observable;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.BaseTrackAdapter;

public class RecordingAdapter extends BaseTrackAdapter {

    private final RecordingViewModel viewModel;
    private UriChangeListener changeListener;
    private BroadcastReceiver receiver = new LoggingStateReceiver();

    RecordingAdapter(RecordingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void start(Context context) {
        super.start(context);
        changeListener = new UriChangeListener();
        viewModel.uri.addOnPropertyChangedCallback(changeListener);
        IntentFilter filter = new IntentFilter(ServiceConstants.LOGGING_STATE_CHANGED_ACTION);
        getContext().registerReceiver(receiver, filter);
    }

    public void stop() {
        viewModel.uri.removeOnPropertyChangedCallback(changeListener);
        getContext().unregisterReceiver(receiver);
        super.stop();
    }

    private class UriChangeListener extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            Uri trackUri = viewModel.uri.get();
            if (trackUri == null) {
                viewModel.isRecording.set(false);
            } else {
                viewModel.isRecording.set(true);
                readTrack(trackUri, viewModel);
            }
        }
    }

    private class LoggingStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO update viewmodel
        }
    }

    public  void readTrack( final Uri trackUri, final RecordingViewModel recordingViewModel) {
        final ArrayList<LatLng> collectedWaypoints = new ArrayList<>();
        final ResultHandler handler = new ResultHandler() {

            public static final long TEN_MINUTES_IN_MS = 10L * 60L * 1000L;

            @Override
            public void addTrack(String name) {
                recordingViewModel.name.set(name);
            }

            @Override
            public void addSegment() {
            }

            @Override
            public String getWaypointSelection() {
                return ContentConstants.WaypointsColumns.TIME+" > ?";
            }

            @Override
            public String getWaypointSelectionArgs() {

                return Long.toString(System.currentTimeMillis() - TEN_MINUTES_IN_MS);
            }

            @Override
            public void addWaypoint(LatLng latLng) {
                collectedWaypoints.add(latLng);
            }
        };

        new AsyncTask<Void, Void, LatLng[]>() {

            @Override
            protected LatLng[] doInBackground(Void[] params) {
                readTrack(trackUri, handler);
                LatLng[] waypoints = new LatLng[collectedWaypoints.size()];

                return waypoints;
            }

            @Override
            protected void onPostExecute(LatLng[] segmentedWaypoints) {
                recordingViewModel.waypoints.set(segmentedWaypoints);
            }
        }.execute();
    }

}
