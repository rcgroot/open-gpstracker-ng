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
package nl.sogeti.android.gpstracker.ng.map

import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentMapBinding

class TrackMapFragment : Fragment() {

    val KEY_TRACK_URI = "KEY_TRACK_URI"
    val trackViewModel: TrackViewModel = TrackViewModel(null)
    var recordingViewModel: RecordingViewModel? = null
        set(value) {
            field = value
            binding?.recorder = value
        }
    private val trackPresenter = TrackPresenter(trackViewModel)
    private var binding: FragmentMapBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            val uri = savedInstanceState.getParcelable<Uri>(KEY_TRACK_URI)
            trackViewModel.uri.set(uri)
        } else {
            trackViewModel.name.set(getString(R.string.app_name))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMapBinding>(inflater!!, R.layout.fragment_map, container, false)
        binding.fragmentMapMapview.onCreate(savedInstanceState)
        binding.viewModel = trackViewModel
        binding.recorder = recordingViewModel
        binding.fragmentMapMapview.getMapAsync(trackPresenter)
        this.binding = binding

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding?.fragmentMapMapview?.onResume()
        trackPresenter.start(activity)
    }

    override fun onPause() {
        super.onPause()
        binding?.fragmentMapMapview?.onPause()
        trackPresenter.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(KEY_TRACK_URI, trackViewModel.uri.get())
        binding?.fragmentMapMapview?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.fragmentMapMapview?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.fragmentMapMapview?.onLowMemory()
    }
}
