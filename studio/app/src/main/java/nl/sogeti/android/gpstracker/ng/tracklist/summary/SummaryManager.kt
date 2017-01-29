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
package nl.sogeti.android.gpstracker.ng.tracklist.summary

import android.content.Context
import android.net.Uri
import android.os.Build
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * Helps in the retrieval, create and keeping up to date of summary data
 */
object summaryManager {
    var executor: ExecutorService? = null
    val calculator by lazy { SummaryCalculator() }
    val summaryCache = ConcurrentHashMap<Uri, Summary>()
    var activeCount = 0

    fun start() {
        synchronized(this, {
            activeCount++
            if (executor == null) {
                executor = Executors.newFixedThreadPool(numberOfThreads(), BackgroundThreadFactory())
            }
        })
    }

    fun stop() {
        synchronized(this, {
            activeCount--
            if (!isRunning()) {
                executor?.shutdown()
                executor = null
            }
            if (activeCount < 0) {
                activeCount++
                throw IllegalStateException("Received more stops then starts")
            }
        })
    }

    fun isRunning(): Boolean = synchronized(this, { activeCount > 0 })

    /**
     * Collects summary data from the meta table.
     */
    fun collectSummaryInfo(context: Context, trackUri: Uri,
                           callbackSummary: (Summary) -> Unit) {
        if (!isRunning()) {
            return
        }
        executor?.submit({
            val cacheHit = summaryCache[trackUri]
            if (cacheHit != null) {
                callbackSummary(cacheHit)
            } else {
                executeTrackCalculation(context, trackUri, callbackSummary)
            }
        })
    }

    fun executeTrackCalculation(context: Context, trackUri: Uri, callbackSummary: (Summary) -> Unit) {
        if (isRunning()) {
            val summary = calculator.calculateSummary(context, trackUri)
            if (isRunning()) {
                summaryCache.put(trackUri, summary)
                callbackSummary(summary)
            }
        }
    }

    fun numberOfThreads(): Int {
        val threads: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            threads = Runtime.getRuntime().availableProcessors()
        } else {
            threads = 2
        }

        return threads
    }

    internal class BackgroundThreadFactory : ThreadFactory {
        val group = ThreadGroup("SummaryManager")

        init {
            group.isDaemon = false
            group.maxPriority = android.os.Process.THREAD_PRIORITY_BACKGROUND
        }

        override fun newThread(task: Runnable?): Thread {
            val thread = Thread(group, task)

            return thread
        }
    }
}