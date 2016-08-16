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
package nl.sogeti.android.gpstracker.ng.map;

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

import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProvider;
import nl.sogeti.android.gpstracker.v2.R;

public class TrackMapFragment extends MapFragment implements OnMapReadyCallback, TrackTileProvider.Listener {

    private static final String KEY_TRACK_URI = "KEY_TRACK_URI";
    private TrackViewModel trackViewModel;
    private TrackPresenter trackPresenter;
    private TileOverlay titleOverLay;
    private final Observable.OnPropertyChangedCallback uriCallback = new UriChangedCallback();
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
        trackPresenter = new TrackPresenter(trackViewModel);
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        trackPresenter.start(getActivity());
        trackViewModel.trackHeadBounds.addOnPropertyChangedCallback(uriCallback);
        trackViewModel.startStopBounds.addOnPropertyChangedCallback(uriCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        trackViewModel.trackHeadBounds.removeOnPropertyChangedCallback(uriCallback);
        trackViewModel.startStopBounds.removeOnPropertyChangedCallback(uriCallback);
        trackPresenter.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleMap = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        TileOverlayOptions options = new TileOverlayOptions();
        TrackTileProvider tileProvider = new TrackTileProvider(getActivity(), trackViewModel, this);
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

    private class UriChangedCallback extends Observable.OnPropertyChangedCallback implements GoogleMap.CancelableCallback {
        private boolean isAnimating = false;

        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            getActivity().invalidateOptionsMenu();
            if (googleMap != null) {
                LatLngBounds bounds = null;
                LatLngBounds visible = googleMap.getProjection().getVisibleRegion().latLngBounds;
                LatLngBounds head = trackViewModel.trackHeadBounds.get();
                if (googleMap.getCameraPosition().zoom == googleMap.getMinZoomLevel()) {
                    bounds = trackViewModel.startStopBounds.get();
                } else if (head != null && (!visible.contains(head.northeast) || !visible.contains(head.southwest))) {
                    bounds = head;
                }
                if (bounds != null && !isAnimating) {
                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, (int) getResources().getDimension(R.dimen.activity_horizontal_margin) * 8);
                    isAnimating = true;
                    googleMap.animateCamera(update, this);
                }
            }
        }

        @Override
        public void onFinish() {
            isAnimating = false;
        }

        @Override
        public void onCancel() {
            isAnimating = false;
        }
    }
}
