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

import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.ViewModel
import timber.log.Timber

internal class ExportPresenter(
        private val driveManager: DriveManager,
        private val exporterManager: ExporterManager) : ViewModel(), ExporterManager.ProgressListener {

    internal val model = ExportModel()

    init {
        connectToServices()
        exporterManager.addListener(this)
    }

    override fun onCleared() {
        driveManager.stop()
        exporterManager.stopExport()
        exporterManager.removeListener(this)
        super.onCleared()
    }

    fun connectToServices() {
        model.isTrackerConnected.set(true)
        driveManager.start { isConnected ->
            model.isDriveConnected.set(isConnected)
            if (isConnected) {
                Timber.d("Everything is connected")
            } else {
                Timber.d("Drive failed")
            }
        }
    }

    fun connectGoogleDrive() {
        driveManager.start { isConnected ->
            model.isDriveConnected.set(isConnected)
        }
    }

    fun connectTracksDatabase() {
        model.isTrackerConnected.set(true)
    }

    override fun updateExportProgress(isRunning: Boolean?, isFinished: Boolean?, completedTracks: Int?, totalTracks: Int?, completedWaypoints: Int?, totalWaypoints: Int?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startTracksConnect() {
        connectTracksDatabase()
    }

    fun startDriveConnect() {
        connectGoogleDrive()
    }

    fun startExport() {
        val client = driveManager.driveClient;
        if (client != null) {
            exporterManager.startExport(client)
        }
    }

    fun onConnectDrive(view: View) {
        if (view is CheckBox) {
            view.isChecked = false
        }
        startDriveConnect()
    }

    fun onConnectTracks(view: View) {
        if (view is CheckBox) {
            view.isChecked = false
        }
        startTracksConnect()
    }

    fun nextStep(model: ExportModel) {
        if (!model.isTrackerConnected.get()) {
            startTracksConnect()
        } else if (!model.isDriveConnected.get()) {
            startDriveConnect()
        } else {
            startExport()
        }
    }
}