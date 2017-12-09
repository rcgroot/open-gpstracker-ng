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
package nl.sogeti.android.gpstracker.ng.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class LoggingReceiver : BroadcastReceiver() {

    @Inject
    @field:Named("stateBroadcastAction")
    lateinit var stateAction: String

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == stateAction) {
            onStateReceive(context, intent)
        }
    }

    private fun onStateReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(EXTRA_LOGGING_STATE, STATE_UNKNOWN)

        when (state) {
            STATE_LOGGING -> if (intent.hasExtra(EXTRA_TRACK)) {
                val trackUri: Uri = intent.getParcelableExtra(EXTRA_TRACK)
                context.startService(LoggingService.createStartedIntent(context, trackUri))
            } else {
                Timber.e("Failed to handle state change $intent")
            }
            STATE_PAUSED -> context.startService(LoggingService.createPausedIntent(context))
            STATE_STOPPED -> context.startService(LoggingService.createStoppedIntent(context))
            else -> context.stopService(LoggingService.createStopIntent(context))

        }
    }

    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(stateAction))
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}
