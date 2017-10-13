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
package nl.sogeti.android.gpstracker.ng.ui.fragments;

import android.Manifest;
import android.support.test.espresso.Espresso;
import android.support.test.rule.GrantPermissionRule;
import android.webkit.WebView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nl.sogeti.android.gpstracker.ng.about.AboutFragment;
import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.ng.util.WebViewIdlingResource;
import nl.sogeti.android.gpstracker.v2.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class AboutFragmentEspressoTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule .grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule
    public FragmentTestRule<AboutFragment> wrapperFragment = new FragmentTestRule<>(AboutFragment.class);
    private AboutFragment sut = null;
    private WebViewIdlingResource webIdlingResource;

    @Before
    public void setUp() {
        sut = wrapperFragment.getFragment();
        WebView webview = sut.getDialog().findViewById(R.id.fragment_about_webview);
        webIdlingResource = new WebViewIdlingResource(webview);
        Espresso.registerIdlingResources(webIdlingResource);
    }

    @After
    public void tearDown() {
        if (webIdlingResource != null) {
            Espresso.unregisterIdlingResources(webIdlingResource);
            webIdlingResource = null;
        }
    }

    @Test
    public void showAboutInfo() {
        // Verify
        onView(withId(R.id.fragment_about_version))
                .check(matches(isDisplayed()));
        onView(withId(R.id.fragment_about_webview))
                .check(matches(isDisplayed()));
    }
}
