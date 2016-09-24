package nl.sogeti.android.gpstracker.ng.tracks.summary

import android.content.Context
import android.net.Uri
import android.os.Build
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * Helps in the retrieval, create and keeping up to date of summary data
 */
object summaryManager {
    var executor: ExecutorService? = null
    val calculator = SummaryCalculator()
    val summaryCache = mutableMapOf<Uri, Summary>()
    var activeCount = 0

    fun start() {
        synchronized(this, {
            activeCount++
            if (executor == null) {
                executor = Executors.newFixedThreadPool(numberOfThreads(), BackgroundThreadFactory())
            }
        })
    }

    fun stop() {
        synchronized(this, {
            activeCount--
            if (!isRunning()) {
                executor?.shutdown()
                executor = null
            }
            if (activeCount < 0) {
                activeCount++
                throw IllegalStateException("Received more stops then starts")
            }
        })
    }

    fun isRunning(): Boolean = synchronized(this, { activeCount > 0 })

    /**
     * Collects summary data from the meta table.
     */
    fun collectSummaryInfo(context: Context, trackUri: Uri,
                           callbackSummary: (Summary) -> Unit) {
        if (!isRunning()) {
            return
        }
        val cacheHit = summaryCache[trackUri]
        if (cacheHit != null) {
            callbackSummary(cacheHit)
        } else {
            executor?.submit({
                executeTrackCalculation(context, trackUri, callbackSummary)
            })
        }
    }

    fun executeTrackCalculation(context: Context, trackUri: Uri, callbackSummary: (Summary) -> Unit) {
        if (isRunning()) {
            val summary = calculator.calculateSummary(context, trackUri)
            if (isRunning()) {
                summaryCache.put(trackUri, summary)
                callbackSummary(summary)
            }
        }
    }

    fun numberOfThreads(): Int {
        val threads: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            threads = Runtime.getRuntime().availableProcessors()
        } else {
            threads = 2
        }

        return threads
    }

    internal class BackgroundThreadFactory : ThreadFactory {
        val group = ThreadGroup("SummaryManager")

        init {
            group.isDaemon = false
            group.maxPriority = android.os.Process.THREAD_PRIORITY_BACKGROUND
        }

        override fun newThread(task: Runnable?): Thread {
            val thread = Thread(group, task)

            return thread
        }
    }
}