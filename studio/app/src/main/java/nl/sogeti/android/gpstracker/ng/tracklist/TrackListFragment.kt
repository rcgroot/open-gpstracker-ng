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
package nl.sogeti.android.gpstracker.ng.tracklist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import nl.sogeti.android.gpstracker.ng.track.TrackNavigator
import nl.sogeti.android.gpstracker.ng.utils.PermissionRequester
import nl.sogeti.android.gpstracker.ng.utils.executeOnUiThread
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentTracklistBinding

/**
 * Sets up display and selection of tracks in a list style
 */
class TrackListFragment : Fragment(), TrackListViewModel.View {

    private val viewModel = TrackListViewModel()
    private val trackListPresenter = TrackListPresenter(viewModel, this)
    private var permissionRequester = PermissionRequester()
    private var binding: FragmentTracklistBinding? = null

    companion object {
        fun newInstance(): TrackListFragment {
            return TrackListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentTracklistBinding>(inflater, R.layout.fragment_tracklist, container, false)
        binding.fragmentTracklistList.layoutManager = LinearLayoutManager(activity)
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        binding.fragmentTracklistList.itemAnimator = itemAnimator
        binding.viewModel = viewModel
        binding.presenter = trackListPresenter
        this.binding = binding

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        permissionRequester.start(this, { trackListPresenter.start(activity, TrackNavigator(activity)) })
    }

    override fun onStop() {
        super.onStop()
        trackListPresenter.stop()
        permissionRequester.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_import_export, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val isHandled: Boolean
        if (item.itemId == R.id.menu_item_export) {
            trackListPresenter.didSelectExport()
            isHandled = true
        } else if (item.itemId == R.id.menu_item_import) {
            trackListPresenter.didSelectImport()
            isHandled = true
        } else {
            isHandled = super.onOptionsItemSelected(item)
        }
        return isHandled
    }

    //region View contract

    override fun moveToPosition(postion: Int) {
        executeOnUiThread { binding?.fragmentTracklistList?.layoutManager?.scrollToPosition(postion) }
    }

    //endregion

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionRequester.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
}
