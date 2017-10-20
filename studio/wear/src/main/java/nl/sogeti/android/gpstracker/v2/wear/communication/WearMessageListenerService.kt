package nl.sogeti.android.gpstracker.v2.wear.communication

import com.google.android.gms.wearable.DataMapItem
import nl.sogeti.android.gpstracker.v2.sharedwear.MessageListenerService
import nl.sogeti.android.gpstracker.v2.sharedwear.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.StatusMessage
import nl.sogeti.android.gpstracker.v2.wear.ControlActivity

class WearMessageListenerService : MessageListenerService() {

    override fun updateStatus(status: StatusMessage) {
        val intent = ControlActivity.createIntent(this, status)
        startActivity(intent)
    }

    override fun updateStatistics(statistics: StatisticsMessage) {
        val intent = ControlActivity.createIntent(this, statistics)
        startActivity(intent)
    }
}
