/*
 *  Open GPS Tracker
 *  Copyright (C) 2018  René de Groot
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.sogeti.android.gpstracker.utils.activityresult

import android.content.Intent
import android.util.SparseArray

interface ActivityResultHandlerRegistry {

    fun registerActivityResult(requestCode: Int, handler: (resultCode: Int, result: Intent?) -> Unit)

    fun registerActivityResult(requestCode: Int, handler: ResultHandler)

    fun onRegisteredActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}

interface ResultHandler {

    fun handleResult(resultCode: Int, result: Intent?)
}

class DefaultActivityResultHandlerRegistry : ActivityResultHandlerRegistry {

    private val registerHandlers = SparseArray<ResultHandler>()

    override fun registerActivityResult(requestCode: Int, handler: (resultCode: Int, result: Intent?) -> Unit) {
        registerHandlers.append(requestCode, object : ResultHandler {
            override fun handleResult(resultCode: Int, result: Intent?) {
                handler(resultCode, result)
            }
        })
    }

    override fun registerActivityResult(requestCode: Int, handler: ResultHandler) {
        registerHandlers.append(requestCode, handler)
    }

    override fun onRegisteredActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        registerHandlers[requestCode]?.handleResult(resultCode, data)
    }
}
