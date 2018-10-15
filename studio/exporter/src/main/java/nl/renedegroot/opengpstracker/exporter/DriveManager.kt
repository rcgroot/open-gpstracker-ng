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
import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import timber.log.Timber

private const val REQUEST_CODE_RESOLUTION = 1
private const val REQUEST_CODE_ERROR = 2

/**
 * Communicates with the Google Drive
 */
internal class DriveManager(val context: Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    var driveClient: GoogleApiClient? = null
        private set
    private var onConnected: (Boolean) -> Unit = {}

    fun start(onConnected: (Boolean) -> Unit = {}) {
        this.onConnected = onConnected
        if (driveClient == null) {
            driveClient = GoogleApiClient.Builder(context.applicationContext)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
        }
        driveClient?.connect()
    }

    fun processResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                driveClient?.connect()
            }
            return true
        }
        return false
    }

    fun stop() {
        onConnected = {}
        driveClient?.disconnect()
    }

    override fun onConnected(result: Bundle?) {
        Timber.d("onConnectionFailed $result")
        onConnected(true)
        onConnected = {}
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Timber.d("onConnectionFailed $result")
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION)
            } catch (exception: IntentSender.SendIntentException) {
                onConnected(false)
                onConnected = {}
            }

        } else {
            GoogleApiAvailability
                    .getInstance()
                    .getErrorDialog(activity, result.errorCode, REQUEST_CODE_ERROR).show()
            onConnected(false)
            onConnected = {}
        }
    }

    override fun onConnectionSuspended(value: Int) {
        Timber.d("onConnectionSuspended $value")
    }
}