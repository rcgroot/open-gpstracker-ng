package nl.sogeti.android.gpstracker.v2.sharedwear.messaging

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.StatisticsMessage.Companion.PATH_STATISTICS
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.StatusMessage.Companion.PATH_STATUS
import timber.log.Timber

abstract class MessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        Timber.d("onDataChanged($messageEvent: MessageEvent?")
        val data = messageEvent?.data
        if (data != null) {
            val dataMap = DataMap.fromByteArray(data)
            handleMessageEvents(messageEvent?.path, dataMap)
        } else {
            Timber.w("onDataChanged did not contain data")
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Timber.d("onDataChanged($dataEvents: DataEventBuffer?")
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
