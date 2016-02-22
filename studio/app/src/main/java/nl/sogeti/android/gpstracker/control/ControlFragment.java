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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.integration.ServiceManager;
import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.FragmentControlBinding;

/**
 * On screen controls for the logging state
 */
public class ControlFragment extends Fragment implements DialogInterface.OnClickListener {

    private static final int REQUEST_TRACKING_CONTROL = 10000001;

    private FragmentControlBinding binding;
    private ControlAdaptor controlAdaptor;
    private LoggerViewModel viewModel;
    private ControlHandler handler;
    private AlertDialog installDialog;
    private AlertDialog permissionDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LoggerViewModel();
        controlAdaptor = new ControlAdaptor(viewModel);
        handler = new ControlHandler(controlAdaptor, viewModel);
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
        checkTrackingPermission();
    }

    @Override
    public void onPause() {
        removePermissionDialogs();
        controlAdaptor.stop();
        super.onPause();
    }

    private void removePermissionDialogs() {
        if (installDialog != null) {
            installDialog.dismiss();
        }
        if (permissionDialog != null) {
            permissionDialog.dismiss();
        }
    }

    private void checkTrackingPermission() {
        if (ServiceManager.isPackageInstalled(getActivity())) {
            if (ContextCompat.checkSelfPermission(getActivity(), ServiceConstants.permission.TRACKING_CONTROL) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), ServiceConstants.permission.TRACKING_CONTROL)) {
                    permissionDialog = new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.permission_explain_need_control)
                            .setPositiveButton(android.R.string.ok, this)
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                } else {
                    executePermissionsRequest();
                }
            } else {
                controlAdaptor.start(getActivity());
            }
        } else {
            installDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.permission_missing_title)
                    .setMessage(R.string.permission_missing_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.permission_button_install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity().getString(R.string.permission_install_uri)));
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        executePermissionsRequest();
    }

    private void executePermissionsRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ServiceConstants.permission.TRACKING_CONTROL}, REQUEST_TRACKING_CONTROL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_TRACKING_CONTROL) {
            for (int i = 0; i < permissions.length; i++) {
                if (ServiceConstants.permission.TRACKING_CONTROL.equals(permissions[i])
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    controlAdaptor.start(getActivity());
                }
            }
        }
    }
}
