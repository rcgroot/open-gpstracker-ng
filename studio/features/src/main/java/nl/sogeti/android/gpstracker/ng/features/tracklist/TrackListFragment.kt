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
package nl.sogeti.android.gpstracker.ng.features.tracklist

import android.app.SearchManager
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.database.CursorWrapper
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.view.*
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.service.util.PermissionRequester
import nl.sogeti.android.gpstracker.utils.activityresult.ActivityResultLambdaFragment
import nl.sogeti.android.opengpstrack.ng.features.R
import nl.sogeti.android.opengpstrack.ng.features.databinding.FragmentTracklistBinding
import javax.inject.Inject


/**
 * Sets up display and selection of tracks in a list style
 */
class TrackListFragment : ActivityResultLambdaFragment() {

    lateinit var presenter: TrackListPresenter
    @Inject
    lateinit var permissionRequester: PermissionRequester
    private var binding: FragmentTracklistBinding? = null

    companion object {
        fun newInstance(): TrackListFragment {
            return TrackListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ViewModelProviders.of(this).get(TrackListPresenter::class.java)
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentTracklistBinding>(inflater, R.layout.fragment_tracklist, container, false)
        binding.fragmentTracklistList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        val itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        binding.fragmentTracklistList.itemAnimator = itemAnimator
        binding.presenter = presenter
        binding.viewModel = presenter.viewModel
        presenter.navigation = TrackListNavigation(this)
        this.binding = binding

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        permissionRequester.start(this) { presenter.start() }
        setHasOptionsMenu(true)
    }

    override fun onStop() {
        super.onStop()
        setHasOptionsMenu(false)
        presenter.stop()
        permissionRequester.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        presenter.navigation = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search, menu)
        inflater.inflate(R.menu.import_export, menu)

        attachSearch(menu.findItem(R.id.action_search))
        presenter.onSearchClosed()
    }

    private fun attachSearch(searchItem: MenuItem) {
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = searchItem.actionView as SearchView?
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView?.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                setQueryToSuggestion(position, false)
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                setQueryToSuggestion(position, true)
                return false
            }

            private fun setQueryToSuggestion(position: Int, submit: Boolean) {
                val suggestionsAdapter: androidx.cursoradapter.widget.CursorAdapter? = searchView.suggestionsAdapter
                val item = suggestionsAdapter?.getItem(position) as CursorWrapper?
                val selected = item?.getString(1)
                searchView.setQuery(selected, submit)
            }
        })
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                presenter.onSearchClosed()
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when {
                item.itemId == R.id.menu_item_export -> {
                    presenter.didSelectExportToDirectory()
                    true
                }
                item.itemId == R.id.menu_item_import -> {
                    presenter.didSelectImportFromDirectory()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionRequester.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

}
