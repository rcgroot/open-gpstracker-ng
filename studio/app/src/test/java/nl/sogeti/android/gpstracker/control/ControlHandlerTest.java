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
package nl.sogeti.android.gpstracker.control;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_PAUSED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_STOPPED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_UNKNOWN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ControlHandlerTest {

    @Mock
    LoggerViewModel mockLogger;
    @Mock
    ControlHandler.Listener mockListener;
    private ControlHandler sut;

    @Before
    public void setup() {
        sut = new ControlHandler(mockListener, mockLogger);
    }

    @Test
    public void setStateUnknown() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        ControlHandler.setState(container, STATE_UNKNOWN);

        // Verify
        verify(container.getChildAt(0)).setVisibility(View.GONE);
        verify(container.getChildAt(1)).setEnabled(false);
    }

    @Test
    public void setStateStopped() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        ControlHandler.setState(container, STATE_STOPPED);

        // Verify
        verify(container.getChildAt(0)).setVisibility(View.GONE);
        verify(container.getChildAt(1)).setEnabled(true);
    }

    @Test
    public void setStatePause() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        ControlHandler.setState(container, STATE_PAUSED);

        // Verify
        verify(container.getChildAt(0)).setVisibility(View.VISIBLE);
        verify(container.getChildAt(1)).setEnabled(true);
    }

    @Test
    public void setStateLogging() {
        // Prepare
        ViewGroup container = createButtonViewGroup();

        // Execute
        ControlHandler.setState(container, STATE_LOGGING);

        // Verify
        verify(container.getChildAt(0)).setVisibility(View.VISIBLE);
        verify(container.getChildAt(1)).setEnabled(true);
    }

    @Test
    public void leftClickDuringUnknown() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_UNKNOWN);

        // Execute
        sut.onClickLeft();

        // Verify
        verifyZeroInteractions(mockListener);
    }

    @Test
    public void leftClickDuringStopped() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_STOPPED);

        // Execute
        sut.onClickLeft();

        // Verify
        verifyZeroInteractions(mockListener);
    }

    @Test
    public void leftClickDuringLogging() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_LOGGING);

        // Execute
        sut.onClickLeft();

        // Verify
        verify(mockListener).stopLogging();
    }

    @Test
    public void leftClickDuringPaused() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_PAUSED);

        // Execute
        sut.onClickLeft();

        // Verify
        verify(mockListener).stopLogging();
    }

    @Test
    public void rightClickDuringUnknown() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_UNKNOWN);

        // Execute
        sut.onClickRight();

        // Verify
        verifyZeroInteractions(mockListener);
    }

    @Test
    public void rightClickDuringStopped() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_STOPPED);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockListener).startLogging();
    }

    @Test
    public void rightClickDuringLogging() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_LOGGING);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockListener).pauseLogging();
    }

    @Test
    public void rightClickDuringPaused() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_PAUSED);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockListener).resumeLogging();
    }

    /* Helpers */

    @NonNull
    private ViewGroup createButtonViewGroup() {
        ViewGroup container = Mockito.mock(ViewGroup.class);
        FloatingActionButton left = Mockito.mock(FloatingActionButton.class);
        final ViewPropertyAnimator mockAnimator = mock(ViewPropertyAnimator.class);
        when(mockAnimator.translationX(anyFloat())).thenReturn(mockAnimator);
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception {
                ((Runnable) invocation.getArguments()[0]).run();
                return mockAnimator;
            }
        }).when(mockAnimator).withEndAction(any(Runnable.class));


        when(left.animate()).thenReturn(mockAnimator);
        FloatingActionButton right = Mockito.mock(FloatingActionButton.class);
        when(container.getChildAt(0)).thenReturn(left);
        when(container.getChildAt(1)).thenReturn(right);

        return container;
    }


}
