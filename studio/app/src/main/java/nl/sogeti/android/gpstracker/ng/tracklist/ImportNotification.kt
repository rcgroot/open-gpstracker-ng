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
import android.app.Notification.PRIORITY_LOW
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.track.TrackActivity
import nl.sogeti.android.gpstracker.ng.utils.VersionHelper
import nl.sogeti.android.gpstracker.v2.R
import javax.inject.Inject


private const val NOTIFICATION_CHANNEL_ID = "import_notification"
private const val NOTIFICATION_IMPORT_ID = R.string.notification_import_title

class ImportNotification(val context: Context) {

    @Inject
    lateinit var versionHelper: VersionHelper
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val builder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_import_title))
                .setContentText(context.getString(R.string.notification_import_context_ongoing))
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setProgress(1, 0, true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setTrackListTargetIntent(context)
    }

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun didStartImport() {
        if (versionHelper.isAtLeast(Build.VERSION_CODES.O)) {
            createChannel()
        }


        importComplete = false
        notificationManager.notify(NOTIFICATION_IMPORT_ID, builder.build())
    }

    fun onProgress(progress: Int, goal: Int) {
        builder.setPriority(NotificationCompat.PRIORITY_MIN)
                .setProgress(goal, progress, false)
        importComplete = false
        notificationManager.notify(NOTIFICATION_IMPORT_ID, builder.build())
    }

    fun didCompleteImport() {
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(context.getString(R.string.notification_import_context_complete))
                .setProgress(0, 0, false)
        importComplete = true
        notificationManager.notify(NOTIFICATION_IMPORT_ID, builder.build())

    }


    fun dismissCompletedImport() {
        if (importComplete) {
            notificationManager.cancel(NOTIFICATION_IMPORT_ID)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_operation_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel.lockscreenVisibility = VISIBILITY_PUBLIC
            channel.name = context.getString(R.string.notification_operation_channel_name)
            channel.description = context.getString(R.string.notification_operation_channel_description)
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.importance = IMPORTANCE_LOW
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private var importComplete = true
    }
}

private fun NotificationCompat.Builder.setTrackListTargetIntent(context: Context): NotificationCompat.Builder {

    val resultPendingIntent: PendingIntent
    if (context.resources.getBoolean(R.bool.track_map_multi_pane)) {
        val intent = TrackActivity.newIntent(context, true)
        resultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    } else {
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(TrackActivity::class.java)
        stackBuilder.addNextIntent(Intent(context, TrackActivity::class.java))
        stackBuilder.addNextIntent(Intent(context, TrackListActivity::class.java))
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    setContentIntent(resultPendingIntent)

    return this
}
