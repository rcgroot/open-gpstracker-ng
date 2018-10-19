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

import android.content.ContentResolver
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.*
import com.google.android.gms.common.api.GoogleApiClient
import nl.sogeti.android.gpstracker.utils.Consumable

internal class ExportPresenter(private val exporterManager: ExporterManager) : ViewModel() {

    private val _navigation = MutableLiveData<Consumable<ExportNavigation>>()
    internal val navigation: LiveData<Consumable<ExportNavigation>>
        get() = _navigation
    internal val viewModel = ExportViewModel()
    private var driveClient: GoogleApiClient? = null
    private val exportObserver = Observer<ExporterManager.ExportState> {
        onExportUpdate(it)
    }

    init {
        exporterManager.state.observeForever(exportObserver)
    }

    override fun onCleared() {
        exporterManager.stopExport()
        exporterManager.state.removeObserver(exportObserver)
        super.onCleared()
    }

    private fun onExportUpdate(state: ExporterManager.ExportState?) = when (state) {
        null, ExporterManager.ExportState.Idle -> {
            viewModel.isRunning.set(false)
            viewModel.isFinished.set(false)
        }
        is ExporterManager.ExportState.Active -> {
            viewModel.isRunning.set(true)
            viewModel.isFinished.set(false)
            viewModel.completedTracks.set(state.completedTracks)
            viewModel.completedWaypoints.set(state.completedWaypoints)
            viewModel.totalTracks.set(state.totalTracks)
            viewModel.totalWaypoints.set(state.totalWaypoints)
        }
        is ExporterManager.ExportState.Finished -> {
            viewModel.isRunning.set(false)
            viewModel.isFinished.set(true)
            viewModel.completedTracks.set(state.completedTracks)
            viewModel.completedWaypoints.set(state.completedWaypoints)
        }
        is ExporterManager.ExportState.Error -> {
            viewModel.isRunning.set(false)
            viewModel.isFinished.set(true)
        }
    }


    fun onConnectDriveClicked(view: View) {
        if (view is CheckBox) {
            view.isChecked = false
        }
        connectToDrive()
    }

    private fun connectToDrive() {
        _navigation.postValue(Consumable(ExportNavigation.ConnectDrive))
    }

    fun onDriveConnected(client: GoogleApiClient?) {
        val connected = client == null
        viewModel.isDriveConnected.set(connected)
        driveClient = client
    }

    fun onNextStepClicked(model: ExportViewModel) {
        if (!model.isDriveConnected.get()) {
            connectToDrive()
        } else {
            driveClient?.let {
                exporterManager.startExport(it)
            }
        }
    }

    internal class Factory(val contentResolver: ContentResolver): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val exporterManager = ExporterManager(contentResolver)
            @Suppress("UNCHECKED_CAST")
            return ExportPresenter(exporterManager) as T
        }
    }
}

sealed class ExportNavigation {
    object ConnectDrive : ExportNavigation()
}
