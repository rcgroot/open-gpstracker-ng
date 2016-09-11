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
    val META_FIELD_TYPE = "SUMMARY_TYPE"
    private val executor = Executors.newFixedThreadPool(numberOfThreads(), BackgroundThreadFactory())
    private val summaryCache = mutableMapOf<Uri, Summary>()
    private var isRunning = AtomicInteger()

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
    fun collectSummaryInfo(context: Context, trackUri: Uri, updateSummary: (Summary) -> Unit) {
        val cacheHit = summaryCache[trackUri]
        if (cacheHit != null) {
            updateSummary(cacheHit)
        } else {
            executor.submit({
                if (isRunning()) {
                    val waypointsUri = trackUri.append(WAYPOINTS)
                    val name = trackUri.apply(context, { it.getString(NAME) }) ?: "Unknown"
                    var start = context.getString(R.string.row_start_default)
                    var duration = context.getString(R.string.row_duraction_default)
                    var distance = context.getString(R.string.row_distance_default)
                    var timestamp = 0L
                    var type = R.drawable.ic_track_type_default_24dp
                    summaryCache.put(trackUri, Summary(trackUri, name, type, start, duration, distance, timestamp))
                    val operation: (cursor: Cursor) -> Unit = {
                        when (it.getString(KEY)) {
                            META_FIELD_TYPE -> convertTypeDescriptionToIcon(it.getString(VALUE))
                            else -> {
                            }
                        }
                    }
                    val trackId = trackUri.lastPathSegment
                    METADATA_URI.map(context, operation, listOf(KEY, VALUE), Pair("$TRACK = ?", listOf(trackId.toString())))
                    val startTimestamp = waypointsUri.apply(context, { it.getLong(TIME) })
                    start = convertTimestampToStart(startTimestamp, start)
                    val endTimestamp = waypointsUri.apply(context, { it.moveToLast();it.getLong(TIME) })
                    if (startTimestamp != null && endTimestamp != null && startTimestamp < endTimestamp) {
                        duration = convertStartEndToDuration(startTimestamp, endTimestamp)
                    }
                    val summary = Summary(trackUri, name, type, start, duration, distance, timestamp)
                    summaryCache.put(trackUri, summary)
                    if (isRunning()) {
                        updateSummary(summary)
                    }
                }
            })
        }
    }

    private fun convertTimestampToStart(timestamp: Long?, default: String): String {
        val start: String
        if (timestamp != null) {
            start = DateFormat.getDateInstance().format(Date(timestamp))
        } else {
            start = default
        }

        return start
    }

    private fun convertStartEndToDuration(startTimestamp: Long, endTimestamp: Long): String {
        //TODO use string resources and single/multi
        val msPerMinute = 1000 * 60
        val msPerHour = msPerMinute * 60
        val msPerDay = msPerHour * 24
        val days = (endTimestamp - startTimestamp) / msPerDay
        val hours = ((endTimestamp - startTimestamp) - (days * msPerDay)) / msPerHour
        val minutes = ((endTimestamp - startTimestamp) - (days * msPerDay) - (hours * msPerHour)) / msPerMinute
        var duration: String
        if (days > 0) {
            duration = "$days days, $hours hours\n$minutes minutes"
        } else if (hours > 0) {
            duration = "$hours hours\n$minutes minutes"
        } else {
            duration = "$minutes minutes"
        }

        return duration
    }

    private fun convertTypeDescriptionToIcon(description: String?) {
        when (description) {
        //TODO convert more track types to icons
            else -> R.drawable.ic_track_type_default_24dp
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

data class Summary(val track: Uri, val name: String, val type: Int, val start: String, val duration: String, val distance: String, val timestamp: Long)