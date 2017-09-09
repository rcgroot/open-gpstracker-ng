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
package nl.sogeti.android.gpstracker.ng.recording

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.Navigation
import nl.sogeti.android.gpstracker.ng.common.controllers.packagemanager.PackageManagerFactory
import nl.sogeti.android.gpstracker.v2.R
import javax.inject.Inject

const val GPS_STATUS_PACKAGE_NAME = "com.eclipsim.gpsstatus2"

class RecordingNavigation : Navigation {

    @Inject
    lateinit var packageManagerFactory: PackageManagerFactory

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun openExternalGpsStatusApp(context: Context) {
        val packageManager = packageManagerFactory.createPackageManager(context)
        val intent = packageManager.getLaunchIntentForPackage(GPS_STATUS_PACKAGE_NAME);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(intent);
    }

    fun showInstallHintForGpsStatusApp(context: Context) {
        AlertDialog.Builder(context)
                .setTitle(R.string.fragment_recording_gpsstatus_title)
                .setMessage(R.string.fragment_recording_gpsstatus_body)
                .setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
                .setPositiveButton(R.string.permission_button_install, { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$GPS_STATUS_PACKAGE_NAME"))
                    context.startActivity(intent)
                })
                .show()
    }
}
