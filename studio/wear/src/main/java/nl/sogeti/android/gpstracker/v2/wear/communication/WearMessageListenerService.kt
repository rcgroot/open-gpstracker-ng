package nl.sogeti.android.gpstracker.v2.wear.communication

import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.MessageListenerService
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.StatusMessage
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
