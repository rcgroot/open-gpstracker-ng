/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints.WAYPOINTS
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackPolylineProvider
import nl.sogeti.android.gpstracker.ng.tracklist.summary.summaryManager
import nl.sogeti.android.gpstracker.ng.utils.append
import nl.sogeti.android.gpstracker.ng.utils.count
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding

class TrackListViewAdapter(val context: Context) : RecyclerView.Adapter<TrackListViewAdapter.ViewHolder>() {

    var listener: TrackListListener? = null
    var model = listOf<Uri>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(TrackDiffer(field, value))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }
    private val rowModels = mutableMapOf<Uri, TrackViewModel>()

    override fun getItemCount(): Int {
        return model.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(parent?.context), R.layout.row_track, parent, false)
        val holder = ViewHolder(binding)
        // Weirdly enough the 'clickable="false"' in the XML resource doesn't work
        holder.binding.rowTrackMap.isClickable = false
        holder.binding.adapter = this
        holder.binding.rowTrackMap.onCreate(null)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modelForUri = rowViewModelForUri(model[position])
        if (holder.binding.viewModel != modelForUri) {
            holder.binding.viewModel = modelForUri
        }
        if (!holder.didGetMaps) {
            holder.didGetMaps = true
            holder.binding.rowTrackMap.getMapAsync {
                holder.googleMap = it
            }
        }
        willDisplayTrack(holder.itemView.context, holder.binding.viewModel)
    }

    fun didSelectTrack(track: TrackViewModel) {
        listener?.didSelectTrack(track)
    }

    private fun rowViewModelForUri(uri: Uri): TrackViewModel? {
        var viewModel = rowModels[uri]
        if (viewModel == null) {
            viewModel = TrackViewModel(uri)
            rowModels[uri] = viewModel
        }

        return viewModel
    }

    private fun willDisplayTrack(context: Context, viewModel: TrackViewModel) {
        summaryManager.collectSummaryInfo(context, viewModel.uri, {
            if (it.trackUri == viewModel.uri) {
                viewModel.completeBounds.set(it.bounds)
                viewModel.distance.set(it.distance)
                viewModel.duration.set(it.duration)
                viewModel.waypoints.set(it.waypoints)
                val trackPolylineProvider = TrackPolylineProvider(viewModel.waypoints.get())
                viewModel.polylines.set(trackPolylineProvider.lineOptions)
                viewModel.name.set(it.name)
                viewModel.iconType.set(it.type)
                viewModel.startDay.set(it.start)
            }
        })
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        var didGetMaps = false
        var googleMap: GoogleMap? = null
            set(map) {
                field = map
                binding.rowTrackMap.tag = field
                map?.uiSettings?.isMapToolbarEnabled = false
                binding.viewModel.polylines.notifyChange()
            }
    }

    inner class TrackDiffer(val oldList: List<Uri>, val newList: List<Uri>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            val renderedWaypoints = rowViewModelForUri(oldItem)?.waypoints?.get()

            val oldCount = renderedWaypoints?.count() ?: -1
            val newCount = newItem.append(WAYPOINTS).count(context)

            return oldCount == newCount
        }
    }

}

