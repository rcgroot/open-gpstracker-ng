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

import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.sharing.ShareIntentFactory
import nl.sogeti.android.gpstracker.ng.track.TrackNavigator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.utils.getLong
import nl.sogeti.android.gpstracker.ng.utils.map
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import nl.sogeti.android.gpstracker.ng.utils.tracksUri
import nl.sogeti.android.gpstracker.v2.R
import java.util.concurrent.Executor
import javax.inject.Inject


class TrackListPresenter(val viewModel: TrackListViewModel, val view: TrackListViewModel.View) : ContextedPresenter<TrackNavigator>(), ContentController.Listener, TrackListAdapterListener, TrackSelection.Listener {

    private var contentController: ContentController? = null
    @Inject
    lateinit var trackSelection: TrackSelection
    @Inject
    lateinit var contentControllerFactory: ContentControllerFactory
    @Inject
    lateinit var summaryManager: SummaryManager
    @Inject
    lateinit var executor: Executor
    @Inject
    lateinit var shareIntentFactory: ShareIntentFactory

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    override fun didStart() {
        trackSelection.addListener(this)
        contentController = contentControllerFactory.createContentController(context, this)
        contentController?.registerObserver(tracksUri())
        summaryManager.start()
        addTracksToModel()
    }

    override fun willStop() {
        trackSelection.removeListener(this)
        contentController?.unregisterObserver()
        contentController = null
        summaryManager.stop()
    }

    /* Content watching */

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        addTracksToModel()
    }

    /* Content retrieval */

    private fun addTracksToModel() {
        executor.execute {
            val trackList = tracksUri().map(context, {
                val id = it.getLong(ContentConstants.Tracks._ID)!!
                trackUri(id)
            })
            viewModel.selectedTrack.set(trackSelection.trackUri)
            viewModel.tracks.set(trackList.asReversed())
            trackSelection.trackUri?.let { scrollToTrack(it) }
        }
    }

    private fun scrollToTrack(trackUri: Uri) {
        val postion = viewModel.tracks.get().indexOf(trackUri)
        view.moveToPosition(postion)
    }

    //region View (adapter) callbacks

    override fun didSelectTrack(track: Uri, name: String) {
        trackSelection.selectTrack(track, name)
        navigation.hideTrackList()
    }

    override fun didShareTrack(track: Uri) {
        val shareIntent = shareIntentFactory.createShareIntent(track)
        navigation.showIntentChooser(shareIntent, context.getText(R.string.track_share))
    }

    override fun didEditTrack(track: Uri) {
        navigation.showTrackEditDialog(track)
    }

    override fun didDeleteTrack(track: Uri) {
        navigation.showTrackDeleteDialog(track)
    }

    override fun didSelectExport() {
        navigation.startFullExport()
    }

    override fun didSelectImport() {
        navigation.startGpxFileSelection()
    }

    //endregion

    //region Track selection listening

    override fun onTrackSelection(trackUri: Uri, name: String) {
        viewModel.selectedTrack.set(trackUri)
    }

    //endregion
}

