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

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.INVALID_POSITION
import android.widget.ImageView
import android.widget.TextView
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.Navigation
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.utils.readName
import nl.sogeti.android.gpstracker.ng.utils.updateName
import javax.inject.Inject

class TrackEditPresenter(val model: TrackEditModel, val view: TrackEditModel.View) : ContextedPresenter<Navigation>() {

    @Inject
    lateinit var summaryManager: SummaryManager
    @Inject
    lateinit var trackTypeDescriptions: TrackTypeDescriptions

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

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    override fun didStart() {
        val trackUri = model.trackUri.get()

        loadTrackTypePosition(context, trackUri)
        loadTrackName(context, trackUri)
    }

    override fun willStop() {
    }

    fun ok() {
        val trackUri = model.trackUri.get()
        saveTrackName(context, trackUri)
        saveTrackTypePosition(context, trackUri)
        summaryManager.removeFromCache(trackUri)
        view.dismiss()
    }

    fun cancel() {
        view.dismiss()
    }

    private fun saveTrackTypePosition(it: Context, trackUri: Uri) {
        val trackType = model.trackTypes.get(model.selectedPosition.get())
        trackTypeDescriptions.saveTrackType(it, trackUri, trackType)
    }

    private fun loadTrackTypePosition(context: Context, trackUri: Uri) {
        val trackType = trackTypeDescriptions.loadTrackType(context, trackUri)
        val position = model.trackTypes.indexOfFirst { it == trackType }
        model.selectedPosition.set(position)
    }

    private fun loadTrackName(context: Context, trackUri: Uri) {
        val trackName = trackUri.readName(context)
        model.name.set(trackName)
    }

    private fun saveTrackName(context: Context, trackUri: Uri) {
        val trackName = model.name.get()
        trackUri.updateName(context, trackName)
    }

    data class ViewHolder(val imageView: ImageView, val textView: TextView)
}
