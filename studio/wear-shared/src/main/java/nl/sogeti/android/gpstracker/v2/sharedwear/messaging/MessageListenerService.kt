package nl.sogeti.android.gpstracker.v2.sharedwear.messaging

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatisticsMessage.Companion.PATH_STATISTICS
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatusMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatusMessage.Companion.PATH_STATUS
import timber.log.Timber

abstract class MessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        Timber.d("onMessageReceived($messageEvent: MessageEvent?")
        openMessage(messageEvent?.path, messageEvent?.data)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Timber.d("onDataChanged($dataEvents: DataEventBuffer?")
        dataEvents?.forEach {
            openMessage(it.dataItem.uri.path, it.dataItem.data)
        }
    }

    private fun openMessage(path: String?, data: ByteArray?) {
        if (path != null && data != null) {
            val dataMap = DataMap.fromByteArray(data)
            handleMessageEvents(path, dataMap)
        } else {
            Timber.w("message did not contain data")
        }
    }

    private fun handleMessageEvents(path: String?, dataMap: DataMap) =
            when (path) {
                PATH_STATISTICS ->
                    updateStatistics(StatisticsMessage(dataMap))
                PATH_STATUS ->
                    updateStatus(StatusMessage(dataMap))
                else -> {
                    Timber.e("Failed to recognize path $path")
                }
            }

    abstract fun updateStatus(status: StatusMessage)

    abstract fun updateStatistics(statistics: StatisticsMessage)
}
