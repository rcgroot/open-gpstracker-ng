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
package nl.sogeti.android.gpstracker.ng.features.trackedit

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.INVALID_POSITION
import android.widget.ImageView
import android.widget.TextView
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.AbstractTrackPresenter
import nl.sogeti.android.gpstracker.service.util.readName
import nl.sogeti.android.gpstracker.service.util.updateName
import javax.inject.Inject

class TrackEditPresenter(trackUri: Uri) : AbstractTrackPresenter(trackUri) {

    @Inject
    lateinit var summaryManager: SummaryManager

    @Inject
    lateinit var trackTypeDescriptions: TrackTypeDescriptions

    val model = TrackEditModel(trackUri)

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
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onChange() {
        loadTrackTypePosition(trackUri)
        loadTrackName(trackUri)
    }

    fun ok(trackUri: Uri, trackName: String) {
        saveTrackName(trackUri, trackName)
        saveTrackTypePosition(trackUri)
        summaryManager.removeFromCache(trackUri)
        model.dismissed.set(true)
    }

    fun cancel() {
        model.dismissed.set(true)
    }

    private fun loadTrackName(trackUri: Uri) {
        val trackName = trackUri.readName()
        model.name.set(trackName)
    }

    private fun loadTrackTypePosition(trackUri: Uri) {
        val trackType = trackTypeDescriptions.loadTrackType(trackUri)
        val position = model.trackTypes.indexOfFirst { it == trackType }
        model.selectedPosition.set(position)
    }

    private fun saveTrackName(trackUri: Uri, trackName: String) {
        trackUri.updateName(trackName)
    }

    private fun saveTrackTypePosition(trackUri: Uri) {
        val trackType = model.trackTypes[model.selectedPosition.get()]
        trackTypeDescriptions.saveTrackType(trackUri, trackType)
    }

    data class ViewHolder(val imageView: ImageView, val textView: TextView)

    companion object {

        fun newFactory(uri: Uri) =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T = TrackEditPresenter(uri) as T
                }

    }
}
