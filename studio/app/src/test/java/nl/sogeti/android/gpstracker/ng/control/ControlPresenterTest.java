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

import android.content.Context;
import android.databinding.ObservableInt;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import nl.sogeti.android.gpstracker.integration.ServiceManager;
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule;

import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_PAUSED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_STOPPED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_UNKNOWN;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ControlPresenterTest {

    @Rule
    public MockAppComponentTestRule appComponentRule = new MockAppComponentTestRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    ControlViewModel viewModel;
    @Mock
    ServiceManager mockServiceManager;
    @Mock
    Context mockContext;
    private ControlPresenter sut;

    @Before
    public void setup() {
        viewModel = new ControlViewModel();
        sut = new ControlPresenter(viewModel);
        sut.setContext(mockContext);
        sut.setServiceManager(mockServiceManager);
    }

    @Test
    public void leftClickDuringUnknown() {
        // Prepare
        viewModel.getState().set(STATE_UNKNOWN);

        // Execute
        sut.onClickLeft();

        // Verify
        verifyZeroInteractions(mockServiceManager);
    }

    @Test
    public void leftClickDuringStopped() {
        // Prepare
        viewModel.getState().set(STATE_STOPPED);

        // Execute
        sut.onClickLeft();

        // Verify
        verifyZeroInteractions(mockServiceManager);
    }

    @Test
    public void leftClickDuringLogging() {
        // Prepare
        viewModel.getState().set(STATE_LOGGING);

        // Execute
        sut.onClickLeft();

        // Verify
        verify(mockServiceManager).stopGPSLogging(mockContext);
    }

    @Test
    public void leftClickDuringPaused() {
        // Prepare
        viewModel.getState().set(STATE_PAUSED);

        // Execute
        sut.onClickLeft();

        // Verify
        verify(mockServiceManager).stopGPSLogging(mockContext);
    }

    @Test
    public void rightClickDuringUnknown() {
        // Prepare
        viewModel.getState().set(STATE_UNKNOWN);

        // Execute
        sut.onClickRight();

        // Verify
        verifyZeroInteractions(mockServiceManager);
    }

    @Test
    public void rightClickDuringStopped() {
        // Prepare
        viewModel.getState().set(STATE_STOPPED);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockServiceManager).startGPSLogging(mockContext, null);
    }

    @Test
    public void rightClickDuringLogging() {
        // Prepare
        viewModel.getState().set(STATE_LOGGING);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockServiceManager).pauseGPSLogging(mockContext);
    }

    @Test
    public void rightClickDuringPaused() {
        // Prepare
        viewModel.getState().set(STATE_PAUSED);

        // Execute
        sut.onClickRight();

        // Verify
        verify(mockServiceManager).resumeGPSLogging(mockContext);
    }
}
