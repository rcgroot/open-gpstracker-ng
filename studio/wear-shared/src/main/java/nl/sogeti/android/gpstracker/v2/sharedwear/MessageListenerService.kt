package nl.sogeti.android.gpstracker.v2.sharedwear

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

abstract class MessageListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent?) {
        val dataMap = DataMap.fromByteArray(messageEvent?.data)
        Timber.d("onDataChanged($dataMap: DataEventBuffer?")
        handleDataEvents(messageEvent?.path, dataMap)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Timber.d("onDataChanged($dataEvents: DataEventBuffer?")
//        val googleApiClient = GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .build()
//        val connectionResult = googleApiClient.blockingConnect(30, TimeUnit.SECONDS)
//        if (!connectionResult.isSuccess) {
//            Timber.e("Failed to connect to GoogleApiClient.")
//            return
//        }
    }

    private fun handleDataEvents(path: String?, dataMap: DataMap) {
        when (path) {
            PATH_STATISTICS ->
                updateStatistics(StatisticsMessage(dataMap))
            PATH_STATUS ->
                updateStatus(StatusMessage(dataMap))
        }
    }

    abstract fun updateStatus(status: StatusMessage)

    abstract fun updateStatistics(statistics: StatisticsMessage)
}
