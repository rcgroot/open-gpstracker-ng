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
package nl.sogeti.android.gpstracker.map;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.ActivityTrackMapBinding;


public class TrackMapActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TRACK_URI = "KEY_SELECTED_TRACK_URI";
    private ActivityTrackMapBinding binding;
    private TrackViewModel selectedTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track_map);
        TrackMapFragment mapFragment = (TrackMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map);
        selectedTrack = mapFragment.getTrack();
        binding.setTrack(selectedTrack);
        setSupportActionBar(binding.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, 1, Menu.NONE, "Q/D track select");
        menu.add(Menu.NONE, 2, Menu.NONE, "Last track");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == 1) {
            showTrackInput();
        } else if (id == 2) {
            Cursor tracks = null;
            try {
                tracks = getContentResolver().query(ContentConstants.Tracks.CONTENT_URI, new String[]{ContentConstants.Tracks._ID}, null, null, null);
                if (tracks != null && tracks.moveToLast()) {
                    long trackId = tracks.getLong(0);
                    selectedTrack.uri.set(ContentUris.withAppendedId(ContentConstants.Tracks.CONTENT_URI, trackId));
                }
            } finally {
                if (tracks != null) {
                    tracks.close();
                }
            }

        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_TRACK_URI, selectedTrack.uri.get());
    }

    private void showTrackInput() {
        final EditText uriField = new EditText(this);
        uriField.setText(R.string.activity_track_map_hint_uri);
        new AlertDialog.Builder(this)
                .setTitle("Track select")
                .setMessage("Enter track Uri")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTrack.uri.set(Uri.parse(uriField.getText().toString()));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(uriField)
                .show();
    }
}
