package nl.sogeti.android.gpstracker.v2.sharedwear.datasync

import android.content.Context
import com.google.android.gms.wearable.*
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatusMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.WearMessage


class DataSender(
        private val context: Context) :
        DataClient.OnDataChangedListener {

    private var updateListener: DataUpdateListener? = null

    fun updateMessage(message: WearMessage, urgent: Boolean = false) {
        val putDataMapReq = PutDataMapRequest.create(message.path)
        putDataMapReq.dataMap.putAll(message.toDataMap())
        if (urgent) {
            putDataMapReq.setUrgent()
        }
        val putDataReq = putDataMapReq.asPutDataRequest()
        Wearable.getDataClient(context).putDataItem(putDataReq)
    }

    fun start(updateListener: DataUpdateListener) {
        this.updateListener = updateListener
        startDataListener()
    }

    fun stop() {
        stopDataListener()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach {
            when (it.dataItem.uri.path) {
                StatusMessage.PATH_STATUS -> {
                    val status = StatusMessage(DataMapItem.fromDataItem(it.dataItem).dataMap)
                    updateListener?.onStatusUpdate(status)
                }
                StatisticsMessage.PATH_STATISTICS -> {
                    val status = StatisticsMessage(DataMapItem.fromDataItem(it.dataItem).dataMap)
                    updateListener?.onStatisticsUpdate(status)
                }
            }
        }
    }

    private fun startDataListener() {
        Wearable.getDataClient(context).addListener(this)

    }

    private fun stopDataListener() {
        Wearable.getDataClient(context).removeListener(this)
    }

    interface DataUpdateListener {
        fun onStatusUpdate(status: StatusMessage)
        fun onStatisticsUpdate(status: StatisticsMessage)

    }

}
