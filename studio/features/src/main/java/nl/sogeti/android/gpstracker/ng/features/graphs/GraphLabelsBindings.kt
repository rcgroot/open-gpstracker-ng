package nl.sogeti.android.gpstracker.ng.features.graphs

import android.databinding.BindingAdapter
import android.widget.TextView
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatting
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphLabelsBindings {

    @Inject
    lateinit var statisticsFormatting: StatisticsFormatting

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    @BindingAdapter("date")
    fun setDate(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp == 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatting.convertTimestampToDate(textView.context, timeStamp)
        }
    }

    @BindingAdapter("time")
    fun setTime(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp == 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatting.convertTimestampToTime(textView.context, timeStamp)
        }
    }

    @BindingAdapter("duration")
    fun setDuration(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatting.convertStartEndToDuration(textView.context, 0L, timeStamp)
        }
    }
}
