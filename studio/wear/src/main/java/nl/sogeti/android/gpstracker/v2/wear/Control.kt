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

sealed class Control(val enabled: Boolean, val iconId: Int, val action: Int, val confirm: Int, val state: Int) {

    class Start(enabled: Boolean) : Control(enabled,
            R.drawable.ic_navigation_black_24dp,
            R.string.control_action_start,
            R.string.control_confirm_start,
            R.string.control_state_started)

    class Pause(enabled: Boolean) : Control(enabled,
            R.drawable.ic_pause_black_24dp,
            R.string.control_action_pause,
            R.string.control_confirm_pause,
            R.string.control_state_paused)

    class Resume(enabled: Boolean) : Control(enabled,
            R.drawable.ic_navigation_black_24dp,
            R.string.control_action_resume,
            R.string.control_confirm_resume,
            R.string.control_state_started)

    class Stop(enabled: Boolean) : Control(enabled,
            R.drawable.ic_stop_black_24dp,
            R.string.control_action_stop,
            R.string.control_confirm_stop,
            R.string.control_state_stopped)

    class Sync : Control(true,
            R.drawable.ic_sync_black_24dp,
            R.string.control_action_sync,
            R.string.control_confirm_syncing,
            R.string.control_state_syncing)

    class Disconnect : Control(true,
            R.drawable.ic_sync_disabled_black_24dp,
            R.string.control_action_sync,
            R.string.control_confirm_disconnecting,
            R.string.control_state_disconnected)
}
