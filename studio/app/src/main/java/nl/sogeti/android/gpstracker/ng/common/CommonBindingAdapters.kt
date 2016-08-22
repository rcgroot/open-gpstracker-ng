/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
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
package nl.sogeti.android.gpstracker.ng.common

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.webkit.WebView
import android.widget.ImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLngBounds


open class CommonBindingAdapters {
    @BindingAdapter("bitmap")
    fun setBitmap(view: ImageView, bitmap: Bitmap?) {
        view.setImageBitmap(bitmap)
    }

    @BindingAdapter("srcCompat")
    fun setSrcCompat(view: ImageView, resource: Int?) {
        if (resource != null) {
            view.setImageResource(resource)
        }
    }

    @BindingAdapter("bind:url")
    fun setUrl(webView: WebView, url: String) {
        webView.loadUrl(url)
    }

    @BindingAdapter("mapFocus")
    fun setMapFocus(map: MapView, bounds: LatLngBounds?) {
        if (bounds != null) {
            map.getMapAsync(MapUpdate(bounds))
        }
    }

    class MapUpdate(val bounds: LatLngBounds) : GoogleMap.CancelableCallback, OnMapReadyCallback {
        val DEFAULT_ZOOM_LEVEL = 12.0f

        override fun onMapReady(googleMap: GoogleMap?) {
            if (googleMap != null) {
                val update = CameraUpdateFactory.newLatLngZoom(bounds.center, DEFAULT_ZOOM_LEVEL)
                googleMap.animateCamera(update, this)
            }
        }

        override fun onFinish() {
        }

        override fun onCancel() {
        }
    }
}