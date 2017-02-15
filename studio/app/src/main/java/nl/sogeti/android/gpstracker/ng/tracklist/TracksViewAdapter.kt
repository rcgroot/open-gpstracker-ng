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
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackPolylineProvider
import nl.sogeti.android.gpstracker.ng.tracklist.summary.summaryManager
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding

class TracksViewAdapter : RecyclerView.Adapter<TracksViewAdapter.ViewHolder>() {

    var listener: TrackListListener? = null
    var model: List<TrackViewModel> = listOf()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(TrackDiffer(field, value))
            field = value.toList()
            diffResult.dispatchUpdatesTo(this)
        }

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
        holder.binding.viewModel = model[position]
        if (!holder.didGetMaps) {
            holder.didGetMaps = true
            holder.binding.rowTrackMap.getMapAsync {
                holder.googleMap = it
            }
        }
        willDisplayTrack(holder.itemView.context, model[position])
    }

    fun willDisplayTrack(context: Context, track: TrackViewModel) {
        summaryManager.collectSummaryInfo(context, track.uri.get(), {
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
                track.polylines.set(trackPolylineProvider.lineOptions)
            }
        })
    }

    fun didSelectTrack(track: TrackViewModel) {
        listener?.didSelectTrack(track)
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

    class TrackDiffer(val oldList: List<TrackViewModel>, val newList: List<TrackViewModel>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uri.get() == newList[newItemPosition].uri.get()
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uri.get() == newList[newItemPosition].uri.get()
        }
    }

}

