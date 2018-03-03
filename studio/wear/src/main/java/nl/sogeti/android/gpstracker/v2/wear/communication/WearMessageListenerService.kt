package nl.sogeti.android.gpstracker.v2.wear.communication

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.MessageListenerService
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatusMessage
import nl.sogeti.android.gpstracker.v2.wear.ControlActivity

class WearMessageListenerService : MessageListenerService() {

    override fun updateStatus(status: StatusMessage) {
        val intent = ControlActivity.createIntent(this, status)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun updateStatistics(statistics: StatisticsMessage) {
        val intent = ControlActivity.createIntent(this, statistics)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
