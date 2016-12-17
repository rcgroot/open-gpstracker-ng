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
import android.databinding.ObservableField
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.ContentController
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackPolylineProvider
import nl.sogeti.android.gpstracker.ng.tracklist.summary.summaryManager
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding

class TracksPresenter(val model: TracksViewModel) : ContextedPresenter(), ContentController.ContentListener {

    var listener: Listener? = null
    val viewAdapter = ViewAdapter()
    private var contentController: ContentController? = null

    override fun didStart() {
        contentController = ContentController(context!!, ObservableField(tracksUri()), this)
        summaryManager.start()
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            addTracksToModel()
        }
    }

    override fun willStop() {
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
        val trackList = tracksUri().map(context!!, {
            val id = it.getLong(ContentConstants.Tracks._ID)!!
            val uri = trackUri(id)
            val name = it.getString(ContentConstants.Tracks.NAME) ?: ""
            TrackViewModel(uri, name)
        })
        model.track.clear()
        model.track.addAll(trackList)
    }

    fun onTrackClick(viewModel: TrackViewModel) {
        listener?.onTrackSelected(viewModel.uri.get())
    }

    interface Listener {
        fun onTrackSelected(uri: Uri)
    }

    inner class ViewAdapter : RecyclerView.Adapter<TracksPresenter.ViewHolder>() {

        override fun getItemCount(): Int {
            return model.track.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(parent?.context), R.layout.row_track, parent, false)
            val holder = ViewHolder(binding)
            // Weirdly enough the 'clickable="false"' in the XML resource doesn't work
            holder.binding.rowTrackMap.isClickable = false
            holder.binding.rowTrackMap.onCreate(null)
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val trackViewModel = model.track[position]
            if (holder != null) {
                holder.binding.presenter = this@TracksPresenter
                holder.binding.viewModel = trackViewModel
                if (holder.googleMap == null) {
                    holder.binding.rowTrackMap.getMapAsync {
                        it.uiSettings.isMapToolbarEnabled = false
                        holder.googleMap = it
                        holder.addLinesToMap()
                    }
                } else {
                    holder.addLinesToMap()
                }
                context?.let {
                    summaryManager.collectSummaryInfo(it, trackViewModel.uri.get(), {
                        if (it.track == trackViewModel.uri.get()) {
                            trackViewModel.name.set(it.name)
                            trackViewModel.distance.set(it.distance)
                            trackViewModel.duration.set(it.duration)
                            trackViewModel.iconType.set(it.type)
                            trackViewModel.startDay.set(it.start)
                            trackViewModel.completeBounds.set(it.bounds)
                            trackViewModel.waypoints.set(it.waypoints)
                            val trackPolylineProvider = TrackPolylineProvider(trackViewModel.waypoints.get())
                            trackPolylineProvider.drawPolylines()
                            trackViewModel.polylines = trackPolylineProvider.lineOptions
                            Handler(Looper.getMainLooper()).post { holder.addLinesToMap() }
                        }
                    })
                }
            }
        }
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        var googleMap: GoogleMap? = null

        fun addLinesToMap() {
            googleMap?.clear()
            binding.viewModel.polylines.map {
                googleMap?.addPolyline(it)
            }
        }
    }
}

