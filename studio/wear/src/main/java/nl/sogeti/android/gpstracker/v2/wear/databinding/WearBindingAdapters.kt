/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.v2.wear.databinding

import android.databinding.BindingAdapter
import android.support.v4.widget.SwipeRefreshLayout
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.ImageView
import android.widget.TextView
import nl.sogeti.android.gpstracker.v2.sharedwear.util.LocaleProvider
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.gpstracker.v2.sharedwear.util.TimeSpanCalculator
import nl.sogeti.android.gpstracker.v2.wear.Control
import nl.sogeti.android.gpstracker.v2.wear.R

class WearBindingAdapters {

    private val statisticsFormatting by lazy {
        StatisticsFormatter(LocaleProvider(), TimeSpanCalculator())
    }

    @BindingAdapter("android:src")
    fun setImageResource(imageView: ImageView, resId: Int) {
        imageView.setImageResource(resId)
    }

    @BindingAdapter("android:src")
    fun setControl(view: ImageView, control: Control?) {
        val id = control?.iconId
        if (id != null) {
            view.setImageResource(id)
            view.isEnabled = control.enabled
            view.alpha = if (control.enabled) 1.0F else 0.5F
        } else {
            view.setImageDrawable(null)
            view.isEnabled = false
            view.alpha = 0.5F
        }
    }

    @BindingAdapter("enabled")
    fun setEnables(view: SwipeRefreshLayout, enabled: Boolean) {
        view.isEnabled = enabled
    }

    @BindingAdapter("duration")
    fun setDuration(textView: TextView, timeStamp: Long?) {
        if (timeStamp == null || timeStamp <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatting.convertSpanToCompactDuration(textView.context, timeStamp)
                    .replace(' ', '\n')
                    .asSmallLetterSpans()
        }
    }

    @BindingAdapter("distance")
    fun setDistance(textView: TextView, distance: Float?) {
        if (distance == null || distance <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatting.convertMetersToDistance(textView.context, distance)
                    .replace(' ', '\n')
                    .asSmallLetterSpans()
        }
    }

    @BindingAdapter("speed", "inverse")
    fun setSpeed(textView: TextView, speed: Float?, inverse: Boolean?) {
        if (speed == null || speed <= 0L) {
            textView.text = textView.context.getText(R.string.empty_dash)
        } else {
            textView.text = statisticsFormatting.convertMeterPerSecondsToSpeed(textView.context, speed, inverse ?: false)
                    .replace(' ', '\n')
                    .asSmallLetterSpans()

        }
    }

    private fun String.asSmallLetterSpans(): SpannableString {
        val spannable = SpannableString(this)
        var start: Int? = null
        for (i in 0 until this.length) {
            if (!this[i].isDigit() && start == null) {
                start = i
            }
            if (this[i].isDigit() && start != null) {
                spannable.setSpan(RelativeSizeSpan(0.5f), start, i, 0)
                start = null
            }
        }
        if (start != null) {
            spannable.setSpan(RelativeSizeSpan(0.5f), start, this.length, 0)
        }

        return spannable
    }
}
