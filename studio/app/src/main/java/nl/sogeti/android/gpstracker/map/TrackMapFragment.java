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

import android.databinding.Observable;
import android.net.Uri;
import android.os.Bundle;

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

    private static final String KEY_TRACK_URI = "KEY_TRACK_URI";
    private TrackViewModel viewModel;
    private TrackAdaptor trackAdaptor;
    private TileOverlay titleOverLay;
    private TrackTileProvider tileProvider;
    private Observable.OnPropertyChangedCallback uriCallback = new UriChangedCallback();
    private GoogleMap googleMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            viewModel = new TrackViewModel(null, getString(R.string.app_name));
        } else {
            Uri uri = savedInstanceState.getParcelable(KEY_TRACK_URI);
            viewModel = new TrackViewModel(uri, getString(R.string.app_name));
        }
        trackAdaptor = new TrackAdaptor(viewModel);
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        trackAdaptor.start(getActivity());
        viewModel.bounds.addOnPropertyChangedCallback(uriCallback);
    }

    @Override
    public void onPause() {
        viewModel.bounds.removeOnPropertyChangedCallback(uriCallback);
        trackAdaptor.stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        googleMap = null;
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        TileOverlayOptions options = new TileOverlayOptions();
        tileProvider = new TrackTileProvider(getActivity(), viewModel, this);
        options.tileProvider(tileProvider);
        options.fadeIn(true);
        titleOverLay = googleMap.addTileOverlay(options);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TRACK_URI, viewModel.uri.get());
    }

    @Override
    public void tilesDidBecomeOutdated(TrackTileProvider provider) {
        titleOverLay.clearTileCache();
    }

    public TrackViewModel getViewModel() {
        return viewModel;
    }

    private class UriChangedCallback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (googleMap != null) {
                LatLngBounds bounds = viewModel.bounds.get();
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, (int) getResources().getDimension(R.dimen.activity_horizontal_margin) * 2);
                googleMap.animateCamera(update);
            }
        }
    }
}
