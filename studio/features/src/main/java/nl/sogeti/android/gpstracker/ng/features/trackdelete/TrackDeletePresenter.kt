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
package nl.sogeti.android.gpstracker.ng.features.trackdelete

import android.content.ContentResolver
import android.net.Uri
import android.support.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.AbstractTrackPresenter
import nl.sogeti.android.gpstracker.service.util.readName
import javax.inject.Inject

class TrackDeletePresenter : AbstractTrackPresenter() {

    internal val viewModel = TrackDeleteModel()
    @Inject
    lateinit var contentResolver: ContentResolver
    @Inject
    lateinit var summaryManager: SummaryManager

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    @WorkerThread
    override fun onChange() {
        val trackUri = viewModel.trackUri.get()
        trackUri?.let {
            loadTrackName(trackUri)
        }
    }

    fun ok() {
        val trackUri = viewModel.trackUri.get()
        trackUri?.let {
            deleteTrack(trackUri)
            summaryManager.removeFromCache(trackUri)
            viewModel.dismiss.set(true)
        }
    }

    fun cancel() {
        viewModel.dismiss.set(true)
    }

    private fun loadTrackName(trackUri: Uri) {
        val trackName = trackUri.readName()
        viewModel.trackUri.set(trackUri)
        viewModel.name.set(trackName)
    }

    private fun deleteTrack(trackUri: Uri) {
        contentResolver.delete(trackUri, null, null)
    }
}
