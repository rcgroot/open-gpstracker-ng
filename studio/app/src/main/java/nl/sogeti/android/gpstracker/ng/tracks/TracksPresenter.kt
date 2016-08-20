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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.utils.getLong
import nl.sogeti.android.gpstracker.ng.utils.getString
import nl.sogeti.android.gpstracker.ng.utils.map
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding

class TracksPresenter(val model: TracksViewModel) : RecyclerView.Adapter<TracksPresenter.ViewHolder>() {

    private var context: Context? = null
    var listener: Listener? = null

    override fun getItemCount(): Int {
        return model.track.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val trackViewModel = model.track[position]
        if (holder != null) {
            val binding = holder.binding
            binding.viewModel = trackViewModel
            binding.presenter = this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(parent?.context), R.layout.row_track, parent, false)
        val viewHolder = ViewHolder(binding)

        return viewHolder
    }

    fun start(context: Context) {
        this.context = context
        val trackCreation: (Cursor) -> TrackViewModel = {
            val id = it.getLong(ContentConstants.Tracks._ID)
            val uri = ContentUris.withAppendedId(ContentConstants.Tracks.CONTENT_URI, id);
            val name = it.getString(ContentConstants.Tracks.NAME)
            TrackViewModel(uri, name)
        }
        val tracks = ContentConstants.Tracks.CONTENT_URI.map(context, trackCreation)
        model.track.addAll(tracks)
    }

    fun stop() {
        context = null
    }

    fun onTrackClick(viewModel: TrackViewModel) {
        listener?.onTrackSelected(viewModel.uri.get())
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root) {}

    interface Listener {
        fun onTrackSelected(uri: Uri)
    }
}