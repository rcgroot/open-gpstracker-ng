package nl.sogeti.android.gpstracker.ng.trackedit

import android.content.Context
import nl.sogeti.android.gpstracker.ng.track.map.LocationFactory
import nl.sogeti.android.gpstracker.v2.R
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class NameGenerator @Inject constructor(@Named("dayFormatter") private val dayFormat: SimpleDateFormat,
                                        private val locationFactory: LocationFactory) {

    fun generateName(context: Context, now: Calendar): String {
        val today = dayFormat.format(now.time)
        val period = period(context, now)
        val location = locationFactory.getLocationName(context)
        var name: String
        if (location == null) {
            name = context.getString(R.string.initial_time_track_name, today, period)
        } else {
            name = context.getString(R.string.initial_time_location_track_name, today, period, location)
        }

        return name
    }

    private fun period(context: Context, now: Calendar): String {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..4 -> context.getString(R.string.period_night)
            in 5..11 -> context.getString(R.string.period_morning)
            in 12..17 -> context.getString(R.string.period_afternoon)
            in 18..23 -> context.getString(R.string.period_evening)
            else -> ""
        }
    }
}
