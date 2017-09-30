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
package nl.sogeti.android.gpstracker.ng.track

import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.NAME
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.utils.apply
import nl.sogeti.android.gpstracker.ng.utils.getString
import javax.inject.Inject

class TrackPresenter(private val viewModel: TrackViewModel, private val view: TrackViewModel.View) : ContextedPresenter<TrackNavigator>(), TrackSelection.Listener, ContentController.Listener {

    private var contentController: ContentController? = null
    @Inject
    lateinit var trackSelection: TrackSelection
    @Inject
    lateinit var contentControllerFactory: ContentControllerFactory

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    //region Presenter context

    override fun didStart() {
        trackSelection.addListener(this)
        contentController = contentControllerFactory.createContentController(context, this)
        trackSelection.trackUri?.let {
            onTrackSelection(it, trackSelection.trackName)
        }
    }

    override fun willStop() {
        trackSelection.removeListener(this)
        contentController?.unregisterObserver()
        contentController = null
    }

    //endregion

    //region View

    fun onListOptionSelected() {
        navigation.showTrackSelection()
    }

    fun onAboutOptionSelected() {
        navigation.showAboutDialog()
    }

    fun onEditOptionSelected() {
        val trackUri = viewModel.trackUri.get()
        trackUri?.let { navigation.showTrackEditDialog(it) }
    }

    fun onGraphsOptionSelected() {
        navigation.showGraphs()
    }

    //endregion

    //region TrackSelection

    override fun onTrackSelection(trackUri: Uri, name: String) {
        viewModel.trackUri.set(trackUri)
        contentController?.registerObserver(trackUri)
        showName(name)
    }

    //endregion

    //region Content watching

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        val name = contentUri.apply(context) { it.getString(NAME) }
        name?.let {
            showName(it)
        }
    }

    //endregion

    //region Private

    private fun showName(name: String) {
        viewModel.name.set(name)
        view.showTrackName(name)
    }

    //endregion
}
