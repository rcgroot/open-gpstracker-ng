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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import nl.renedegroot.opengpstracker.exporter.databinding.ExporterFragmentExportBinding

/**
 * A placeholder fragment containing a simple view.
 */
class ExportFragment : Fragment() {

    private lateinit var presenter: ExportPresenter
    private var binding: ExporterFragmentExportBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ViewModelProviders.of(this).get(ExportPresenter::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val createdBinding = DataBindingUtil.inflate<ExporterFragmentExportBinding>(inflater, R.layout.exporter__fragment_export, container, false)
        createdBinding.presenter = presenter
        createdBinding.model = presenter.model
        binding = createdBinding

        return createdBinding.root
    }
}
