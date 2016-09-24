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

abstract class TrackObservingPresenter : ConnectedServicePresenter() {
    private val observer = TrackObserver()
    private val uriChangeListener = TrackUriChangeListener()

    override fun didStart() {
        super.didStart()
        val field = getTrackUriField()
        uriChangeListener.listenTo(field)
        observer.registerOn(field.get())
    }

    override fun willStop() {
        super.willStop()
        uriChangeListener.listenTo(null)
        observer.registerOn(null)
    }

    abstract fun getTrackUriField(): ObservableField<Uri?>

    abstract fun didChangeUriContent(uri: Uri, includingUri: Boolean)

    private inner class TrackUriChangeListener : Observable.OnPropertyChangedCallback() {
        var uriField = ObservableField<Uri?>()

        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            val newUri = uriField.get()
            observer.registerOn(newUri)
            if (newUri != null) {
                didChangeUriContent(newUri, true)
            }
        }

        fun listenTo(uri: ObservableField<Uri?>?) {
            uriField.removeOnPropertyChangedCallback(this)
            uri?.addOnPropertyChangedCallback(this)
            uriField = uri ?: ObservableField<Uri?>()
        }

    }

    private inner class TrackObserver : ContentObserver(null) {

        override fun onChange(selfChange: Boolean, uri: Uri) {
            didChangeUriContent(uri, false)
        }

        fun registerOn(uri: Uri?) {
            context?.contentResolver?.unregisterContentObserver(this)
            if (uri != null) {
                context?.contentResolver?.registerContentObserver(uri, true, this)
            }
        }
    }
}