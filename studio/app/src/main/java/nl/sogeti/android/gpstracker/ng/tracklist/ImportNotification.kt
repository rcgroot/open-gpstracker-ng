/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
-
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
package nl.sogeti.android.gpstracker.ng.tracklist

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.utils.VersionHelper
import nl.sogeti.android.gpstracker.v2.R
import javax.inject.Inject


private const val NOTIFICATION_CHANNEL_ID = "import_notification"
private const val NOTIFICATION_IMPORT_ID = R.string.notification_import_title

class ImportNotification(val context: Context) {

    @Inject
    lateinit var versionHelper: VersionHelper
    private var importBuilder: NotificationCompat.Builder? = null
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun didStartImport() {
        if (versionHelper.isAtLeast(Build.VERSION_CODES.O)) {
            createChannel()
        }

        val importBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_import_title))
                .setContentText(context.getString(R.string.notification_import_context_ongoing))
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setProgress(1, 0, true)
        importComplete = false
        notificationManager.notify(NOTIFICATION_IMPORT_ID, importBuilder.build())

        this.importBuilder = importBuilder
    }

    fun onProgress(progress: Int, goal: Int) {
        importBuilder?.let {
            it.setProgress(goal, progress, false)
            importComplete = false
            notificationManager.notify(NOTIFICATION_IMPORT_ID, it.build())
        }
    }

    fun didCompleteImport() {
        importBuilder?.let {
            it.setContentText(context.getString(R.string.notification_import_context_complete))
                    .setProgress(0, 0, false)
            importComplete = true
            notificationManager.notify(NOTIFICATION_IMPORT_ID, it.build())

        }
    }

    fun dismissCompletedImport() {
        if (importComplete) {
            notificationManager.cancel(NOTIFICATION_IMPORT_ID)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_operation_channel_name),
                    NotificationManager.IMPORTANCE_HIGH)
            channel.description = context.getString(R.string.notification_operation_channel_description)
            channel.enableLights(false)
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private var importComplete = true
    }
}
