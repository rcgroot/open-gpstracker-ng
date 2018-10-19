/*
 * Open GPS Tracker
 * Copyright (C) 2018  René de Groot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.renedegroot.opengpstracker.exporter

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import nl.sogeti.android.gpstracker.utils.activityresult.ActivityResultHandlerRegistry
import timber.log.Timber

private const val REQUEST_CODE_RESOLUTION = 1
private const val REQUEST_CODE_ERROR = 2

/**
 * Communicates with the Google Drive
 */
internal class DriveManager(val activity: Activity) {

    private var driveClient: GoogleApiClient? = null
    private var onConnected: (GoogleApiClient?) -> Unit = {}
    private val connectionCallback = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(result: Bundle?) {
            Timber.d("onConnectionFailed $result")
            onConnected(driveClient)
            onConnected = {}
        }

        override fun onConnectionSuspended(value: Int) {
            Timber.d("onConnectionSuspended $value")
        }
    }
    private val connectionFailed = GoogleApiClient.OnConnectionFailedListener { result ->
        Timber.d("onConnectionFailed $result")
        if (result.hasResolution()) {
            try {
                (activity as ActivityResultHandlerRegistry)
                        .registerActivityResult(REQUEST_CODE_RESOLUTION) { resultCode, _ ->
                            processResult(resultCode)
                        }
                result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION)
            } catch (exception: IntentSender.SendIntentException) {
                onConnected(null)
                onConnected = {}
            }

        } else {
            GoogleApiAvailability
                    .getInstance()
                    .getErrorDialog(activity, result.errorCode, REQUEST_CODE_ERROR).show()
            onConnected(null)
            onConnected = {}
        }
    }

    fun start(onConnected: (GoogleApiClient?) -> Unit = {}) {
        this.onConnected = onConnected
        if (driveClient == null) {
            driveClient = GoogleApiClient.Builder(activity.applicationContext)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(connectionCallback)
                    .addOnConnectionFailedListener(connectionFailed)
                    .build()
        }
        driveClient?.connect()
    }

    private fun processResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            Timber.d("Problem resolved")
            driveClient?.connect()
        } else {
            Timber.d("Problem remains")
        }
    }

    fun stop() {
        onConnected = {}
        driveClient?.disconnect()
    }
}
