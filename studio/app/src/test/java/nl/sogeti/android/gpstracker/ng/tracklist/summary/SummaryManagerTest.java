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
package nl.sogeti.android.gpstracker.ng.tracklist.summary;

import android.content.Context;
import android.net.Uri;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SummaryManagerTest {

    @Rule
    public MockAppComponentTestRule appComponentRule = new MockAppComponentTestRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    Context mockContext = null;
    @Mock
    ExecutorService mockExecutor = null;
    private SummaryManager sut;

    @Before
    public void setUp() {
        sut = new SummaryManager();
        sut.setExecutor(mockExecutor);
    }

    @After
    public void tearDown() {
        while (sut.isRunning()) {
            sut.stop();
        }
    }

    @Test
    public void testStartSetsRunning() {
        // Execute
        sut.start();

        // Verify
        assertTrue(sut.isRunning());
    }

    @Test
    public void testBeginsStopped() {
        // Verify
        assertFalse(sut.isRunning());
    }

    @Test
    public void testTwiceStartSingleStop() {
        // Execute
        sut.start();
        sut.start();
        sut.stop();

        // Verify
        assertTrue(sut.isRunning());
    }

    @Test
    public void testStopStoppedIllegal() {
        // Prepare
        IllegalStateException caught = null;

        // Execute
        try {
            sut.stop();
        } catch (IllegalStateException e) {
            caught = e;
        }

        // Verify
        assertNotNull(caught);
    }

    @Test
    public void testCoreCount() {
        // Execute
        int threads = sut.numberOfThreads();

        // Verify
        assertThat(threads, Matchers.greaterThan(1));
    }

    @Test
    public void testBackgroundPriority() {
        // Prepare
        ThreadFactory factory = new SummaryManager.BackgroundThreadFactory();

        // Execute
        Thread thread = factory.newThread(new Runnable() {
            @Override
            public void run() {
            }
        });

        // Verify
        assertThat(thread.getPriority(), Matchers.lessThanOrEqualTo(android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    @Test
    public void testCallWhenStopped() {
        // Prepare
        final List<Summary> callback = new LinkedList<>();
        Uri uri = mock(Uri.class);
        sut.start();
        sut.stop();

        // Execute
        sut.collectSummaryInfo(mockContext, uri, new Function1<Summary, Unit>() {
            @Override
            public Unit invoke(Summary summary) {
                callback.add(summary);
                return null;
            }
        });

        // Verify
        assertThat(callback.size(), Matchers.is(0));
        verify(mockExecutor, times(0)).submit(ArgumentMatchers.any(Runnable.class));
    }

    @Test
    public void testExecutionWhenStopped() {
        // Prepare
        final List<Summary> callback = new LinkedList<>();
        Uri uri = mock(Uri.class);

        // Execute
        sut.executeTrackCalculation(mockContext, uri, new Function1<Summary, Unit>() {
            @Override
            public Unit invoke(Summary summary) {
                callback.add(summary);
                return null;
            }
        });

        // Verify
        assertThat(callback.size(), is(0));
    }
}