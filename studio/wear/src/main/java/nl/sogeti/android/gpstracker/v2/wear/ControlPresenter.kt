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
import nl.sogeti.android.gpstracker.utils.concurrent.BackgroundThreadFactory
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.MessageSender
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatusMessage
import timber.log.Timber
import java.util.concurrent.Executors

class ControlPresenter(applicationContext: Context) : HolderFragment.Holdable, MessageSender.MessageSenderStatusListener {

    val viewModel = ControlViewModel()
    private var messageSender: MessageSender? = null
    private var loaderCancelTask: LoaderCancelTask? = null

    init {
        val executorService = Executors.newFixedThreadPool(1, BackgroundThreadFactory("WearMessageSender"))
        messageSender = MessageSender(applicationContext, MessageSender.Capability.CAPABILITY_RECORD, executorService)
        messageSender?.start()
        messageSender?.messageSenderStatusListener = this

        refresh(16)
    }

    fun enterAmbient() {
        viewModel.ambient.set(true)
    }

    fun exitAmbient() {
        viewModel.ambient.set(false)
    }

    override fun onCleared() {
        super.onCleared()
        messageSender?.messageSenderStatusListener = null
        messageSender?.stop()
        messageSender = null
    }

    //region View callbacks

    fun onClickControl() {
        viewModel.scrollToPage.set(0)
    }

    fun onClickSummary() {
        viewModel.ambient.set(false)
    }

    fun pulledRefresh() {
        refresh(3)
    }

    // Using ObservableField<Control> instead of Control due to bug:
    // https://issuetracker.google.com/issues/69535017
    fun didClickControl(control: ObservableField<Control>) {
        viewModel.confirmAction.set(control.get())
    }

    fun didCancelControl() {
        viewModel.confirmAction.set(null)
        refresh(3)
    }

    fun confirmTimerFinished() {
        val action = viewModel.confirmAction.get()
        viewModel.confirmAction.set(null)
        showRefreshStatus()
        val actionId = action?.action
        when (actionId) {
            R.string.control_action_start -> {
                messageSender?.sendMessage(StatusMessage(StatusMessage.Status.START))
                viewModel.scrollToPage.set(1)
            }
            R.string.control_action_pause -> messageSender?.sendMessage(StatusMessage(StatusMessage.Status.PAUSE))
            R.string.control_action_resume -> {
                messageSender?.sendMessage(StatusMessage(StatusMessage.Status.RESUME))
                viewModel.scrollToPage.set(1)
            }
            R.string.control_action_stop -> {
                messageSender?.sendMessage(StatusMessage(StatusMessage.Status.STOP))
                viewModel.scrollToPage.set(1)
            }
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
            if (viewModel.state.get()?.iconId == R.drawable.ic_sync_disabled_black_24dp) {
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
        resumedLogging()
        viewModel.averageSpeed.set(statisticsMessage.speed)
        viewModel.duration.set(statisticsMessage.duration)
        viewModel.distance.set(statisticsMessage.distance)
    }

    fun didReceiveStatus(statusMessage: StatusMessage) {
        Timber.d("Received $statusMessage")
        loaderCancelTask?.cancel = true
        viewModel.manualRefresh.set(false)
        when (statusMessage.status) {
            StatusMessage.Status.START -> startedLogging()
            StatusMessage.Status.RESUME -> resumedLogging()
            StatusMessage.Status.PAUSE -> pausedLogging()
            StatusMessage.Status.STOP -> stopLogging()
            else -> unknownState()
        }
    }

    //endregion

    private fun refresh(timeout: Int) {
        loaderCancelTask?.cancel = true
        loaderCancelTask = LoaderCancelTask(viewModel).schedule(timeout)
        viewModel.manualRefresh.set(true)
        showRefreshStatus()
        messageSender?.sendMessage(StatusMessage(StatusMessage.Status.UNKNOWN))
    }

    private fun startedLogging() {
        resumedLogging()
        viewModel.averageSpeed.set(0F)
        viewModel.distance.set(0F)
        viewModel.duration.set(0L)
    }

    private fun pausedLogging() {
        viewModel.state.set(Control.Pause(true))
        viewModel.leftControl.set(Control.Stop(true))
        viewModel.bottomControl.set(Control.Pause(false))
        viewModel.rightControl.set(Control.Resume(true))
    }

    private fun resumedLogging() {
        viewModel.state.set(Control.Start(true))
        viewModel.leftControl.set(Control.Stop(true))
        viewModel.bottomControl.set(Control.Pause(true))
        viewModel.rightControl.set(Control.Resume(false))
    }

    private fun stopLogging() {
        viewModel.state.set(Control.Stop(true))
        viewModel.leftControl.set(Control.Stop(false))
        viewModel.bottomControl.set(Control.Pause(false))
        viewModel.rightControl.set(Control.Start(true))
    }

    private fun showRefreshStatus() {
        viewModel.state.set(Control.Sync())
    }

    private fun unknownState() {
        viewModel.state.set(Control.Disconnect())
        viewModel.leftControl.set(Control.Stop(false))
        viewModel.bottomControl.set(Control.Pause(false))
        viewModel.rightControl.set(Control.Start(false))
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
