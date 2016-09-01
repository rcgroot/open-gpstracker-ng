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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.sogeti.android.gpstracker.integration.ServiceManager;

import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_PAUSED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_STOPPED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_UNKNOWN;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ControlPresenterTest {

    @Mock
    LoggerViewModel mockLogger;
    @Mock
    ServiceManager mockServiceManager;
    private ControlPresenter sut;

    @Before
    public void setup() {
        sut = new ControlPresenter(mockLogger);
        sut.serviceManager = mockServiceManager;
    }

    @Test
    public void leftClickDuringUnknown() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_UNKNOWN);

        // Execute
        sut.onClickLeft();

        // Verify
        verifyZeroInteractions(mockServiceManager);
    }

    @Test
    public void leftClickDuringStopped() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_STOPPED);

        // Execute
        sut.onClickLeft();

        // Verify
        verifyZeroInteractions(mockServiceManager);
    }

    @Test
    public void leftClickDuringLogging() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_LOGGING);

        // Execute
        sut.onClickLeft();

        // Verify
        verify(mockServiceManager).stopGPSLogging(null);
    }

    @Test
    public void leftClickDuringPaused() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_PAUSED);

        // Execute
        sut.onClickLeft();

        // Verify
        verify(mockServiceManager).stopGPSLogging(null);
    }

    @Test
    public void rightClickDuringUnknown() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_UNKNOWN);

        // Execute
        sut.onClickRight();

        // Verify
        verifyZeroInteractions(mockServiceManager);
    }

    @Test
    public void rightClickDuringStopped() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_STOPPED);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockServiceManager).startGPSLogging(null, "New NG track!");
    }

    @Test
    public void rightClickDuringLogging() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_LOGGING);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockServiceManager).pauseGPSLogging(null);
    }

    @Test
    public void rightClickDuringPaused() {
        // Prepare
        when(mockLogger.getState()).thenReturn(STATE_PAUSED);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockServiceManager).resumeGPSLogging(null);
    }
}
