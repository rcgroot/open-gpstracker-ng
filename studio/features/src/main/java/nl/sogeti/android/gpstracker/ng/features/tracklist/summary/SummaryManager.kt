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
package nl.sogeti.android.gpstracker.ng.features.tracklist.summary

import android.content.Context
import android.net.Uri
import android.os.Build
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Waypoints.WAYPOINTS
import nl.sogeti.android.gpstracker.utils.append
import nl.sogeti.android.gpstracker.utils.concurrent.BackgroundThreadFactory
import nl.sogeti.android.gpstracker.utils.count
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Helps in the retrieval, create and keeping up to date of summary data
 */
class SummaryManager {

    var executor: ExecutorService? = null
    val summaryCache = ConcurrentHashMap<Uri, Summary>()
    var activeCount = 0
    @Inject
    lateinit var calculator: SummaryCalculator
    @Inject
    lateinit var context: Context

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    fun start() {
        synchronized(this, {
            activeCount++
            if (executor == null) {
                executor = Executors.newFixedThreadPool(numberOfThreads(), BackgroundThreadFactory("SummaryManager"))
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
    fun collectSummaryInfo(trackUri: Uri,
                           callbackSummary: (Summary) -> Unit) {
        val executor = executor ?: return
        executor.submit({
            val cacheHit = summaryCache[trackUri]
            if (cacheHit != null) {
                val trackWaypointsUri = trackUri.append(WAYPOINTS)
                val trackCount = trackWaypointsUri.count(context)
                val cacheCount = cacheHit.count
                if (trackCount == cacheCount) {
                    callbackSummary(cacheHit)
                } else {
                    executeTrackCalculation(trackUri, callbackSummary)
                }
            } else {
                executeTrackCalculation(trackUri, callbackSummary)
            }
        })
    }

    fun executeTrackCalculation(trackUri: Uri, callbackSummary: (Summary) -> Unit) {
        if (isRunning()) {
            val summary = calculator.calculateSummary(trackUri)
            if (isRunning()) {
                summaryCache.put(trackUri, summary)
                callbackSummary(summary)
            }
        }
    }

    fun numberOfThreads() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Runtime.getRuntime().availableProcessors()
            } else {
                2
            }

    fun removeFromCache(trackUri: Uri) {
        summaryCache.remove(trackUri)
    }
}
