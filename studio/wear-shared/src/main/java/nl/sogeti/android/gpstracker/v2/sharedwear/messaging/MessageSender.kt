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
package nl.sogeti.android.gpstracker.v2.sharedwear.messaging

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import nl.sogeti.android.gpstracker.v2.sharedwear.util.trying
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor


class MessageSender(
        private val context: Context,
        private val capability: Capability,
        private val executor: Executor,
        private val queueSize: Int = 3)
    :
        CapabilityClient.OnCapabilityChangedListener {

    private val messageQueue = ConcurrentLinkedQueue<WearMessage>()
    val connected: Boolean
        get() = recentNodes.isNotEmpty()
    private var recentNodes: List<String> by trying {
        try {
            val capabilityInfo = Tasks.await(Wearable.getCapabilityClient(context).getCapability(capability.itemName, CapabilityClient.FILTER_REACHABLE))
            findCapabilityNodeId(capabilityInfo)
        } catch (exception: ExecutionException) {
            emptyList<String>()
        }
    }
    var messageSenderStatusListener: MessageSenderStatusListener? = null

    fun start() {
        startNodeIdCapabilityListener()
    }

    fun stop() {
        stopNodeIdCapabilityListener()
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
            val currentNode = recentNodes
            if (currentNode.isNotEmpty()) {
                while (messageQueue.isNotEmpty()) {
                    val message: WearMessage = messageQueue.poll() ?: continue
                    val path = message.path
                    val data = message.toDataMap().toByteArray()
                    try {
                        val results = currentNode.map {
                            Tasks.await(Wearable.getMessageClient(context).sendMessage(it, path, data))
                        }
                        Timber.d("Successful sent message $results")
                        messageSenderStatusListener?.didConnect(true)
                    } catch (e: ApiException) {
                        Timber.d(e, "Failed to sent message")
                        messageSenderStatusListener?.didConnect(false)
                        break
                    }
                }
            } else {
                messageSenderStatusListener?.didConnect(false)
                Timber.d("Did not have node with capability $capability")
            }
        }
    }

    private fun startNodeIdCapabilityListener() {
        Wearable.getCapabilityClient(context).addListener(this, capability.itemName)
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        Timber.w("onCapabilityChanged $info")
        val currentNearbyNodes = findCapabilityNodeId(info)
        this.recentNodes = currentNearbyNodes

        if (currentNearbyNodes.isNotEmpty()) {
            messageSenderStatusListener?.didConnect(false)
        } else {
            messageSenderStatusListener?.didConnect(true)
            runMessageQueue()
        }
    }

    private fun stopNodeIdCapabilityListener() {
        Wearable.getCapabilityClient(context).removeListener(this)
    }

    private fun findCapabilityNodeId(capabilityInfo: CapabilityInfo): List<String> {
        val nearbyNodes = capabilityInfo.nodes.filter {
            it.isNearby
        }

        return nearbyNodes.map { it.id }
    }

    enum class Capability(val itemName: String) {
        CAPABILITY_CONTROL("gps_track_control"),
        CAPABILITY_RECORD("gps_track_record")
    }

    interface MessageSenderStatusListener {
        fun didConnect(connect: Boolean)
    }
}
