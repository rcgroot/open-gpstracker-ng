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
package nl.sogeti.android.gpstracker.ng.common.bindings

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.support.v7.widget.AppCompatSpinner
import android.webkit.WebView
import android.widget.ImageView
import android.widget.SpinnerAdapter
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions


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

    @BindingAdapter("url")
    fun setUrl(webView: WebView, url: String) {
        webView.loadUrl(url)
    }

    @BindingAdapter("polylines")
    fun setPolylines(map: MapView, polylines: List<PolylineOptions>?) {
        val addLines: (GoogleMap) -> Unit = { googleMap ->
            googleMap.uiSettings.isMapToolbarEnabled = false
            map.tag = googleMap
            googleMap.clear()
            polylines?.map {
                googleMap.addPolyline(it)
            }
        }
        val tag = map.tag
        if (tag is GoogleMap) {
            addLines(tag)
        } else {
            map.getMapAsync(addLines)
        }
    }

    @BindingAdapter("bounds")
    fun setMapBounds(map: MapView, bounds: LatLngBounds?) {
        if (bounds != null) {
            map.getMapAsync {
                val update = CameraUpdateFactory.newLatLngBounds(bounds, 0)
                it.animateCamera(update, null)
            }
        }
    }

    @BindingAdapter("center")
    fun setMapTarget(map: MapView, center: LatLng?) {
        val ZOOM_WORLD = 1.0F
        val ZOOM_STREETS = 15.0F
        if (center != null) {
            map.getMapAsync {
                val update: CameraUpdate
                if (it.cameraPosition.zoom == ZOOM_WORLD) {
                    update = CameraUpdateFactory.newLatLngZoom(center, ZOOM_STREETS)
                } else {
                    update = CameraUpdateFactory.newLatLng(center)
                }
                it.animateCamera(update)
            }
        }
    }

    @BindingAdapter("adapter")
    fun setAdapter(view: AppCompatSpinner, adapter: SpinnerAdapter) {
        view.adapter = adapter
    }

    @BindingAdapter("selection")
    fun setSelected(spinner: AppCompatSpinner, selection: Int) {
        if (spinner.adapter != null && spinner.selectedItemPosition != selection) {
            spinner.setSelection(selection)
        }
    }

//    @BindingAdapter("bind:selection", "bind:selectionAttrChanged", requireAll = false)
//    fun setSelected(spinner: AppCompatSpinner, selection: Int, selectionAttrChanged: InverseBindingListener) {
//        if (spinner.selectedItemPosition != selection) {
//            spinner.setSelection(selection)
//        }
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                selectionAttrChanged.onChange()
//            }
//
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                selectionAttrChanged.onChange()
//            }
//        }
//    }
//
//    @InverseBindingAdapter(attribute = "bind:selection", event = "bind:selectionAttrChanged")
//    fun getSelectedValue(spinner: AppCompatSpinner): Int {
//        return spinner.selectedItemPosition
//    }
}