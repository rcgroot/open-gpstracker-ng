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
package nl.sogeti.android.gpstracker.ng.trackedit

import android.net.Uri
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.INVALID_POSITION
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.trackedit.TrackTypeDescriptions.Companion.loadTrackTypeFromContext
import nl.sogeti.android.gpstracker.ng.tracklist.summary.summaryManager
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R

class TrackEditPresenter(val model: TrackEditModel, val listener: TrackEditModel.View) : ContextedPresenter() {

    val spinnerAdapter: SpinnerAdapter by lazy {
        object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val viewHolder: ViewHolder
                val itemView: View
                if (convertView == null) {
                    itemView = LayoutInflater.from(context).inflate(R.layout.row_track_type, parent, false)
                    viewHolder = ViewHolder(
                            itemView.findViewById(R.id.row_track_type_image) as ImageView,
                            itemView.findViewById(R.id.row_track_type_text) as TextView)
                    itemView.tag = viewHolder
                } else {
                    itemView = convertView
                    viewHolder = convertView.tag as ViewHolder
                }
                val trackType = model.trackTypes[position]
                viewHolder.textView.text = context?.getString(trackType.stringId)
                viewHolder.imageView.setImageDrawable(context?.getDrawable(trackType.drawableId))
                context?.let { viewHolder.imageView.setImageDrawable(AppCompatResources.getDrawable(it, trackType.drawableId)) }

                return itemView
            }

            override fun getItem(position: Int): TrackType = model.trackTypes[position]

            override fun getItemId(position: Int): Long = model.trackTypes[position].drawableId.toLong()

            override fun getCount() = model.trackTypes.size
        }
    }

    val onItemSelectedListener: AdapterView.OnItemSelectedListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.selectedPosition.set(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                model.selectedPosition.set(INVALID_POSITION)
            }
        }
    }

    override fun didStart() {
        val trackUri = model.trackUri.get()
        val trackId: Long = trackUri.lastPathSegment.toLong()

        loadTrackName(trackUri)
        loadTrackType(trackId)
    }

    override fun willStop() {
    }

    fun ok() {
        saveTrackName()
        saveTrackType()
        summaryManager.summaryCache.remove(model.trackUri.get())
        listener.dismiss()
    }

    fun cancel() {
        listener.dismiss()
    }

    private fun loadTrackType(trackId: Long) {
        context?.let {
            val trackType = loadTrackTypeFromContext(trackId, it)
            val position = model.trackTypes.indexOfFirst { it == trackType }
            model.selectedPosition.set(position)
        }
    }

    private fun saveTrackType() {
        val trackUri = model.trackUri.get()
        val trackId: Long = trackUri.lastPathSegment.toLong()
        val trackType = model.trackTypes.get(model.selectedPosition.get())
        metaDataTrackUri(trackId).updateCreateMetaData(context!!, TrackTypeDescriptions.KEY_META_FIELD_TRACK_TYPE, trackType.contentValue)
    }

    private fun loadTrackName(trackUri: Uri) {
        model.name.set(trackUri.apply(context!!, { it.getString(ContentConstants.TracksColumns.NAME) ?: "" }))
    }

    private fun saveTrackName() {
        model.trackUri.get()?.updateName(context!!, model.name.get())
    }

    data class ViewHolder(val imageView: ImageView, val textView: TextView)
}
