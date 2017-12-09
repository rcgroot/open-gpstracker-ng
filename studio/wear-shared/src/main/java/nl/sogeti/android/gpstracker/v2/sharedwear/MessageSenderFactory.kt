package nl.sogeti.android.gpstracker.v2.sharedwear

import android.content.Context
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

class MessageSenderFactory {

    fun createMessageSender(context: Context, capability: Capability, executor: Executor) = MessageSender(context, capability, executor)
}
