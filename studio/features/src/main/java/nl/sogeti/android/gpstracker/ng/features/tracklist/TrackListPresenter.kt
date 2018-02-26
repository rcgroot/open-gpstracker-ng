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

import android.content.pm.PackageManager
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.gpxexport.ShareIntentFactory
import nl.sogeti.android.gpstracker.ng.features.gpximport.ImportService
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.KEY_META_FIELD_TRACK_TYPE
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_DEFAULT
import nl.sogeti.android.gpstracker.ng.features.util.AbstractPresenter
import nl.sogeti.android.gpstracker.service.integration.ContentConstants
import nl.sogeti.android.gpstracker.service.util.getLong
import nl.sogeti.android.gpstracker.service.util.map
import nl.sogeti.android.gpstracker.service.util.trackUri
import nl.sogeti.android.gpstracker.service.util.tracksUri
import nl.sogeti.android.opengpstrack.ng.features.R
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

const val OGT_EXPORTER_PACKAGE_NAME = "nl.renedegroot.android.opengpstracker.exporter"

class TrackListPresenter : AbstractPresenter(), ContentController.Listener, TrackListAdapterListener, TrackSelection.Listener {

    private var contentController: ContentController? = null

    @Inject
    lateinit var trackSelection: TrackSelection

    @Inject
    lateinit var contentControllerFactory: ContentControllerFactory

    @Inject
    lateinit var summaryManager: SummaryManager

    @Inject
    @field:Named("SystemBackgroundExecutor")
    lateinit var executor: Executor

    @Inject
    lateinit var shareIntentFactory: ShareIntentFactory

    @Inject
    lateinit var packageManager: PackageManager

    @Inject
    lateinit var notification: ImportNotification

    var navigation: TrackListNavigation? = null

    val viewModel: TrackListViewModel = TrackListViewModel()

    init {
        FeatureConfiguration.featureComponent.inject(this)

        trackSelection.addListener(this)
        contentController = contentControllerFactory.createContentController(this)
        contentController?.registerObserver(tracksUri())
    }

    override fun onStart() {
        super.onStart()
        summaryManager.start()
    }

    override fun onChange() {
        addTracksToModel()
    }

    override fun onStop() {
        summaryManager.stop()
        super.onStop()
    }

    override fun onCleared() {
        trackSelection.removeListener(this)
        contentController?.unregisterObserver()
        contentController = null
        notification.dismissCompletedImport()
        super.onCleared()
    }

    /* Content watching */

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        super.markDirty()
    }

    /* Content retrieval */

    private fun addTracksToModel() {
        executor.execute {
            val trackList = tracksUri().map {
                val id = it.getLong(ContentConstants.Tracks._ID)!!
                trackUri(id)
            }
            viewModel.selectedTrack.set(trackSelection.trackUri)
            viewModel.tracks.set(trackList.asReversed())
            trackSelection.trackUri?.let { scrollToTrack(it) }
        }
    }

    private fun scrollToTrack(trackUri: Uri) {
        val position = viewModel.tracks.get()?.indexOf(trackUri)
        position?.let {
            viewModel.focusPosition.set(position)
        }
    }

    //region View (adapter) callbacks

    override fun didSelectTrack(track: Uri, name: String) {
        trackSelection.selectTrack(track, name)
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

    //endregion

    //region Track selection listening

    override fun onTrackSelection(trackUri: Uri, trackName: String) {
        viewModel.selectedTrack.set(trackUri)
    }

    //endregion
}

