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
package nl.sogeti.android.gpstracker.v2.sharedwear

import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityApi
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

enum class Capability(val itemName: String) {
    CAPABILITY_CONTROL("gps_track_control"),
    CAPABILITY_RECORD("gps_track_record")
}

class MessageSender(private val context: Context, private val capability: Capability, private val executor: Executor, private val queueSize: Int = 3) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, CapabilityApi.CapabilityListener {

    private lateinit var client: GoogleApiClient
    private val messageQueue = ConcurrentLinkedQueue<WearMessage>()
    private var _connected = false
    val connected: Boolean
        get() = _connected
    private var nodeId: String? = null
    var messageSenderStatusListener: MessageSenderStatusListener? = null

    fun start() {
        client = GoogleApiClient.Builder(context, this, this)
                .addApi(Wearable.API)
                .build()
        client.connect()
    }

    fun stop() {
        Wearable.CapabilityApi.removeCapabilityListener(client, this, capability.itemName)
        client.disconnect()
    }

    fun sendMessage(message: WearMessage) {
        Timber.d("Queueing message $message")
        messageQueue.add(message)
        if (messageQueue.size > queueSize) {
            Timber.d("Queue larger then $queueSize trimming oldest message")
            messageQueue.poll()
        }
        runMessageQueue()
    }

    private fun runMessageQueue() {
        executor.execute {
            while (_connected && messageQueue.isNotEmpty()) {
                checkForNode(capability)
                if (nodeId != null) {
                    val message: WearMessage = messageQueue.poll() ?: continue
                    val path = message.path
                    val data = message.toDataMap().toByteArray()
                    val result = Wearable.MessageApi.sendMessage(client, nodeId, path, data).await()
                    if (result.status.isSuccess) {
                        Timber.d("Successful sent message $result")
                        messageSenderStatusListener?.didConnect(true)
                    } else {
                        Timber.d("Failed to sent message $result")
                        messageSenderStatusListener?.didConnect(false)
                    }
                } else {
                    messageSenderStatusListener?.didConnect(false)
                    Timber.d("Did not have node with capability $capability")
                    break
                }
            }
        }
    }

    private fun checkForNode(receiver: Capability) {
        if (nodeId == null) {
            val capabilityResult = Wearable.CapabilityApi
                    .getCapability(client,
                            receiver.itemName,
                            CapabilityApi.FILTER_REACHABLE)
                    .await()
            updateTranscriptionCapability(capabilityResult.capability)
        }
    }

    private fun updateTranscriptionCapability(info: CapabilityInfo) {
        val node = info.nodes.firstOrNull {
            it.isNearby
        }

        nodeId = node?.id ?: info.nodes.lastOrNull()?.id
    }

    //region GoogleApiClient callbacks

    override fun onConnected(bundle: Bundle?) {
        _connected = true
        Timber.e("onConnected")
        Wearable.CapabilityApi.addCapabilityListener(
                client,
                this,
                capability.itemName)
        runMessageQueue()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        _connected = false
        Timber.e("Connection failed, reason $result")
    }

    override fun onConnectionSuspended(cause: Int) {
        _connected = false
        Timber.w("Connection suspended, reason $cause")
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        Timber.w("onCapabilityChanged $info")
        updateTranscriptionCapability(info)
        if (nodeId == null) {
            messageSenderStatusListener?.didConnect(false)
        } else {
            messageSenderStatusListener?.didConnect(true)
            runMessageQueue()
        }

    }

    //endregion

    interface MessageSenderStatusListener {
        fun didConnect(connect: Boolean)
    }
}
