/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
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
package nl.sogeti.android.gpstracker.ng.features.wear

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateBroadcastReceiver

class WearLoggingStateBroadcastReceiver : LoggingStateBroadcastReceiver() {

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun didStopLogging(context: Context) {
        context.startService(WearLoggingService.createStoppedIntent(context))
    }

    override fun didPauseLogging(context: Context, trackUri: Uri) {
        context.startService(WearLoggingService.createPausedIntent(context, trackUri))
    }

    override fun didStartLogging(context: Context, trackUri: Uri) {
        context.startService(WearLoggingService.createStartedIntent(context, trackUri))
    }

    override fun onError(context: Context) {
        context.stopService(WearLoggingService.createStoppedIntent(context))
    }
}
