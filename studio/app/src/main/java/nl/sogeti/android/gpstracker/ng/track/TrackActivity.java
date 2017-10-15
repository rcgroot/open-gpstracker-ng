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
package nl.sogeti.android.gpstracker.ng.track;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.ActivityTrackMapBinding;

public class TrackActivity extends AppCompatActivity implements TrackViewModel.View {

    private static final String KEY_SELECTED_TRACK_URI = "KEY_SELECTED_TRACK_URI";
    private static final String KEY_SELECTED_TRACK_NAME = "KEY_SELECTED_TRACK_NAME";
    private static final String ARG_SHOW_TRACKS = "ARG_SHOW_TRACKS";

    private final TrackViewModel viewModel = new TrackViewModel();
    private final TrackPresenter presenter = new TrackPresenter(viewModel, this);
    private boolean startWithOpenTracks;

    @NotNull
    public static Intent newIntent(Context context, boolean showTracks) {
        Intent intent = new Intent(context, TrackActivity.class);
        intent.putExtra(ARG_SHOW_TRACKS, showTracks);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTrackMapBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_track_map);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.bringToFront();
        binding.setViewModel(viewModel);
        if (savedInstanceState == null) {
            startWithOpenTracks = getIntent().getBooleanExtra(ARG_SHOW_TRACKS, false);
        } else {
            startWithOpenTracks = false;
            Uri uri = savedInstanceState.getParcelable(KEY_SELECTED_TRACK_URI);
            String name = savedInstanceState.getString(KEY_SELECTED_TRACK_NAME);
            viewModel.getTrackUri().set(uri);
            viewModel.getName().set(name);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        TrackNavigator navigation = new TrackNavigator(this);
        presenter.start(this, navigation);
        if (startWithOpenTracks) {
            navigation.showTrackSelection();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_TRACK_URI, viewModel.getTrackUri().get());
        outState.putString(KEY_SELECTED_TRACK_NAME, viewModel.getName().get());
    }

    //region Context menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_map, menu);
        DrawableCompat.setTint(menu.findItem(R.id.action_edit).getIcon(), ContextCompat.getColor(this, R.color.primary_light));
        DrawableCompat.setTint(menu.findItem(R.id.action_list).getIcon(), ContextCompat.getColor(this, R.color.primary_light));
        DrawableCompat.setTint(menu.findItem(R.id.action_graphs).getIcon(), ContextCompat.getColor(this, R.color.primary_light));

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setEnabled(viewModel.isEditable());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed;
        if (item.getItemId() == R.id.action_edit) {
            presenter.onEditOptionSelected();
            consumed = true;
        } else if (item.getItemId() == R.id.action_about) {
            presenter.onAboutOptionSelected();
            consumed = true;
        } else if (item.getItemId() == R.id.action_list) {
            presenter.onListOptionSelected();
            consumed = true;
        } else if (item.getItemId() == R.id.action_graphs) {
            presenter.onGraphsOptionSelected();
            consumed = true;
        } else {
            consumed = super.onOptionsItemSelected(item);
        }

        return consumed;
    }

    //endregion

    //region View contract

    @Override
    public void showTrackName(@NotNull String name) {
        invalidateOptionsMenu();
    }

    //endregion
}
