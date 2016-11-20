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
import android.support.test.espresso.idling.CountingIdlingResource
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.ng.injection.Injection
import nl.sogeti.android.gpstracker.ng.utils.trackUri

class MockBroadcastSender {

    val ACTION = Injection.CONFIG_BROADCAST

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
        val filter = IntentFilter(Injection.CONFIG_BROADCAST)
        filter.priority = -2000;
        context.registerReceiver(receiver, filter)
        val intent = Intent(ACTION)
        intent.putExtra(ServiceConstants.EXTRA_LOGGING_STATE, state)
        intent.putExtra(ServiceConstants.EXTRA_LOGGING_PRECISION, precision)
        trackId?.let { intent.putExtra(ServiceConstants.EXTRA_TRACK, trackUri(trackId)) }
        context.sendOrderedBroadcast(intent, null)
    }

    companion object Espresso {
        val resource = CountingIdlingResource("MockBroadcastSender", true)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                resource.decrement()
                context?.unregisterReceiver(this)
            }
        }
    }
}