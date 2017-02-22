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
package nl.sogeti.android.gpstracker.ng.map

import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import javax.inject.Inject

class TrackPresenter(private val viewModel: TrackViewModel, private val view: TrackViewModel.View) : ContextedPresenter(), TrackSelection.Listener {

    @Inject
    lateinit var trackSelection: TrackSelection

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    //region Presenter context

    override fun didStart() {
        trackSelection.addListener(this)
        trackSelection.trackUri?.let {
            didSelectTrack(it, trackSelection.trackName)
        }

    }

    override fun willStop() {
        trackSelection.removeListener(this)
    }

    //endregion

    //region View

    fun onListOptionSelected() {
        view.selectTrack()
    }

    fun onAboutOptionSelected() {
        view.showAboutDialog()
    }

    fun onEditOptionSelected() {
        view.showTrackTitleDialog()
    }

    //endregion

    //region TrackSelection

    override fun didSelectTrack(trackUri: Uri, name: String) {
        viewModel.trackUri.set(trackUri)
        viewModel.name.set(name)
        view.setTrackName(name)
    }

    //endregion
}