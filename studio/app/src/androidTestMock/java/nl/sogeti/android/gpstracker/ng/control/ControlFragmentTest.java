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
package nl.sogeti.android.gpstracker.ng.control;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.ng.util.MockServiceManager;
import nl.sogeti.android.gpstracker.v2.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class ControlFragmentTest {
    @Rule
    public FragmentTestRule<ControlFragment> wrapperFragment = new FragmentTestRule<>(ControlFragment.class);
    private ControlFragment sut;
    private MockServiceManager mockServiceManager;

    @Before
    public void setUp() {
        mockServiceManager = new MockServiceManager();
        mockServiceManager.reset();
        sut = wrapperFragment.getFragment();
    }

    @After
    public void tearDown() {
        mockServiceManager.reset();
        mockServiceManager = null;
        sut = null;
    }

    @Test
    public void testStartUp() {
        // Verify
        onView(withId(R.id.widget_control_left))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.widget_control_right))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testVisibleWhenStarted() {
        // Execute
        mockServiceManager.startGPSLogging(sut.getActivity(), null);

        // Verify
        onView(withId(R.id.widget_control_left))
                .check(matches(isDisplayed()));
        onView(withId(R.id.widget_control_right))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testVisibleWhenPaused() {
        // Execute
        mockServiceManager.pauseGPSLogging(sut.getActivity());

        // Verify
        onView(withId(R.id.widget_control_left))
                .check(matches(isDisplayed()));
        onView(withId(R.id.widget_control_right))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testVisibleWhenResumed() {
        // Execute
        mockServiceManager.resumeGPSLogging(sut.getActivity());

        // Verify
        onView(withId(R.id.widget_control_left))
                .check(matches(isDisplayed()));
        onView(withId(R.id.widget_control_right))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testVisibleWhenStopped() {
        // Execute
        mockServiceManager.stopGPSLogging(sut.getActivity());

        // Verify
        onView(withId(R.id.widget_control_left))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.widget_control_right))
                .check(matches(isDisplayed()));
    }
}
