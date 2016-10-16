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
package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.database.ContentObserver
import android.databinding.Observable
import android.databinding.ObservableField
import android.net.Uri
import timber.log.Timber

abstract class TrackObservingPresenter() : ConnectedServicePresenter() {
    private val contentObserver = TrackContentObserver()
    private var fieldObserver : UriFieldObserver? = null

    override fun didStart() {
        super.didStart()
        val field = getTrackUriField()
        val fieldObserver = UriFieldObserver(field)
        this.fieldObserver = fieldObserver
        contentObserver.registerOn(field.get())
    }

    override fun willStop() {
        super.willStop()
        fieldObserver?.stop()
        contentObserver.registerOn(null)
    }

    abstract fun getTrackUriField(): ObservableField<Uri?>

    abstract fun onChangeUriField(uri: Uri)
    abstract fun onChangeUriContent(uri: Uri)

    private inner class UriFieldObserver(val field: ObservableField<Uri?>) : Observable.OnPropertyChangedCallback() {
        init {
            field.addOnPropertyChangedCallback(this)
        }

        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            Timber.d("uri field: fun onPropertyChanged($sender: Observable, $propertyId: Int)")
            val uri = field.get()
            if (uri != null) {
                contentObserver.registerOn(uri)
                onChangeUriField(uri)
            }
        }

        fun stop() {
            this.field.removeOnPropertyChangedCallback(this)
        }
    }

    private inner class TrackContentObserver : ContentObserver(null) {

        override fun onChange(selfChange: Boolean, uri: Uri) {
            Timber.d("uri content: override fun onChange($selfChange: Boolean, $uri: Uri)")
            onChangeUriContent(uri)
        }

        fun registerOn(uri: Uri?) {
            context?.contentResolver?.unregisterContentObserver(this)
            if (uri != null) {
                context?.contentResolver?.registerContentObserver(uri, true, this)
            }
        }
    }
}