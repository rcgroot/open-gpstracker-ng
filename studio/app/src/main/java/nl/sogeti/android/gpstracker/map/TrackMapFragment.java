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

import android.content.DialogInterface;
import android.databinding.Observable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import nl.sogeti.android.gpstracker.map.rendering.TrackTileProvider;
import nl.sogeti.android.gpstracker.v2.R;

public class TrackMapFragment extends MapFragment implements OnMapReadyCallback, TrackTileProvider.Listener {

    private static final int ITEM_ID_EDIT_TRACK = 3;
    private static final String KEY_TRACK_URI = "KEY_TRACK_URI";
    private TrackViewModel trackViewModel;
    private TrackAdaptor trackAdaptor;
    private TileOverlay titleOverLay;
    private TrackTileProvider tileProvider;
    private Observable.OnPropertyChangedCallback uriCallback = new UriChangedCallback();
    private GoogleMap googleMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            trackViewModel = new TrackViewModel(null, getString(R.string.app_name));
        } else {
            Uri uri = savedInstanceState.getParcelable(KEY_TRACK_URI);
            trackViewModel = new TrackViewModel(uri, getString(R.string.app_name));
        }
        trackAdaptor = new TrackAdaptor(trackViewModel);
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        trackAdaptor.start(getActivity());
        trackViewModel.bounds.addOnPropertyChangedCallback(uriCallback);
    }

    @Override
    public void onPause() {
        trackViewModel.bounds.removeOnPropertyChangedCallback(uriCallback);
        trackAdaptor.stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        googleMap = null;
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, ITEM_ID_EDIT_TRACK, Menu.NONE, R.string.activity_track_map_edit);
        MenuItem menuItem = menu.findItem(ITEM_ID_EDIT_TRACK);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, getResources().getColor(R.color.primary_light));
        menuItem.setIcon(drawable);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(ITEM_ID_EDIT_TRACK).setEnabled(trackViewModel.uri.get()!=null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed;
        if (item.getItemId() == ITEM_ID_EDIT_TRACK) {
            showTrackTitleDialog();
            consumed = true;
        } else {
            consumed = super.onOptionsItemSelected(item);
        }

        return consumed;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        TileOverlayOptions options = new TileOverlayOptions();
        tileProvider = new TrackTileProvider(getActivity(), trackViewModel, this);
        options.tileProvider(tileProvider);
        options.fadeIn(true);
        titleOverLay = googleMap.addTileOverlay(options);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TRACK_URI, trackViewModel.uri.get());
    }

    @Override
    public void tilesDidBecomeOutdated(TrackTileProvider provider) {
        titleOverLay.clearTileCache();
    }

    public TrackViewModel getTrackViewModel() {
        return trackViewModel;
    }

    private void showTrackTitleDialog() {
        final Uri trackUri = trackViewModel.uri.get();
        // TODO make sure that nameField has proper margins
        final EditText nameField = new EditText(getActivity());
        nameField.setText(trackViewModel.name.get());
        nameField.setSelection(0, nameField.getText().length());
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.activity_track_map_rename_title))
                .setMessage(getString(R.string.activity_track_map_rename_message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userInput = nameField.getText().toString();
                        TrackAdaptor.updateName(getActivity(), trackUri, userInput);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(nameField)
                .show();
    }
    private class UriChangedCallback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            getActivity().invalidateOptionsMenu();
            if (googleMap != null) {
                LatLngBounds bounds = trackViewModel.bounds.get();
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, (int) getResources().getDimension(R.dimen.activity_horizontal_margin) * 2);
                googleMap.animateCamera(update);
            }
        }

    }
}
