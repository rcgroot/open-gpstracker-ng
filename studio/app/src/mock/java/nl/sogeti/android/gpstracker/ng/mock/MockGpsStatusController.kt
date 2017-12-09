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
package nl.sogeti.android.gpstracker.ng.mock

import android.os.Handler
import android.os.Looper
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController

typealias Action = (GpsStatusController.Listener) -> Unit

class MockGpsStatusController(val listener: GpsStatusController.Listener) : GpsStatusController {
    private val commands = listOf<Action>(
            { it.onStart() },
            { it.onChange(0, 0) },
            { it.onChange(0, 8) },
            { it.onChange(1, 10) },
            { it.onChange(3, 12) },
            { it.onChange(5, 11) },
            { it.onChange(7, 14) },
            { it.onChange(9, 21) },
            { it.onFirstFix() },
            { it.onStop() }
    )

    private var handler: Handler? = null

    override fun startUpdates() {
        handler = Handler(Looper.getMainLooper())
        nextCommand(0)
    }

    override fun stopUpdates() {
        handler = null
    }

    private fun scheduleNextCommand(i: Int) {
        handler?.postDelayed({ nextCommand(i) }, 1500)
    }

    private fun nextCommand(i: Int) {
        handler?.let {
            commands[i](listener)
            val next = if (i < (commands.count() - 1)) i + 1 else 0
            scheduleNextCommand(next)
        }
    }
}
