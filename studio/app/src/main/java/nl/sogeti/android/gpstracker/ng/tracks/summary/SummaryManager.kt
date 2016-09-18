package nl.sogeti.android.gpstracker.ng.tracks.summary

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import nl.sogeti.android.gpstracker.integration.ContentConstants.MetaData.*
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.NAME
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints.TIME
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints.WAYPOINTS
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R
import java.text.DateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Helps in the retrieval, create and keeping up to date of summary data
 */
object summaryManager {
    private val executor = Executors.newFixedThreadPool(numberOfThreads(), BackgroundThreadFactory())
    private val summaryCache = mutableMapOf<Uri, Summary>()
    private var isRunning = AtomicInteger()
    private val calculator = SummaryCalculator()

    fun start() {
        isRunning.incrementAndGet()
    }

    fun stop() {
        isRunning.decrementAndGet()
    }

    fun isRunning() = isRunning.get() > 0

    /**
     * Collects summary data from the meta table.
     */
    fun collectSummaryInfo(context: Context, trackUri: Uri, callbackSummary: (Summary) -> Unit) {
        val cacheHit = summaryCache[trackUri]
        if (cacheHit != null) {
            callbackSummary(cacheHit)
        } else {
            executor.submit({
                if (isRunning()) {
                    val summary = calculator.calculateSummary(context, trackUri)
                    if (isRunning()) {
                        summaryCache.put(trackUri, summary)
                        callbackSummary(summary)
                    }
                }
            })
        }
    }

    private fun numberOfThreads(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Runtime.getRuntime().availableProcessors()
        } else {
            return 2
        }
    }

    class BackgroundThreadFactory : ThreadFactory {
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