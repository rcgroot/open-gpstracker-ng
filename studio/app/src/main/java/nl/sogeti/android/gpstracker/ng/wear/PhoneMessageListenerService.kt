/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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

import nl.renedegroot.android.concurrent.ExecutorFactory
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.trackedit.NameGenerator
import nl.sogeti.android.gpstracker.v2.sharedwear.*
import java.util.*
import java.util.concurrent.ExecutorService
import javax.inject.Inject

class PhoneMessageListenerService : MessageListenerService() {

    @Inject
    lateinit var nameGenerator: NameGenerator
    @Inject
    lateinit var messageSenderFactory: MessageSenderFactory
    @Inject
    lateinit var executorFactory: ExecutorFactory
    @Inject
    lateinit var serviceManager: ServiceManagerInterface
    private val executor: ExecutorService
    private var messageSender: MessageSender? = null

    init {
        GpsTrackerApplication.appComponent.inject(this)
        executor = executorFactory.createExecutor(1, "WearMessageSender")
    }

    override fun onCreate() {
        super.onCreate()
        messageSender = messageSenderFactory.createMessageSender(this, Capability.CAPABILITY_CONTROL, executor)
        messageSender?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageSender?.stop()
        messageSender = null
    }

    override fun updateStatus(event: StatusMessage) {
        when (event.status) {
            STATE_START -> startLogging()
            STATE_PAUSE -> pauseLogging()
            STATE_RESUME -> resumeLogging()
            STATE_STOP -> stopLogging()
            STATE_UNKNOWN -> respondLoggingState()
        }
    }

    override fun updateStatistics(event: StatisticsMessage) {
        // Not registered as intent filter
    }

    private fun startLogging() {
        val generatedName = nameGenerator.generateName(this, Calendar.getInstance())
        serviceManager.startGPSLogging(this, generatedName)
        respondLoggingState()
    }

    private fun pauseLogging() {
        serviceManager.pauseGPSLogging(this)
        respondLoggingState()
    }

    private fun resumeLogging() {
        serviceManager.resumeGPSLogging(this)
        respondLoggingState()
    }

    private fun stopLogging() {
        serviceManager.stopGPSLogging(this)
        respondLoggingState()
    }

    private fun respondLoggingState() {
        serviceManager.startup(this) {
            when (serviceManager.loggingState) {
                ServiceConstants.STATE_LOGGING -> messageSender?.sendMessage(StatusMessage(STATE_START))
                ServiceConstants.STATE_PAUSED -> messageSender?.sendMessage(StatusMessage(STATE_PAUSE))
                ServiceConstants.STATE_STOPPED -> messageSender?.sendMessage(StatusMessage(STATE_STOP))
                ServiceConstants.STATE_UNKNOWN -> messageSender?.sendMessage(StatusMessage(STATE_UNKNOWN))
            }
            serviceManager.shutdown(this)
        }
    }
}
