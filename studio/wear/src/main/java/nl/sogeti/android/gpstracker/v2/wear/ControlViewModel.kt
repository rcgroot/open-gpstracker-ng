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
package nl.sogeti.android.gpstracker.v2.wear

import android.databinding.ObservableBoolean
import android.databinding.ObservableField

class ControlViewModel {
    val duration = ObservableField<Long>(0L)
    val distance = ObservableField<Float>(0F)
    val averageSpeed = ObservableField<Float>(0F)
    val state = ObservableField<Control>(Control.Sync())
    val manualRefresh = ObservableBoolean(false)
    val confirmAction = ObservableField<Control?>()

    val leftControl = ObservableField<Control>(Control.Stop(false))
    val rightControl = ObservableField<Control>(Control.Start(false))
    val bottomControl = ObservableField<Control>(Control.Pause(false))
}

interface View {
    fun darken()
    fun brighter()
    fun startConfirmTimer()
    fun cancelConfirmTimer()
    fun showControls()
}
