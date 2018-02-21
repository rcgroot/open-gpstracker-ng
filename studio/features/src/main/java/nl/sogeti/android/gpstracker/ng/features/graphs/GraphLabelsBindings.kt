package nl.sogeti.android.gpstracker.ng.features.graphs

import android.databinding.BindingAdapter
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import android.widget.TextView
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphLabelsBindings {

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    @BindingAdapter("date")
    fun setDate(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp == 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatter.convertTimestampToDate(textView.context, timeStamp)
        }
    }

    @BindingAdapter("time")
    fun setTime(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp == 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatter.convertTimestampToTime(textView.context, timeStamp)
        }
    }

    @BindingAdapter("timeSpan")
    fun setTimeSpan(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp == 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatter.convertTimestampToTime(textView.context, timeStamp)
        }
    }

    @BindingAdapter("duration")
    fun setDuration(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatter.convertSpanDescriptiveDuration(textView.context, timeStamp)
        }
    }

    @BindingAdapter("distance")
    fun setDistance(textView: TextView, distance: Float?) {
        if (distance== null || distance <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatter.convertMetersToDistance(textView.context, distance)
        }
    }

    @BindingAdapter("speed")
    fun setSpeed(textView: TextView, speed: Float?) {
        if (speed == null || speed <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatter.convertMeterPerSecondsToSpeed(textView.context, speed)

        }
    }

    @BindingAdapter("x_unit")
    fun setXUnit(graph: LineGraph, @StringRes id: Int) {
        graph.xUnit = graph.context.getString(id)
    }

    @BindingAdapter("y_unit")
    fun setYUnit(graph: LineGraph, @StringRes id: Int) {
        graph.yUnit = graph.context.getString(id)
    }
}
