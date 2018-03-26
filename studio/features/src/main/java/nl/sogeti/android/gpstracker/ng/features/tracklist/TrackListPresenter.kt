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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.gpxexport.ShareIntentFactory
import nl.sogeti.android.gpstracker.ng.features.gpximport.ImportService
import nl.sogeti.android.gpstracker.ng.features.model.TrackSearch
import nl.sogeti.android.gpstracker.ng.features.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.KEY_META_FIELD_TRACK_TYPE
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_DEFAULT
import nl.sogeti.android.gpstracker.ng.features.util.AbstractPresenter
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Tracks._ID
import nl.sogeti.android.gpstracker.service.util.trackUri
import nl.sogeti.android.gpstracker.service.util.tracksUri
import nl.sogeti.android.gpstracker.utils.contentprovider.getLong
import nl.sogeti.android.gpstracker.utils.contentprovider.map
import nl.sogeti.android.opengpstrack.ng.features.R
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

const val OGT_EXPORTER_PACKAGE_NAME = "nl.renedegroot.android.opengpstracker.exporter"

class TrackListPresenter @Inject constructor(
        val contentController: ContentController,
        private val trackSelection: TrackSelection,
        private val trackSearch: TrackSearch,
        val summaryManager: SummaryManager,
        @Named("SystemBackgroundExecutor") val executor: Executor,
        private val shareIntentFactory: ShareIntentFactory,
        private val packageManager: PackageManager,
        private val notification: ImportNotification)
    : AbstractPresenter(), ContentController.Listener, TrackListAdapterListener {

    var navigation: TrackListNavigation? = null

    val viewModel: TrackListViewModel = TrackListViewModel()

    private val selectionObserver = Observer<Uri> { trackUri -> onTrackSelected(trackUri) }
    private val searchQueryObserver = Observer<String> { _ -> onSearchQuery() }

    private val selection: Pair<String, List<String>>?
        get() {
            val argument = trackSearch.query.value.asArgument()
            return if (argument.isBlank()) {
                null
            } else {
                Pair("name LIKE ?", listOf(argument))
            }
        }

    init {
        trackSelection.selection.observeForever(selectionObserver)
        trackSearch.query.observeForever(searchQueryObserver)
        contentController.registerObserver(this, tracksUri())
    }

    override fun onStart() {
        super.onStart()
        summaryManager.start()
    }

    override fun onChange() {
        executor.execute {
            val trackList = tracksUri().map(BaseConfiguration.appComponent.contentResolver(), selection, listOf(_ID)) {
                val id = it.getLong(_ID)!!
                trackUri(id)
            }
            viewModel.selectedTrack.set(trackSelection.selection.value)
            viewModel.tracks.set(trackList.asReversed())
            trackSelection.selection.value?.let { scrollToTrack(it) }
        }
    }

    override fun onStop() {
        summaryManager.stop()
        super.onStop()
    }

    public override fun onCleared() {
        trackSelection.selection.removeObserver(selectionObserver)
        trackSearch.query.removeObserver(searchQueryObserver)
        contentController.unregisterObserver()
        notification.dismissCompletedImport()
        super.onCleared()
    }

    /* Content watching */

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        markDirty()
    }

    /* Content retrieval */

    private fun scrollToTrack(trackUri: Uri) {
        val position = viewModel.tracks.get()?.indexOf(trackUri)
        position?.let {
            viewModel.focusPosition.set(position)
        }
    }

    //region View (adapter) callbacks

    override fun didSelectTrack(track: Uri, name: String) {
        trackSelection.selection.value = track
        navigation?.finishTrackSelection()
    }

    override fun didEditTrack(track: Uri) {
        navigation?.showTrackEditDialog(track)
    }

    override fun didDeleteTrack(track: Uri) {
        navigation?.showTrackDeleteDialog(track)
    }

    override fun didSelectExportTrack(track: Uri) {
        val shareIntent = shareIntentFactory.createShareIntent(track)
        navigation?.showIntentChooser(shareIntent, R.string.track_share)
    }

    override fun didSelectExportToDirectory() {
        val intent = packageManager.getLaunchIntentForPackage(OGT_EXPORTER_PACKAGE_NAME)
        if (intent == null) {
            navigation?.showInstallHintForOGTExporterApp()
        } else {
            navigation?.openExternalOGTExporterApp()
        }
    }

    override fun didSelectImportTrack() {
        navigation?.startGpxFileSelection({ intent ->
            val uri = intent?.data
            uri?.let {
                val trackType = intent.getStringExtra(KEY_META_FIELD_TRACK_TYPE)
                        ?: VALUE_TYPE_DEFAULT
                ImportService.importFile(uri, trackType)
            }
        })
    }

    override fun didSelectImportFromDirectory() {
        navigation?.startGpxDirectorySelection { intent ->
            val uri = intent?.data
            uri?.let {
                val trackType = intent.getStringExtra(KEY_META_FIELD_TRACK_TYPE)
                        ?: VALUE_TYPE_DEFAULT
                ImportService.importDirectory(uri, trackType)
            }
        }
    }

    internal fun onSearchClosed() {
        trackSearch.query.value = null
    }

    //endregion

    //region Track selection listening

    private fun onTrackSelected(trackUri: Uri?) {
        viewModel.selectedTrack.set(trackUri)
    }

    private fun onSearchQuery() {
        markDirty()
    }

    //endregion


    companion object {

        @Suppress("UNCHECKED_CAST")
        fun newFactory() =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        val presenter = FeatureConfiguration.featureComponent.trackListPresenter()
                        return presenter as T
                    }
                }

    }

    private fun String?.asArgument(): String =
            if (this == null) {
                ""
            } else {
                val filtered = this.filter { it != '%' }
                if (filtered.isNotBlank()) {
                    "%$filtered%"
                } else {
                    ""
                }

            }

}

