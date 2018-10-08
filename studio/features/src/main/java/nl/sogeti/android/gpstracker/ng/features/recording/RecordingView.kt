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

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.features.util.SingleLiveEvent
import nl.sogeti.android.opengpstrack.ng.features.R

class RecordingView(uri: Uri?) {
    val trackUri = ObservableField(uri)
    val isRecording = ObservableBoolean(false)
    val state = ObservableField<Int>(R.string.empty_dash)
    val name = ObservableField<String?>()
    val summary = ObservableField<SummaryText>()
    val maxSatellites = ObservableInt(0)
    val currentSatellites = ObservableInt(0)
    val isScanning = ObservableBoolean(false)
    val hasFix = ObservableBoolean(false)
    val signalQuality = ObservableInt(0)

    val navigation = SingleLiveEvent<Navigation>()

    object SignalQualityLevel {
        const val none = 0
        const val low = 1
        const val medium = 2
        const val high = 3
        const val excellent = 4
    }
}

data class SummaryText(val string: Int, val meterPerSecond: Float, val isRunners: Boolean, val meters: Float, val msDuration: Long)

sealed class Navigation {
    class GpsStatusAppInstallHint : Navigation()
    class GpsStatusAppOpen : Navigation()
}
