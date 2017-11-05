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
package nl.sogeti.android.gpstracker.v2.wear

import android.content.Context
import android.os.Handler
import android.os.Looper
import nl.renedegroot.android.concurrent.BackgroundThreadFactory
import nl.sogeti.android.gpstracker.v2.sharedwear.*
import timber.log.Timber
import java.util.concurrent.Executors

class ControlPresenter(private val model: ControlViewModel, private val view: View) : MessageSender.MessageSenderStatus {

    var messageSender: MessageSender? = null
    var loaderCancelTask: LoaderCancelTask? = null

    fun start(context: Context) {
        val executorService = Executors.newFixedThreadPool(1, BackgroundThreadFactory("WearMessageSender"))
        messageSender = MessageSender(context, Capability.CAPABILITY_RECORD, executorService)
        messageSender?.start()
        messageSender?.messageSenderStatus = this
        messageSender?.sendMessage(StatusMessage(STATE_UNKNOWN))
        refreshStatus()
    }

    fun enterAmbient() {
        view.darken()
    }

    fun exitAmbient() {
        view.brighter()
    }

    fun stop() {
        messageSender?.messageSenderStatus = null
        messageSender?.stop()
        messageSender = null
    }

    //region View callbacks

    fun onClickSummary() {
        view.brighter()
    }

    fun pulledRefresh() {
        model.manualRefresh.set(true)
        loaderCancelTask?.shouldCancel = false
        loaderCancelTask = LoaderCancelTask(model).schedule(3)
        refreshStatus()
        messageSender?.sendMessage(StatusMessage(STATE_UNKNOWN))
    }

    fun didClickControl(control: Control) {
        model.confirmAction.set(control)
        view.startConfirmTimer()
    }

    fun didCancelControl() {
        view.cancelConfirmTimer()
        model.confirmAction.set(null)
    }

    fun confirmTimerFinished() {
        val action = model.confirmAction.get()
        model.confirmAction.set(null)
        refreshStatus()
        val actionId = action?.stringId?.get()
        when (actionId) {
            R.string.control_start -> messageSender?.sendMessage(StatusMessage(STATE_START))
            R.string.control_pause -> messageSender?.sendMessage(StatusMessage(STATE_PAUSE))
            R.string.control_resume -> messageSender?.sendMessage(StatusMessage(STATE_RESUME))
            R.string.control_stop -> messageSender?.sendMessage(StatusMessage(STATE_STOP))
            else -> {
                Timber.e("Failed to process selected action ${actionId}")
                messageSender?.sendMessage(StatusMessage(STATE_UNKNOWN))
            }
        }
    }

    //endregion

    //region Message sender callbacks

    override fun isAbleToSendMessages(isAble: Boolean) {
        if (isAble) {
            if (model.state.get()?.iconId?.get() == R.drawable.ic_sync_disabled_black_24dp) {
                refreshStatus()
            }
        } else {
            unknownState()
        }
    }

    //endregion

    //region Phone callbacks

    fun didReceiveStatistics(statisticsMessage: StatisticsMessage) {
        Timber.d("Received $statisticsMessage")
        model.averageSpeed.set(statisticsMessage.speed)
        model.duration.set(statisticsMessage.duration)
        model.distance.set(statisticsMessage.distance)
    }

    fun didReceiveStatus(statusMessage: StatusMessage) {
        Timber.d("Received $statusMessage")
        model.manualRefresh.set(false)
        loaderCancelTask?.shouldCancel = false
        when (statusMessage.status) {
            STATE_START -> startedLogging()
            STATE_PAUSE -> pausedLogging()
            STATE_STOP -> stopLogging()
            else -> unknownState()
        }
    }

    //endregion

    private fun startedLogging() {
        model.controls.clear()
        model.controls.addAll(listOf(
                Control.Pause(),
                Control.Stop()))
        model.state.set(Control.Start())
    }

    private fun pausedLogging() {
        model.controls.clear()
        model.controls.addAll(listOf(
                Control.Start(),
                Control.Stop()))
        model.state.set(Control.Pause())
    }

    private fun stopLogging() {
        model.controls.clear()
        model.controls.addAll(listOf(
                Control.Start()))
        model.state.set(Control.Stop())
    }

    private fun refreshStatus() {
        model.controls.clear()
        model.state.set(Control.Sync())
    }

    private fun unknownState() {
        model.controls.clear()
        model.state.set(Control.Disconnect())
    }

    class LoaderCancelTask(private val model: ControlViewModel) {

        var shouldCancel = true

        fun schedule(seconds: Int): LoaderCancelTask {
            Handler(Looper.getMainLooper()).postDelayed({
                if (shouldCancel) {
                    model.manualRefresh.set(false)
                }
            }, 1000L * seconds)
            return this
        }
    }
}
