package nl.sogeti.android.gpstracker.v2.sharedwear.util

import android.content.Context
import android.content.res.Resources
import android.support.annotation.StringRes
import android.text.format.DateFormat
import nl.sogeti.android.gpstracker.v2.sharedwear.R
import java.util.*
import kotlin.math.floor

class StatisticsFormatter(private val localeProvider: LocaleProvider, private val timeSpanUtil: TimeSpanCalculator) {

    fun convertMetersToDistance(context: Context, meters: Float): String {
        val distance: String
        distance = when {
            meters >= 100000 -> {
                val convert = context.resources.getFloat(R.string.m_to_big_distance)
                context.getString(R.string.format_big_100_kilometer).format(localeProvider.getLocale(), meters / convert)
            }
            meters >= 1000 -> {
                val convert = context.resources.getFloat(R.string.m_to_big_distance)
                context.getString(R.string.format_big_kilometer).format(localeProvider.getLocale(), meters / convert)
            }
            meters >= 100 -> {
                val convert = context.resources.getFloat(R.string.m_to_small_distance)
                context.getString(R.string.format_small_100_meters).format(localeProvider.getLocale(), meters / convert)
            }
            meters > 0 -> {
                val convert = context.resources.getFloat(R.string.m_to_small_distance)
                context.getString(R.string.format_small_meters).format(localeProvider.getLocale(), meters / convert)
            }
            else -> {
                context.getString(R.string.empty_dash)
            }
        }
        return distance
    }

    fun convertTimestampToStart(context: Context, timestamp: Long?): String {
        val start: CharSequence
        if (timestamp == null || timestamp == 0L) {
            start = context.getString(R.string.empty_dash)
        } else {
            start = timeSpanUtil.getRelativeTimeSpanString(timestamp)
        }

        return start.toString()
    }


    fun convertSpanToCompactDuration(context: Context, msDuration: Long): String {
        if (msDuration == 0L) {
            return context.getString(R.string.empty_dash)
        }
        val days = (msDuration / msPerDay).toInt()
        val hours = ((msDuration - (days * msPerDay)) / msPerHour).toInt()
        val minutes = ((msDuration - (days * msPerDay) - (hours * msPerHour)) / msPerMinute).toInt()
        val seconds = ((msDuration - (days * msPerDay) - (hours * msPerHour) - (minutes * msPerMinute)) / msPerSecond).toInt()

        val result = if (days > 0) {
            context.getString(R.string.days_compact, days, hours)
        } else {
            if (hours > 0) {
                context.getString(R.string.hours_compact, hours, minutes)
            } else {
                context.getString(R.string.minutes_compact, minutes, seconds)
            }
        }

        return result
    }

    fun convertSpanDescriptiveDuration(context: Context, msDuration: Long): String {
        if (msDuration == 0L) {
            return context.getString(R.string.empty_dash)
        }
        val days = (msDuration / msPerDay).toInt()
        val hours = ((msDuration - (days * msPerDay)) / msPerHour).toInt()
        val minutes = ((msDuration - (days * msPerDay) - (hours * msPerHour)) / msPerMinute).toInt()
        val seconds = ((msDuration - (days * msPerDay) - (hours * msPerHour) - (minutes * msPerMinute)) / msPerSecond).toInt()
        var duration: String
        if (days > 0) {
            duration = context.resources.getQuantityString(R.plurals.track_duration_days, days, days)
            if (hours > 0) {
                duration += " "
                duration += context.resources.getQuantityString(R.plurals.track_duration_hours, hours, hours)
            }
        } else if (hours > 0) {
            duration = context.resources.getQuantityString(R.plurals.track_duration_hours, hours, hours)
            if (minutes > 0) {
                duration += " "
                duration += context.resources.getQuantityString(R.plurals.track_duration_minutes, minutes, minutes)
            }
        } else if (minutes > 0) {
            duration = context.resources.getQuantityString(R.plurals.track_duration_minutes, minutes, minutes)
        } else {
            duration = context.resources.getQuantityString(R.plurals.track_duration_seconds, seconds, seconds)
        }

        return duration
    }

    fun convertMeterPerSecondsToSpeed(context: Context, meterPerSecond: Float, runners: Boolean = true): String {
        return if (meterPerSecond > 0) {
            if (runners) {
                val conversion = context.resources.getFloat(R.string.spm_to_speed)
                val runnerSpeed = (1F / meterPerSecond) / conversion
                val minutes = floor(runnerSpeed)
                val seconds = (runnerSpeed - minutes) * 60
                val unit = context.resources.getString(R.string.speed_runners_unit)
                context.getString(R.string.format_runners_speed).format(localeProvider.getLocale(), minutes, seconds, unit)
            } else {
                val conversion = context.resources.getFloat(R.string.mps_to_speed)
                val speed = meterPerSecond * conversion
                val unit = context.resources.getString(R.string.speed_unit)
                context.getString(R.string.format_speed).format(localeProvider.getLocale(), speed, unit)
            }
        } else {
            context.getString(R.string.empty_dash)
        }
    }

    fun convertTimestampToDate(context: Context, startTimestamp: Long): String {
        val date = Date(startTimestamp)
        return DateFormat.getDateFormat(context).format(date)
    }

    fun convertTimestampToTime(context: Context, startTimestamp: Long): String {
        val date = Date(startTimestamp)
        return DateFormat.getTimeFormat(context).format(date)
    }


    private fun Resources.getFloat(@StringRes resourceId: Int): Float {
        val stringValue = this.getString(resourceId)
        return stringValue.toFloat()
    }

    companion object {
        private const val msPerMinute = 1000L * 60L
        private const val msPerHour = msPerMinute * 60L
        private const val msPerDay = msPerHour * 24L
        private const val msPerSecond = 1000L
    }
}
