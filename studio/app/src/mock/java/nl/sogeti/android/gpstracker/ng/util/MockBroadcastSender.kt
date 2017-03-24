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
package nl.sogeti.android.gpstracker.ng.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.SYSTEM_LOW_PRIORITY
import android.os.Handler
import android.os.Looper
import android.support.test.espresso.idling.CountingIdlingResource
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.ng.common.MockedGpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class MockBroadcastSender {

    @field:[Inject Named("loggingStateFilter")]
    lateinit var loggingStateIntentFilter: IntentFilter

    init {
        MockedGpsTrackerApplication.mockAppComponent.inject(this)
    }

    fun sendStartedRecording(context: Context, trackId: Long) {
        broadcastLoggingState(context, ServiceConstants.STATE_LOGGING, trackId)
    }

    fun sendStoppedRecording(context: Context) {
        broadcastLoggingState(context, ServiceConstants.STATE_STOPPED, null)
    }

    fun sendPausedRecording(context: Context, trackId: Long) {
        broadcastLoggingState(context, ServiceConstants.STATE_PAUSED, trackId)
    }

    fun sendResumedRecording(context: Context, trackId: Long) {
        broadcastLoggingState(context, ServiceConstants.STATE_LOGGING, trackId)
    }

    fun broadcastLoggingState(context: Context, state: Int, trackId: Long?, precision: Int = ServiceConstants.LOGGING_NORMAL) {
        resource.increment()
        loggingStateIntentFilter.priority = SYSTEM_LOW_PRIORITY + 5
        context.registerReceiver(receiver, loggingStateIntentFilter)
        val intent = Intent(loggingStateIntentFilter.getAction(0))
        intent.putExtra(ServiceConstants.EXTRA_LOGGING_STATE, state)
        intent.putExtra(ServiceConstants.EXTRA_LOGGING_PRECISION, precision)
        trackId?.let { intent.putExtra(ServiceConstants.EXTRA_TRACK, trackUri(trackId)) }
        context.sendOrderedBroadcast(intent, null)
    }

    companion object Espresso {
        val resource = CountingIdlingResource("MockBroadcastSender", true)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    Timber.d("Received in the mock sender")
                    resource.decrement()
                    context?.unregisterReceiver(this)
                }, 50)
            }
        }
    }
}