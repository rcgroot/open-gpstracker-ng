/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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
import android.content.Intent;
import android.net.Uri;

import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.integration.ServiceManager;
import nl.sogeti.android.gpstracker.ng.common.ConnectedServicePresenter;

import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_LOGGING;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_PAUSED;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.STATE_STOPPED;

public class ControlPresenter extends ConnectedServicePresenter {
    private final LoggerViewModel viewModel;
    ServiceManager serviceManager = new ServiceManager();

    public ControlPresenter(LoggerViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void didChangeLoggingState(Uri trackUri, int loggingState) {
        viewModel.setState(loggingState);
    }

    @Override
    protected void didConnectService(ServiceManager serviceManager) {
        viewModel.setState(serviceManager.getLoggingState());
    }

    public void onClickLeft() {
        if (viewModel.getState() == STATE_LOGGING) {
            stopLogging();
        } else if (viewModel.getState() == STATE_PAUSED) {
            stopLogging();
        }
    }

    public void onClickRight() {
        if (viewModel.getState() == STATE_STOPPED) {
            startLogging();
        } else if (viewModel.getState() == STATE_LOGGING) {
            pauseLogging();
        } else if (viewModel.getState() == STATE_PAUSED) {
            resumeLogging();
        }
    }

    public void startLogging() {
        serviceManager.startGPSLogging(getContext(), "New NG track!");
    }

    public void stopLogging() {
        serviceManager.stopGPSLogging(getContext());
    }

    public void pauseLogging() {
        serviceManager.pauseGPSLogging(getContext());
    }

    public void resumeLogging() {
        serviceManager.resumeGPSLogging(getContext());
    }


}
