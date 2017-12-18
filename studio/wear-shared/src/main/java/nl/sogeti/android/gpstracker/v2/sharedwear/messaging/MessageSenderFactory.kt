package nl.sogeti.android.gpstracker.v2.sharedwear.messaging

import android.content.Context
import java.util.concurrent.Executor

class MessageSenderFactory {

    fun createMessageSender(context: Context, capability: MessageSender.Capability, executor: Executor) = MessageSender(context, capability, executor)
}
