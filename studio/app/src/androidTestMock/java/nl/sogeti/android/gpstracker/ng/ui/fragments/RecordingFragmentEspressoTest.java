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
import androidx.test.espresso.IdlingRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nl.sogeti.android.gpstracker.ng.features.recording.RecordingFragment;
import nl.sogeti.android.gpstracker.ng.util.FragmentTestRule;
import nl.sogeti.android.gpstracker.service.mock.MockBroadcastSender;
import nl.sogeti.android.gpstracker.service.mock.MockServiceManager;
import nl.sogeti.android.gpstracker.service.mock.MockTracksContentProvider;
import nl.sogeti.android.gpstracker.v2.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

public class RecordingFragmentEspressoTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule
    public FragmentTestRule<RecordingFragment> wrapperFragment = new FragmentTestRule<>(RecordingFragment.class);
    private RecordingFragment sut;
    private MockServiceManager mockServiceManager;
    private MockTracksContentProvider mockTracksContentProvider;

    @Before
    public void setUp() {
        IdlingRegistry.getInstance().register(MockBroadcastSender.Espresso.getResource());
        sut = wrapperFragment.getFragment();
        mockServiceManager = new MockServiceManager(sut.requireContext());
        mockServiceManager.getGpsRecorder().setShouldScheduleWaypoints(false);
        mockTracksContentProvider = new MockTracksContentProvider();
    }

    @After
    public void tearDown() {
        if (mockServiceManager != null) {
            mockServiceManager.reset();
            mockServiceManager = null;
        }
        if (mockTracksContentProvider != null) {
            mockTracksContentProvider.reset();
            mockTracksContentProvider = null;
        }
        sut = null;
        IdlingRegistry.getInstance().unregister(MockBroadcastSender.Espresso.getResource());
    }

    @Test
    public void testVisibleWhenStarted() {
        // Execute
        mockServiceManager.startGPSLogging(null);
        MockTracksContentProvider.globalState.createTrack(1L, MockTracksContentProvider.globalState.getGpxAmsterdam(), null);

        // Verify
        onView(withId(R.id.fragment_recording_container)).check(matches(isDisplayed()));
    }

    @Test
    public void testVisibleWhenPaused() {
        // Execute
        mockServiceManager.pauseGPSLogging();

        // Verify
        onView(withId(R.id.fragment_recording_container)).check(matches(isDisplayed()));
    }

    @Test
    public void testVisibleWhenResumed() {
        // Execute
        mockServiceManager.resumeGPSLogging();

        // Verify
        onView(withId(R.id.fragment_recording_container)).check(matches(isDisplayed()));
    }
}
