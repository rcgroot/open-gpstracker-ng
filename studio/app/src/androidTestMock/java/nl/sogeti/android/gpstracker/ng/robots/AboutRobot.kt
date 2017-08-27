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

import android.support.test.espresso.Espresso
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import nl.sogeti.android.gpstracker.ng.about.AboutFragment
import nl.sogeti.android.gpstracker.ng.util.WebViewIdlingResource
import nl.sogeti.android.gpstracker.v2.R

class AboutRobot(private val activity: AppCompatActivity) : Robot<AboutRobot>("AboutScreen") {

    private var resource: WebViewIdlingResource? = null

    fun ok(): AboutRobot {
        Espresso.onView(ViewMatchers.withText(activity.getString(android.R.string.ok)))
                .perform(ViewActions.click())

        return this
    }

    fun start(): AboutRobot {
        waitForIdle()

        val fragment = activity.supportFragmentManager.findFragmentByTag(AboutFragment.TAG) as AboutFragment
        val webview = fragment.dialog.findViewById<WebView>(R.id.fragment_about_webview)
        resource = WebViewIdlingResource(webview)
        IdlingRegistry.getInstance().register(resource)

        return this
    }

    fun stop() {
        resource?.let {
            IdlingRegistry.getInstance().unregister(it)
        }
    }
}