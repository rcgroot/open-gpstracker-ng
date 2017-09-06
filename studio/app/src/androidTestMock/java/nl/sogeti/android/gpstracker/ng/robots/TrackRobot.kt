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
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.RootMatchers.isPlatformPopup
import android.support.test.espresso.matcher.ViewMatchers.*
import com.google.android.gms.maps.MapView
import nl.sogeti.android.gpstracker.ng.util.IdlingMapResource
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.Matchers.anyOf

class TrackRobot(private val activity: Activity) : Robot<TrackRobot>("TrackScreen") {

    var resource: IdlingMapResource? = null

    fun editTrack(): TrackRobot {
        openActionBarOverflowOrOptionsMenu(activity)
        onView(anyOf(withText(R.string.action_edit), withText(R.string.action_edit)))
                .perform(click())

        return this
    }

    fun openTrackTypeSpinner(): TrackRobot {
        onView(withId(R.id.spinner))
                .perform(click())

        return this
    }

    fun selectWalking(): TrackRobot {
        onView(withText(R.string.track_type_walk))
                .inRoot(isPlatformPopup())
                .perform(click())

        return this
    }

    fun ok(): TrackRobot {
        onView(withText(android.R.string.ok))
                .perform(click())

        return this
    }

    fun openAbout(): TrackRobot {
        openActionBarOverflowOrOptionsMenu(activity)
        onView(anyOf(withText(R.string.action_about), withText(R.string.action_about)))
                .perform(click())

        return this
    }

    fun openGraph(): TrackRobot {
        onView(anyOf(withId(R.id.action_graphs), withText(R.string.action_graphs)))
                .perform(click())

        return this
    }

    fun openTrackList(): TrackRobot {
        onView(anyOf(withId(R.id.action_list), withText(R.string.action_list)))
                .perform(click())

        return this
    }

    fun startRecording(): TrackRobot {
        onView(withContentDescription(R.string.control_record))
                .perform(click())

        return this
    }

    fun pauseRecording(): TrackRobot {
        onView(withContentDescription(R.string.control_pause))
                .perform(click())

        return this
    }

    fun resumeRecording(): TrackRobot {
        onView(withContentDescription(R.string.control_resume))
                .perform(click())

        return this
    }

    fun stopRecording(): TrackRobot {
        onView(withContentDescription(R.string.control_stop))
                .perform(click())

        return this
    }

    fun start(): TrackRobot {
        val mapView = activity.findViewById<MapView>(R.id.fragment_map_mapview)
        resource = IdlingMapResource(mapView)
        IdlingRegistry.getInstance().register(resource)
        sleep(5)

        return this
    }

    fun stop() {
        resource?.let {
            IdlingRegistry.getInstance().register(it)
        }
    }
}