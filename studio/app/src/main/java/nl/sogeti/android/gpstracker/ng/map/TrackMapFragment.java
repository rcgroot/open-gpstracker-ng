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

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.FragmentMapBinding;

public class TrackMapFragment extends Fragment {

    private static final String KEY_TRACK_URI = "KEY_TRACK_URI";
    private TrackViewModel trackViewModel;
    private TrackPresenter trackPresenter;
    private FragmentMapBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            trackViewModel = new TrackViewModel(null, getString(R.string.app_name));
        } else {
            Uri uri = savedInstanceState.getParcelable(KEY_TRACK_URI);
            trackViewModel = new TrackViewModel(uri, getString(R.string.app_name));
        }
        trackPresenter = new TrackPresenter(trackViewModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);
        binding.fragmentMapMapview.onCreate(savedInstanceState);
        binding.setViewModel(trackViewModel);

        binding.fragmentMapMapview.getMapAsync(trackPresenter);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.fragmentMapMapview.onResume();
        trackPresenter.start(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.fragmentMapMapview.onPause();
        trackPresenter.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.fragmentMapMapview.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TRACK_URI, trackViewModel.uri.get());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.fragmentMapMapview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.fragmentMapMapview.onLowMemory();
    }

    public TrackViewModel getTrackViewModel() {
        return trackViewModel;
    }
}
