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
package nl.sogeti.android.gpstracker.ng.tracks

import android.database.Cursor
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.tracks.summary.summaryManager
import nl.sogeti.android.gpstracker.ng.utils.getLong
import nl.sogeti.android.gpstracker.ng.utils.getString
import nl.sogeti.android.gpstracker.ng.utils.map
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding

class TracksPresenter(val model: TracksViewModel) : ContextedPresenter() {
    var listener: Listener? = null
    val viewAdapter = ViewAdapter()

    override fun didStart() {
        summaryManager.start()
        val trackCreation: (Cursor) -> TrackViewModel = {
            val id = it.getLong(ContentConstants.Tracks._ID)!!
            val uri = trackUri(id)
            val name = it.getString(ContentConstants.Tracks.NAME) ?: ""
            TrackViewModel(uri, name)
        }
        context?.let {
            val tracks = trackUri().map(it, trackCreation)
            model.track.addAll(tracks)
        }
    }

    override fun willStop() {
        summaryManager.stop()
    }

    fun onTrackClick(viewModel: TrackViewModel) {
        listener?.onTrackSelected(viewModel.uri.get())
    }

    inner class ViewAdapter : RecyclerView.Adapter<TracksPresenter.ViewHolder>() {

        override fun getItemCount(): Int {
            return model.track.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(parent?.context), R.layout.row_track, parent, false)
            val viewHolder = ViewHolder(binding)

            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val trackViewModel = model.track[position]
            if (holder != null) {
                val binding = holder.binding
                binding.viewModel = trackViewModel
                binding.presenter = this@TracksPresenter
                context?.let {
                    summaryManager.collectSummaryInfo(it, trackViewModel.uri.get(), {
                        if (it.track.equals(trackViewModel.uri.get())) {
                            trackViewModel.name.set(it.name)
                            trackViewModel.distance.set(it.distance)
                            trackViewModel.duration.set(it.duration)
                            trackViewModel.iconType.set(it.type)
                            trackViewModel.startDay.set(it.start)
                        }
                    })
                }
            }
        }

    }

    interface Listener {
        fun onTrackSelected(uri: Uri)
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root) {}
}