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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import nl.sogeti.android.gpstracker.utils.activityresult.ActivityResultHandlerRegistry
import nl.sogeti.android.gpstracker.utils.activityresult.DefaultActivityResultHandlerRegistry

class ExportActivity : AppCompatActivity(),
        ActivityResultHandlerRegistry by DefaultActivityResultHandlerRegistry() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exporter__activity_export)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onRegisteredActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }
}
