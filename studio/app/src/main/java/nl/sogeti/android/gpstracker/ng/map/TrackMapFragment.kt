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
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.utils.PermissionRequester
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentMapBinding
import javax.inject.Inject

class TrackMapFragment : Fragment() {

    private val viewModel = TrackMapViewModel()
    private val trackPresenter = TrackMapPresenter(viewModel)
    private val permissionRequester = PermissionRequester()
    private var binding: FragmentMapBinding? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMapBinding>(inflater, R.layout.fragment_map, container, false)
        binding.fragmentMapMapview.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        binding.presenter = trackPresenter
        this.binding = binding

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding!!.fragmentMapMapview.onStart()
        permissionRequester.start(this, {
            trackPresenter.start(activity)
            binding!!.fragmentMapMapview.getMapAsync(trackPresenter)
        })
    }

    override fun onResume() {
        super.onResume()
        binding!!.fragmentMapMapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding!!.fragmentMapMapview.onPause()

    }

    override fun onStop() {
        super.onStop()
        trackPresenter.stop()
        permissionRequester.stop()
        binding!!.fragmentMapMapview.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        binding!!.fragmentMapMapview.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding!!.fragmentMapMapview.onDestroy()
        binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.fragmentMapMapview?.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionRequester.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
}
