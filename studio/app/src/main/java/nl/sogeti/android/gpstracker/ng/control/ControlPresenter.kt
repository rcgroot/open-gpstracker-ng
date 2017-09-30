/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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
package nl.sogeti.android.gpstracker.ng.control

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.Navigation
import nl.sogeti.android.gpstracker.ng.trackedit.NameGenerator
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject

class ControlPresenter(private val viewModel: ControlViewModel) : ConnectedServicePresenter<Navigation>() {
    @Inject
    lateinit var nameGenerator: NameGenerator
    @Inject
    lateinit var asyncExecutor: Executor

    val handler = Handler(Looper.getMainLooper())
    private val enableRunnable = { enableButtons() }

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    //region Service connection

    override fun didConnectToService(trackUri: Uri?, name: String?, loggingState: Int) {
        viewModel.state.set(loggingState)
        enableButtons()
    }

    override fun didChangeLoggingState(trackUri: Uri?, name: String?, loggingState: Int) {
        viewModel.state.set(loggingState)
        enableButtons()

        if (trackUri != null && loggingState == STATE_LOGGING) {
            asyncExecutor.execute {
                checkForInitialName(context, trackUri)
            }
        }
    }

    //endregion

    //region View callback

    fun onClickLeft() {
        disableUntilChange(200)
        if (viewModel.state.get() == STATE_LOGGING) {
            stopLogging(context)
        } else if (viewModel.state.get() == STATE_PAUSED) {
            stopLogging(context)
        }
    }

    fun onClickRight() {
        disableUntilChange(200)
        if (viewModel.state.get() == STATE_STOPPED) {
            startLogging(context)
        } else if (viewModel.state.get() == STATE_LOGGING) {
            pauseLogging(context)
        } else if (viewModel.state.get() == STATE_PAUSED) {
            resumeLogging(context)
        }
    }

    //endregion

    private fun disableUntilChange(timeout: Long) {
        viewModel.enabled.set(false)
        handler.postDelayed(enableRunnable, timeout)
    }

    private fun enableButtons() {
        handler.removeCallbacks { enableRunnable }
        viewModel.enabled.set(true)
    }

    private fun checkForInitialName(context: Context, trackUri: Uri) {
        val name = trackUri.readName(context)
        if (name == context.getString(R.string.initial_track_name)) {
            val generatedName = nameGenerator.generateName(context, Calendar.getInstance())
            trackUri.updateName(context, generatedName)
        }
    }

    fun startLogging(context: Context) {
        serviceManager.startGPSLogging(context, context.getString(R.string.initial_track_name))
    }

    fun stopLogging(context: Context) {
        serviceManager.stopGPSLogging(context)
        deleteEmptyTrack(context, serviceManager.trackId)
    }

    fun pauseLogging(context: Context) {
        serviceManager.pauseGPSLogging(context)
    }

    fun resumeLogging(context: Context) {
        serviceManager.resumeGPSLogging(context)
    }

    private fun deleteEmptyTrack(context: Context, trackId: Long) {
        if (trackId <= 0) {
            return
        }

        val waypointsUri = waypointsUri(trackId)
        val firstWaypointId = waypointsUri.apply(context) { it.getLong(0) } ?: -1L
        if (firstWaypointId == -1L) {
            context.contentResolver.delete(trackUri(trackId), null, null)
        }
    }
}
