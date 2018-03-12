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
package nl.sogeti.android.gpstracker.ng.features.trackedit

import android.content.Context
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.opengpstrack.ng.features.R
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class NameGenerator @Inject constructor(@Named("dayFormatter") private val dayFormat: SimpleDateFormat,
                                        private val locationFactory: LocationFactory) {

    fun generateName(context: Context, now: Calendar): String {
        val today = dayFormat.format(now.time)
        val period = period(context, now)
        val location = locationFactory.getLocationName()

        return if (location == null) {
            context.getString(R.string.initial_time_track_name, today, period)
        } else {
            context.getString(R.string.initial_time_location_track_name, today, period, location)
        }
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
