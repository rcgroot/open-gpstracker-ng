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
package nl.sogeti.android.gpstracker.ng.features.wear

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import javax.inject.Inject


class LoggingService : Service() {

    @Inject
    lateinit var statisticsCollector: StatisticsCollector

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateTrack(intent)
        updateLoggingState(intent)

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        statisticsCollector.stop()
    }

    private fun updateLoggingState(intent: Intent?) {
        val state = intent?.getStringExtra(STATE)
        when (state) {
            START -> statisticsCollector.start()
            PAUSE -> statisticsCollector.pause()
            STOP -> stopSelf()
            null -> stopSelf()
        }
    }

    private fun updateTrack(intent: Intent?) {
        val trackUri: Uri? = intent?.getParcelableExtra(TRACK)
        statisticsCollector.trackUri = trackUri
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        private const val TRACK = "WearLoggingService_Track"
        private const val STATE = "WearLoggingService_State"
        private const val START = "WearLoggingService_State_Started"
        private const val PAUSE = "WearLoggingService_State_Paused"
        private const val STOP = "WearLoggingService_State_Stopped"

        @JvmStatic
        fun createStartedIntent(context: Context, trackUri: Uri): Intent {
            val intent = Intent(context, LoggingService::class.java)
            intent.putExtra(TRACK, trackUri)
            intent.putExtra(STATE, START)
            return intent
        }

        @JvmStatic
        fun createPausedIntent(context: Context, trackUri: Uri): Intent {
            val intent = Intent(context, LoggingService::class.java)
            intent.putExtra(TRACK, trackUri)
            intent.putExtra(STATE, PAUSE)
            return intent
        }

        @JvmStatic
        fun createStoppedIntent(context: Context): Intent {
            val intent = Intent(context, LoggingService::class.java)
            intent.putExtra(STATE, STOP)
            return intent
        }
    }
}
