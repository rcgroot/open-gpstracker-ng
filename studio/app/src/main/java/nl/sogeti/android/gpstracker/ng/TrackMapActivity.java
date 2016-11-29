/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
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
package nl.sogeti.android.gpstracker.ng;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.ng.about.AboutFragment;
import nl.sogeti.android.gpstracker.ng.map.TrackMapFragment;
import nl.sogeti.android.gpstracker.ng.map.TrackViewModel;
import nl.sogeti.android.gpstracker.ng.recording.RecordingFragment;
import nl.sogeti.android.gpstracker.ng.trackedit.TrackEditDialogFragment;
import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.ActivityTrackMapBinding;


public class TrackMapActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TRACK_URI = "KEY_SELECTED_TRACK_URI";
    private static final int REQUEST_CODE_TRACK_SELECTION = 234;
    private static final String TAG_DIALOG = "DIALOG_TRACK_EDIT";
    private TrackViewModel selectedTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTrackMapBinding binding;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track_map);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.bringToFront();

        TrackMapFragment mapFragment = (TrackMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        selectedTrack = mapFragment.getTrackViewModel();
        selectedTrack.getTrackUri().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                supportInvalidateOptionsMenu();
            }
        });
        binding.setTrack(selectedTrack);

        RecordingFragment recordingFragment = (RecordingFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_recording);
        mapFragment.setRecordingViewModel(recordingFragment.getRecordingViewModel());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_map, menu);
        DrawableCompat.setTint(menu.findItem(R.id.action_edit).getIcon(), ContextCompat.getColor(this, R.color.primary_light));
        DrawableCompat.setTint(menu.findItem(R.id.action_list).getIcon(), ContextCompat.getColor(this, R.color.primary_light));

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setEnabled(selectedTrack.getTrackUri().get() != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed;
        if (item.getItemId() == R.id.action_edit) {
            showTrackTitleDialog();
            consumed = true;
        } else if (item.getItemId() == R.id.action_about) {
            showAboutDialog();
            consumed = true;
        } else if (item.getItemId() == R.id.action_list) {
            Intent intent = new Intent(this, TracksActivity.class);
            startActivityForResult(intent, REQUEST_CODE_TRACK_SELECTION);
            consumed = true;
        } else {
            consumed = super.onOptionsItemSelected(item);
        }

        return consumed;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_TRACK_URI, selectedTrack.getTrackUri().get());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TRACK_SELECTION && resultCode == RESULT_OK) {
            Uri trackUri = data.getParcelableExtra(ContentConstants.Tracks.TRACKS);
            selectedTrack.getTrackUri().set(trackUri);
        }
    }

    private void showAboutDialog() {
        new AboutFragment().show(getSupportFragmentManager(), "ABOUT");
    }

    private void showTrackTitleDialog() {
        final Uri trackUri = selectedTrack.getTrackUri().get();
        TrackEditDialogFragment.Companion.newInstance(trackUri).show(getSupportFragmentManager(), TAG_DIALOG);
    }
}
