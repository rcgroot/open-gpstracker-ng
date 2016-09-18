package nl.sogeti.android.gpstracker.ng.tracks.summary

import android.content.Context
import android.database.Cursor
import android.location.Location
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R
import java.text.DateFormat
import java.util.*

class SummaryCalculator {

    private val META_FIELD_TRACK_TYPE = "SUMMARY_TYPE"

    fun calculateSummary(context: Context, trackUri: Uri): Summary {
        val waypointsUri = trackUri.append(ContentConstants.Waypoints.WAYPOINTS)

        // Defaults
        val name = trackUri.apply(context, { it.getString(ContentConstants.Tracks.NAME) }) ?: "Unknown"
        var start = context.getString(R.string.row_start_default)
        var duration = context.getString(R.string.row_duraction_default)
        var distance = context.getString(R.string.row_distance_default)
        val timestamp = 0L
        val type = R.drawable.ic_track_type_default_24dp

        // Meta-data fields
        val metadataReadOperation: (cursor: Cursor) -> Unit = {
            when (it.getString(ContentConstants.MetaData.KEY)) {
                META_FIELD_TRACK_TYPE -> convertTypeDescriptionToIcon(it.getString(ContentConstants.MetaData.VALUE))
                else -> {
                }
            }
        }
        val trackId = trackUri.lastPathSegment
        ContentConstants.MetaData.METADATA_URI.map(context, metadataReadOperation, listOf(ContentConstants.MetaData.KEY, ContentConstants.MetaData.VALUE), Pair("${ContentConstants.MetaData.TRACK} = ?", listOf(trackId.toString())))

        // Calculate
        val startTimestamp = waypointsUri.apply(context, { it.getLong(ContentConstants.Waypoints.TIME) })
        start = convertTimestampToStart(startTimestamp, start)
        val endTimestamp = waypointsUri.apply(context, { it.moveToLast();it.getLong(ContentConstants.Waypoints.TIME) })
        if (startTimestamp != null && endTimestamp != null && startTimestamp < endTimestamp) {
            duration = convertStartEndToDuration(startTimestamp, endTimestamp)
        }
        val operation = { distance: Float?, first: Waypoint, second: Waypoint
            ->
            distance(first, second) + (distance ?: 0.0F)
        }
        val calculated = trackUri.traverseTrack(context, operation)
        if (calculated != null && calculated > 1) {
            distance = convertMetersToDistance(calculated)
        }

        // Return value
        val summary = Summary(trackUri, name, type, start, duration, distance, timestamp)

        return summary
    }

    fun distance(first: Waypoint, second: Waypoint): Float {
        val result: FloatArray = floatArrayOf(0.0F)
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, result)

        return result[0]
    }

    //region Converter methods

    private fun convertMetersToDistance(meters: Float): String {
        //TODO use string resources and single/multi
        val distance: String
        if (meters > 1000) {
            distance = "%s KM".format(meters.toInt() / 1000)
        } else {
            distance = "$meters M"
        }
        return distance
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
        val duration: String
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

    //endregion

}