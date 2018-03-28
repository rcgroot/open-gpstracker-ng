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
package nl.sogeti.android.gpstracker.ng.features.track

import android.app.SearchManager
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.databinding.FeaturesBindingComponent
import nl.sogeti.android.gpstracker.ng.features.model.TrackSearch
import nl.sogeti.android.opengpstrack.ng.features.R
import nl.sogeti.android.opengpstrack.ng.features.databinding.ActivityTrackMapBinding
import javax.inject.Inject

class TrackActivity : AppCompatActivity() {

    private val navigation = TrackNavigator(this)

    private lateinit var presenter: TrackPresenter

    private var startWithOpenTracks: Boolean = false

    private val optionMenuObserver: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            invalidateOptionsMenu()
        }
    }

    @Inject
    lateinit var trackSearch: TrackSearch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FeatureConfiguration.featureComponent.inject(this)
        val binding = DataBindingUtil.setContentView<ActivityTrackMapBinding>(this, R.layout.activity_track_map, FeaturesBindingComponent())
        setSupportActionBar(binding.toolbar)
        binding.toolbar.bringToFront()
        presenter = ViewModelProviders.of(this, TrackPresenter.newFactory()).get(TrackPresenter::class.java)
        presenter.navigation = navigation
        presenter.viewModel.name.addOnPropertyChangedCallback(optionMenuObserver)
        binding.viewModel = presenter.viewModel

        if (savedInstanceState == null) {
            startWithOpenTracks = intent.getBooleanExtra(ARG_SHOW_TRACKS, false)
        } else {
            startWithOpenTracks = false
            val uri = savedInstanceState.getParcelable<Uri>(KEY_SELECTED_TRACK_URI)
            val name = savedInstanceState.getString(KEY_SELECTED_TRACK_NAME)
            presenter.viewModel.trackUri.set(uri)
            presenter.viewModel.name.set(name)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
        if (startWithOpenTracks) {
            navigation.showTrackSelection()
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
    }

    override fun onDestroy() {
        presenter.navigation = null
        presenter.viewModel.name.removeOnPropertyChangedCallback(optionMenuObserver)

        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_SEARCH) {
            trackSearch.query.value = intent.getStringExtra(SearchManager.QUERY)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_SELECTED_TRACK_URI, presenter.viewModel.trackUri.get())
        outState.putString(KEY_SELECTED_TRACK_NAME, presenter.viewModel.name.get())
    }

    //region Context menu

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.track, menu)
        DrawableCompat.setTint(menu.findItem(R.id.action_edit).icon, ContextCompat.getColor(this, R.color.primary_text))
        DrawableCompat.setTint(menu.findItem(R.id.action_list).icon, ContextCompat.getColor(this, R.color.primary_text))
        DrawableCompat.setTint(menu.findItem(R.id.action_graphs).icon, ContextCompat.getColor(this, R.color.primary_text))

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_edit).isEnabled = presenter.viewModel.isEditable

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.action_edit -> {
                presenter.onEditOptionSelected()
                true
            }
            item.itemId == R.id.action_about -> {
                presenter.onAboutOptionSelected()
                true
            }
            item.itemId == R.id.action_list -> {
                presenter.onListOptionSelected()
                true
            }
            item.itemId == R.id.action_graphs -> {
                presenter.onGraphsOptionSelected()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val KEY_SELECTED_TRACK_URI = "KEY_SELECTED_TRACK_URI"
        private const val KEY_SELECTED_TRACK_NAME = "KEY_SELECTED_TRACK_NAME"
        private const val ARG_SHOW_TRACKS = "ARG_SHOW_TRACKS"

        fun newIntent(context: Context, showTracks: Boolean): Intent {
            val intent = Intent(context, TrackActivity::class.java)
            intent.putExtra(ARG_SHOW_TRACKS, showTracks)

            return intent
        }
    }

    //endregion
}
