/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.robots

import android.app.Activity
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.IdlingResource
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import nl.sogeti.android.gpstracker.ng.utils.executeOnUiThread
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.Matchers.anyOf
import timber.log.Timber

class TrackRobot(private val activity: Activity) : Robot<TrackRobot>("TrackScreen") {

    var resource: IdlingMapResource? = null

    fun editTrack(): TrackRobot {
        onView(withId(R.id.action_edit))
                .perform(click())

        return this
    }

    fun openTrackTypeSpinner(): TrackRobot {
        onView(withId(R.id.spinner))
                .perform(click())

        return this
    }

    fun openAbout(): TrackRobot {
        openActionBarOverflowOrOptionsMenu(activity)
        onView(anyOf(withId(R.id.action_about), withText(R.string.action_about)))
                .perform(click())

        return this
    }

    fun start() {
        val mapView = activity.findViewById(R.id.fragment_map_mapview) as MapView
        resource = IdlingMapResource(mapView)
        Espresso.registerIdlingResources(resource)

    }

    fun stop() {
        Espresso.unregisterIdlingResources(resource)
    }

    class IdlingMapResource(map: MapView) : IdlingResource, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnMapLoadedCallback {

        private var isLoaded = false
        private var isIdle = false

        private var callback: IdlingResource.ResourceCallback? = null

        init {
            executeOnUiThread {
                map.getMapAsync {
                    it.setOnCameraIdleListener(this)
                    it.setOnCameraMoveListener(this)
                    it.setOnMapLoadedCallback(this)
                }
            }
        }

        override fun getName(): String = "MapResource"

        override fun isIdleNow(): Boolean {
            Timber.d("Is idle $isIdle")
            return isIdle && isLoaded
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = callback
        }

        override fun onCameraIdle() {
            Timber.d("Became idle")
            isIdle = true
            if (isIdleNow) {
                callback?.onTransitionToIdle()
            }
        }

        override fun onCameraMove() {
            isLoaded = false
            isIdle = false
        }

        override fun onMapLoaded() {
            isLoaded = true
            if (isIdleNow) {
                callback?.onTransitionToIdle()
            }
        }
    }
}