package nl.sogeti.android.gpstracker.utils.concurrent

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExecutorFactory {
    fun createExecutor(threads: Int, name: String): ExecutorService =
            Executors.newFixedThreadPool(threads, BackgroundThreadFactory(name))
}
