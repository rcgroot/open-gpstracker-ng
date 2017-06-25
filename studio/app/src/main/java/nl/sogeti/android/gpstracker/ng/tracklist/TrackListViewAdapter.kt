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
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints.WAYPOINTS
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackPolylineProvider
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.utils.append
import nl.sogeti.android.gpstracker.ng.utils.count
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding
import javax.inject.Inject

class TrackListViewAdapter(val context: Context) : RecyclerView.Adapter<TrackListViewAdapter.ViewHolder>() {

    @Inject
    lateinit var summaryManager: SummaryManager
    @Inject
    lateinit var calculator: SummaryCalculator
    var listener: TrackListAdapterListener? = null

    var selection: Uri? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var model = listOf<Uri>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(TrackDiffer(field, value))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }
    private val rowModels = mutableMapOf<Uri, TrackViewModel>()

    init {
        GpsTrackerApplication.appComponent.inject(this)
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return model.size
    }

    override fun getItemId(position: Int): Long {
        return model[position].lastPathSegment.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(context), R.layout.row_track, parent, false)
        val holder = ViewHolder(binding)
        // Weirdly enough the 'clickable="false"' in the XML resource doesn't work
        holder.binding.rowTrackMap.isClickable = false
        holder.binding.adapter = this
        holder.binding.rowTrackMap.onCreate(null)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewModel = rowViewModelForUri(model[position])
        if (holder.binding.viewModel != viewModel) {
            holder.binding.viewModel = viewModel
        }
        holder.binding.rowTrackCard.isActivated = (viewModel.uri == selection)
        willDisplayTrack(holder.itemView.context, viewModel)
    }

    //region Row callbacks

    fun didSelectTrack(trackModel: TrackViewModel) {
        listener?.didSelectTrack(trackModel.uri, trackModel.name.get())
        trackModel.editMode.set(false)
    }

    fun didShareTrack(trackModel: TrackViewModel) {
        listener?.didShareTrack(trackModel.uri)
        trackModel.editMode.set(false)
    }

    fun didEditTrack(trackModel: TrackViewModel) {
        listener?.didEditTrack(trackModel.uri)
        trackModel.editMode.set(false)
    }

    fun didDeleteTrack(trackModel: TrackViewModel) {
        listener?.didDeleteTrack(trackModel.uri)
        trackModel.editMode.set(false)
    }

    fun didClickRowOptions(track: TrackViewModel) {
        val opposite = !track.editMode.get()
        track.editMode.set(opposite)
    }

    //endregion

    private fun rowViewModelForUri(uri: Uri): TrackViewModel {
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
                val listOfLatLngs = it.waypoints.map { it.map { it.latLng } }
                viewModel.waypoints.set(listOfLatLngs)
                val trackPolylineProvider = TrackPolylineProvider(viewModel.waypoints.get())
                viewModel.polylines.set(trackPolylineProvider.lineOptions)
                viewModel.iconType.set(it.type)
                viewModel.name.set(it.name)
                viewModel.startDay.set(calculator.convertTimestampToStart(context, it.startTimestamp))
                var duration = context.getString(R.string.row_duraction_default)
                if (it.startTimestamp in 1..(it.stopTimestamp - 1)) {
                    duration = calculator.convertStartEndToDuration(context, it.startTimestamp, it.stopTimestamp)
                }
                viewModel.duration.set(duration)
                var distance = context.getString(R.string.row_distance_default)
                if (it.distance > 0) {
                    distance = calculator.convertMetersToDistance(context, it.distance)
                }
                viewModel.distance.set(distance)
            }
        })
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root)

    inner class TrackDiffer(val oldList: List<Uri>, val newList: List<Uri>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            val renderedWaypoints = rowViewModelForUri(oldItem).waypoints.get()

            val oldCount = renderedWaypoints?.count() ?: -1
            val newCount = newItem.append(WAYPOINTS).count(context)

            return oldCount == newCount
        }
    }
}

