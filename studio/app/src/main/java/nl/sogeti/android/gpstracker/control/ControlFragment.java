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

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.sogeti.android.gpstracker.integration.PermissionRequestor;
import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.FragmentControlBinding;

/**
 * On screen controls for the logging state
 */
public class ControlFragment extends Fragment  {

    private FragmentControlBinding binding;
    private ControlAdaptor controlAdaptor;
    private LoggerViewModel viewModel;
    private ControlHandler handler;
    private PermissionRequestor permissionRequestor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LoggerViewModel();
        controlAdaptor = new ControlAdaptor(viewModel);
        handler = new ControlHandler(controlAdaptor, viewModel);
        permissionRequestor = new PermissionRequestor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_control, container, false);
        binding.setLogger(viewModel);
        binding.setHandler(handler);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        permissionRequestor.checkTrackingPermission(getActivity(), new Runnable() {
            @Override
            public void run() {
                controlAdaptor.start(getActivity());
            }
        });
    }

    @Override
    public void onPause() {
        permissionRequestor.stop();
        controlAdaptor.stop();
        super.onPause();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionRequestor.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
