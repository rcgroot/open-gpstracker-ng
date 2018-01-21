package nl.sogeti.android.gpstracker.utils.concurrent

import java.util.concurrent.ThreadFactory

class BackgroundThreadFactory(name: String) : ThreadFactory {
    private val group = ThreadGroup(name)

    init {
        group.isDaemon = false
        group.maxPriority = android.os.Process.THREAD_PRIORITY_BACKGROUND
    }

    override fun newThread(task: Runnable?) = Thread(group, task)
}
