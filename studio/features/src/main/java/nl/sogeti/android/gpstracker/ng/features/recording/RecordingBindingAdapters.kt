/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.features.recording

import android.databinding.BindingAdapter
import android.support.v7.content.res.AppCompatResources
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration

import nl.sogeti.android.gpstracker.ng.features.databinding.CommonBindingAdapters
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class RecordingBindingAdapters : CommonBindingAdapters() {

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    @BindingAdapter("isRecording")
    fun setRecording(container: ViewGroup, isRecording: Boolean?) {
        if (isRecording == true) {
            container.visibility = View.VISIBLE
            container.animate().translationY(0f).start()
        } else {
            container.animate().translationY((-container.height).toFloat()).withEndAction { container.visibility = View.GONE }.start()
        }
    }

    @BindingAdapter("gps_signal_quality")
    fun setGpsSignalQuality(imageView: ImageView, quality: Int?) {
        when (quality) {
            0 -> imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_satellite_none))
            1 -> imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_satellite_low))
            2 -> imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_satellite_medium))
            3 -> imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_satellite_high))
            4 -> imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_satellite_full))
            else -> imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_satellite_none))
        }
    }

    @BindingAdapter("android:text")
    fun setSummary(textView: TextView, summary: SummaryText?) {
        if (summary == null) {
            textView.text = ""
        }
        else {
            val context = textView.context
            val speed = statisticsFormatter.convertMeterPerSecondsToSpeed(context, summary.meterPerSecond, summary.isRunners)
            val distance = statisticsFormatter.convertMetersToDistance(context, summary.meters)
            val duration = statisticsFormatter.convertSpanDescriptiveDuration(context, summary.msDuration)
            textView.text = context.getString(summary.string, distance, duration, speed)
        }
    }

    @BindingAdapter("android:text")
    fun setIntegerText(textView: TextView, textId: Int?) {
        if (textId == null) {
            textView.text = ""
        } else {
            textView.text = textView.resources.getText(textId)
        }

    }
}
