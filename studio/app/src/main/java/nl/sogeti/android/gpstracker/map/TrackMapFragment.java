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

import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import nl.sogeti.android.gpstracker.map.rendering.TrackTileProvider;
import nl.sogeti.android.gpstracker.v2.R;

public class TrackMapFragment extends MapFragment implements OnMapReadyCallback, TrackTileProvider.Listener {

    private static final String KEY_TRACK_URI = "KEY_TRACK_URI";
    private TrackViewModel track;
    private TrackAdaptor trackAdaptor;
    private TileOverlay titleOverLay;
    private TrackTileProvider tileProvider;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            track = new TrackViewModel(null, getString(R.string.app_name));
        } else {
            Uri uri = savedInstanceState.getParcelable(KEY_TRACK_URI);
            track = new TrackViewModel(uri, getString(R.string.app_name));
        }
        trackAdaptor = new TrackAdaptor(track);
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        trackAdaptor.start(getActivity());
    }

    @Override
    public void onDestroy() {
        trackAdaptor.stop();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        TileOverlayOptions options = new TileOverlayOptions();
        tileProvider = new TrackTileProvider(getActivity(), track, this);
        options.tileProvider(tileProvider);
        options.fadeIn(true);
        titleOverLay = googleMap.addTileOverlay(options);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TRACK_URI, track.uri.get());
    }

    @Override
    public void tilesDidBecomeOutdated(TrackTileProvider provider) {
        titleOverLay.clearTileCache();
    }

    public TrackViewModel getTrack() {
        return track;
    }
}
