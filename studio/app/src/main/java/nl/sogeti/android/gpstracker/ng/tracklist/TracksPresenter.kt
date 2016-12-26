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

import android.databinding.ObservableField
import android.net.Uri
import android.os.AsyncTask
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.ContentController
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackPolylineProvider
import nl.sogeti.android.gpstracker.ng.tracklist.summary.summaryManager
import nl.sogeti.android.gpstracker.ng.utils.*
import timber.log.Timber

class TracksPresenter(val model: TracksViewModel) : ContextedPresenter(), ContentController.ContentListener, TrackListListener {
    var listener: Listener? = null
    private var contentController: ContentController? = null

    override fun didStart() {
        contentController = ContentController(context!!, ObservableField(tracksUri()), this)
        summaryManager.start()
        addTracksToModel()
    }

    override fun willStop() {
        contentController?.destroy()
        summaryManager.stop()
    }

    /* Content watching */

    override fun onChangeUriField(uri: Uri) {
        addTracksToModel()
    }

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        addTracksToModel()
    }

    /* Content retrieval */

    private fun addTracksToModel() {
        val context = this.context
        if (context != null) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute {
                val trackList = tracksUri().map(context, {
                    val id = it.getLong(ContentConstants.Tracks._ID)!!
                    val uri = trackUri(id)
                    val name = it.getString(ContentConstants.Tracks.NAME) ?: ""
                    TrackViewModel(uri, name)
                })
                model.tracks.clear()
                model.tracks.addAll(trackList.asReversed())
            }
        } else {
            Timber.w("Unexpected tracks update when context is gone")
        }
    }

    /* Adapter callbacks */

    override fun willDisplayTrack(track: TrackViewModel, completion: () -> Unit) {
        context?.let {
            summaryManager.collectSummaryInfo(it, track.uri.get(), {
                if (it.track == track.uri.get()) {
                    track.name.set(it.name)
                    track.distance.set(it.distance)
                    track.duration.set(it.duration)
                    track.iconType.set(it.type)
                    track.startDay.set(it.start)
                    track.completeBounds.set(it.bounds)
                    track.waypoints.set(it.waypoints)
                    val trackPolylineProvider = TrackPolylineProvider(track.waypoints.get())
                    trackPolylineProvider.drawPolylines()
                    track.polylines = trackPolylineProvider.lineOptions
                    executeOnUiThread(completion)
                }
            })
        }
    }

    override fun didSelectTrack(track: TrackViewModel) {
        listener?.onTrackSelected(track.uri.get())
    }

    interface Listener {
        fun onTrackSelected(uri: Uri)
    }
}

