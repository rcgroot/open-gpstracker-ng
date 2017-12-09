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

import android.content.Context
import android.database.ContentObserver
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.readTrack
import nl.sogeti.android.gpstracker.v2.sharedwear.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class StatisticsCollector {

    private var context: Context? = null
    private var contentObserver: ContentObserver? = null
    private lateinit var messageSender: MessageSender
    @Inject
    lateinit var calculator: SummaryCalculator
    @Inject
    lateinit var messageSenderFactory: MessageSenderFactory

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun start(context: Context, trackUri: Uri) {
        this.context = context
        messageSender = messageSenderFactory.createMessageSender(context, Capability.CAPABILITY_CONTROL, AsyncTask.SERIAL_EXECUTOR)

        messageSender.start()
        observeUri(trackUri)
    }

    fun pause() {
        messageSender.sendMessage(StatusMessage(StatusMessage.Status.PAUSE))
    }

    fun stop() {
        unObserveUri()
        messageSender.sendMessage(StatusMessage(StatusMessage.Status.STOP))
        messageSender.stop()
    }

    private fun observeUri(trackUri: Uri) {
        val handlerThread = HandlerThread("WearContentObserver", Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val contentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                if (messageSender.connected) {
                    val message = collectStatistics(trackUri)
                    if (message != null) {
                        messageSender.sendMessage(message)
                    }
                } else {
                    Timber.d("No peer for Wear statistics")
                }
            }
        }
        context?.contentResolver?.registerContentObserver(trackUri, true, contentObserver)
    }

    private fun collectStatistics(trackUri: Uri): StatisticsMessage? {
        context?.let {
            val recent = Date().time - ONE_MINUTE
            val handler = DefaultResultHandler()
            trackUri.readTrack(it, handler)
            if (handler.waypoints.isNotEmpty()) {
                var milliSeconds = 0L
                var meters = 0.0F
                var recentMilliSeconds = 0L
                var recentMeters = 0.0F
                val results = FloatArray(1)
                handler.waypoints.forEach {
                    for (i in 1..it.lastIndex) {
                        val w1 = it[i - 1]
                        val w2 = it[i]
                        Location.distanceBetween(w1.latitude, w1.longitude, w2.latitude, w2.longitude, results)
                        meters += results.first()
                        milliSeconds += w2.time - w1.time
                        if (w2.time > recent) {
                            recentMeters += results.first()
                            recentMilliSeconds += w2.time - w1.time
                        }
                    }
                }
                val endTime = handler.waypoints.last().last().time
                val startTime = handler.waypoints.first().first().time
                val currentSpeed = calculator.convertMeterPerSecondsToSpeed(it, recentMeters, recentMilliSeconds / 1000).replace(' ', '\n')
                val distance = calculator.convertMetersToDistance(it, meters).replace(' ', '\n')
                val duration = calculator.convertStartEndToDuration(it, startTime, endTime).replace(' ', '\n')

                return StatisticsMessage(currentSpeed, distance, duration)
            }
        }

        return null
    }

    private fun unObserveUri() {
        if (contentObserver != null) {
            context?.contentResolver?.unregisterContentObserver(contentObserver)
        }
    }

    companion object {
        const val ONE_MINUTE = 60_000
    }
}
