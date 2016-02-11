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

public class TrackMapFragment extends MapFragment implements OnMapReadyCallback, TrackTileProvider.Listener {

    private static final String KEY_TRACK_URI = "KEY_TRACK_URI";
    private TrackViewModel track;
    private TileOverlay titleOverLay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            track = new TrackViewModel(getActivity(), null, "");
        } else {
            Uri uri = savedInstanceState.getParcelable(KEY_TRACK_URI);
            track = new TrackViewModel(getActivity(), uri, "");
        }
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        TileOverlayOptions options = new TileOverlayOptions();
        options.tileProvider(new TrackTileProvider(getActivity(), track, this));
        options.fadeIn(true);
        titleOverLay = googleMap.addTileOverlay(options);
    }

    public void setTrack(TrackViewModel track) {
        this.track = track;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TRACK_URI, track.uri);
    }

    @Override
    public void tilesDidBecomeOutdated(TrackTileProvider provider) {
        titleOverLay.clearTileCache();
    }
}
