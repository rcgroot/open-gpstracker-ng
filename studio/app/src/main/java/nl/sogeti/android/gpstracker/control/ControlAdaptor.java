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
package nl.sogeti.android.gpstracker.control;

import android.content.Context;

import nl.sogeti.android.gpstracker.BaseTrackAdapter;
import nl.sogeti.android.gpstracker.integration.ServiceManager;

public class ControlAdaptor extends BaseTrackAdapter implements ControlHandler.Listener {
    private final LoggerViewModel viewModel;

    public ControlAdaptor(LoggerViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void start(Context context) {
        super.start(context, true);
    }

    @Override
    public void didConnectService() {
        viewModel.setState(getServiceManager().getLoggingState());
    }

    @Override
    public void startLogging() {
        ServiceManager.startGPSLogging(getContext(), "New NG track!");
    }

    @Override
    public void stopLogging() {
        ServiceManager.stopGPSLogging(getContext());
    }

    @Override
    public void pauseLogging() {
        ServiceManager.pauseGPSLogging(getContext());
    }

    @Override
    public void resumeLogging() {
        ServiceManager.resumeGPSLogging(getContext());
    }


}
