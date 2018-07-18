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
package nl.sogeti.android.gpstracker.ng.features.map

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.databinding.FeaturesBindingComponent
import nl.sogeti.android.gpstracker.service.util.PermissionRequester
import nl.sogeti.android.opengpstrack.ng.features.R
import nl.sogeti.android.opengpstrack.ng.features.databinding.FragmentMapBinding
import javax.inject.Inject

class TrackMapFragment : Fragment() {

    @Inject
    lateinit var permissionRequester : PermissionRequester
    private lateinit var presenter: TrackMapPresenter
    private var binding: FragmentMapBinding? = null

    private val optionMenuObserver: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            activity?.invalidateOptionsMenu()
        }
    }

    private val wakelockObservable: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val wakelock = presenter.viewModel.isLocked.get()
            if (wakelock) {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter = ViewModelProviders.of(this).get(TrackMapPresenter::class.java)
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMapBinding>(inflater, R.layout.fragment_map, container, false, FeaturesBindingComponent())
        binding.fragmentMapMapview.onCreate(savedInstanceState)
        binding.viewModel = presenter.viewModel
        binding.presenter = presenter
        presenter.viewModel.showSatellite.addOnPropertyChangedCallback(optionMenuObserver)
        presenter.viewModel.willLock.addOnPropertyChangedCallback(optionMenuObserver)
        presenter.viewModel.isLocked.addOnPropertyChangedCallback(wakelockObservable)
        this.binding = binding

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val mapView = getMapView()
        mapView.onStart()
        permissionRequester.start(this) { presenter.start(mapView) }
    }

    override fun onResume() {
        super.onResume()
        getMapView().onResume()
    }

    override fun onPause() {
        getMapView().onPause()
        super.onPause()

    }

    override fun onStop() {
        presenter.stop()
        permissionRequester.stop()
        getMapView().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        getMapView().onDestroy()
        binding = null
        presenter.viewModel.showSatellite.removeOnPropertyChangedCallback(optionMenuObserver)
        presenter.viewModel.willLock.removeOnPropertyChangedCallback(optionMenuObserver)
        presenter.viewModel.isLocked.removeOnPropertyChangedCallback(wakelockObservable)
        super.onDestroyView()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getMapView().onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.action_satellite).isChecked = presenter.viewModel.showSatellite.get()
        menu.findItem(R.id.action_lock).isChecked = presenter.viewModel.willLock.get()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.action_satellite -> {
                presenter.onSatelliteSelected()
                true
            }
            item.itemId == R.id.action_lock -> {
                presenter.onScreenLockSelected()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.fragmentMapMapview?.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionRequester.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    private fun getMapView() = binding!!.fragmentMapMapview
}
