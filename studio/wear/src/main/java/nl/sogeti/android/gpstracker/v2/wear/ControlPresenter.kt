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
import android.databinding.ObservableField
import android.os.Handler
import android.os.Looper
import nl.renedegroot.android.concurrent.BackgroundThreadFactory
import nl.sogeti.android.gpstracker.v2.sharedwear.*
import timber.log.Timber
import java.util.concurrent.Executors

class ControlPresenter(private val model: ControlViewModel, private val view: View) : MessageSender.MessageSenderStatusListener {

    private var messageSender: MessageSender? = null
    private var loaderCancelTask: LoaderCancelTask? = null

    fun start(context: Context) {
        val executorService = Executors.newFixedThreadPool(1, BackgroundThreadFactory("WearMessageSender"))
        messageSender = MessageSender(context, Capability.CAPABILITY_RECORD, executorService)
        messageSender?.start()
        messageSender?.messageSenderStatusListener = this

        refresh(8)
    }

    fun enterAmbient() {
        view.darken()
    }

    fun exitAmbient() {
        view.brighter()
    }

    fun stop() {
        messageSender?.messageSenderStatusListener = null
        messageSender?.stop()
        messageSender = null
    }

    //region View callbacks

    fun onClickSummary() {
        view.brighter()
    }

    fun pulledRefresh() {
        refresh(3)
    }

    // Using ObservableField<Control> instead of Control due to bug:
    // https://issuetracker.google.com/issues/69535017
    fun didClickControl(control: ObservableField<Control>) {
        model.confirmAction.set(control.get())
        view.startConfirmTimer()
    }

    fun didCancelControl() {
        view.cancelConfirmTimer()
        model.confirmAction.set(null)
        refresh(3)
    }

    fun confirmTimerFinished() {
        val action = model.confirmAction.get()
        model.confirmAction.set(null)
        showRefreshStatus()
        val actionId = action?.action
        when (actionId) {
            R.string.control_action_start -> messageSender?.sendMessage(StatusMessage(StatusMessage.Status.START))
            R.string.control_action_pause -> messageSender?.sendMessage(StatusMessage(StatusMessage.Status.PAUSE))
            R.string.control_action_resume -> messageSender?.sendMessage(StatusMessage(StatusMessage.Status.RESUME))
            R.string.control_action_stop -> messageSender?.sendMessage(StatusMessage(StatusMessage.Status.STOP))
            else -> {
                Timber.e("Failed to process selected action $actionId")
                messageSender?.sendMessage(StatusMessage(StatusMessage.Status.UNKNOWN))
            }
        }
    }

    //endregion

    //region Message sender callbacks

    override fun didConnect(connect: Boolean) {
        if (connect) {
            if (model.state.get()?.iconId == R.drawable.ic_sync_disabled_black_24dp) {
                showRefreshStatus()
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
        startedLogging()
    }

    fun didReceiveStatus(statusMessage: StatusMessage) {
        Timber.d("Received $statusMessage")
        loaderCancelTask?.cancel = true
        model.manualRefresh.set(false)
        when (statusMessage.status) {
            StatusMessage.Status.START -> startedLogging()
            StatusMessage.Status.PAUSE -> pausedLogging()
            StatusMessage.Status.STOP -> stopLogging()
            else -> unknownState()
        }
    }

    //endregion

    private fun refresh(timeout: Int) {
        loaderCancelTask?.cancel = true
        loaderCancelTask = LoaderCancelTask(model).schedule(timeout)
        model.manualRefresh.set(true)
        showRefreshStatus()
        messageSender?.sendMessage(StatusMessage(StatusMessage.Status.UNKNOWN))
    }

    private fun startedLogging() {
        model.state.set(Control.Start(true))
        model.leftControl.set(Control.Stop(true))
        model.bottomControl.set(Control.Pause(true))
        model.rightControl.set(Control.Resume(false))
    }

    private fun pausedLogging() {
        model.state.set(Control.Pause(true))
        model.leftControl.set(Control.Stop(true))
        model.bottomControl.set(Control.Pause(false))
        model.rightControl.set(Control.Resume(true))
    }

    private fun stopLogging() {
        model.state.set(Control.Stop(true))
        model.leftControl.set(Control.Stop(false))
        model.bottomControl.set(Control.Pause(false))
        model.rightControl.set(Control.Start(true))
    }

    private fun showRefreshStatus() {
        model.state.set(Control.Sync())
    }

    private fun unknownState() {
        model.state.set(Control.Disconnect())
        model.leftControl.set(Control.Stop(false))
        model.bottomControl.set(Control.Pause(false))
        model.rightControl.set(Control.Start(false))
    }

    class LoaderCancelTask(private val model: ControlViewModel) {
        var cancel = false

        fun schedule(seconds: Int): LoaderCancelTask {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!cancel) {
                    model.state.set(Control.Disconnect())
                    model.manualRefresh.set(false)
                }

            }, 1000L * seconds)
            return this
        }

    }
}
