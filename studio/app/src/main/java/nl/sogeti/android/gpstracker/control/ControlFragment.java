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

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.sogeti.android.gpstracker.integration.ExternalConstants;
import nl.sogeti.android.gpstracker.integration.GPSLoggerServiceManager;
import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.FragmentControlBinding;

/**
 * On screen controls for the logging state
 */
public class ControlFragment extends Fragment implements ControlHandler.Listener, DialogInterface.OnClickListener {

    private static final int REQUEST_TRACKING_CONTROL = 10000001;

    private FragmentControlBinding binding;
    private LoggerViewModel logger;
    private GPSLoggerServiceManager serviceManager;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLogger();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = new GPSLoggerServiceManager();
        connectToService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_control, container, false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkTrackingPermission();
    }

    @Override
    public void onDestroy() {
        disconnectService();
        super.onDestroy();
    }

    private void connectToService() {
        serviceManager.startup(getActivity(), new Runnable() {
            @Override
            public void run() {
                updateLogger();
            }
        });
        IntentFilter filter = new IntentFilter(ExternalConstants.LOGGING_STATE_CHANGED_ACTION);
        getActivity().getApplicationContext().registerReceiver(receiver, filter);
    }

    private void disconnectService() {
        serviceManager.shutdown(getActivity());
        getActivity().getApplicationContext().unregisterReceiver(receiver);
    }

    private void updateLogger() {
        if (logger == null) {
            logger = new LoggerViewModel();
            ControlHandler handler = new ControlHandler(this, logger);
            binding.setHandler(handler);
            binding.setLogger(logger);
        }

        logger.setState(serviceManager.getLoggingState());
    }

    private void checkTrackingPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), ExternalConstants.permission.TRACKING_CONTROL) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), ExternalConstants.permission.TRACKING_CONTROL)) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.permission_explain_need_control)
                        .setPositiveButton(android.R.string.ok, this)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                dialog.show();

            } else {
                executePermissionsRequest();
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        executePermissionsRequest();
    }

    private void executePermissionsRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ExternalConstants.permission.TRACKING_CONTROL}, REQUEST_TRACKING_CONTROL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_TRACKING_CONTROL) {
            for (int i = 0; i < permissions.length; i++) {
                if (ExternalConstants.permission.TRACKING_CONTROL.equals(permissions[i])
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    connectToService();
                }
            }
        }
    }

    @Override
    public void startLogging() {
        GPSLoggerServiceManager.startGPSLogging(getActivity(), "New NG track!");
    }

    @Override
    public void stopLogging() {
        GPSLoggerServiceManager.stopGPSLogging(getActivity());
    }

    @Override
    public void pauseLogging() {
        GPSLoggerServiceManager.pauseGPSLogging(getActivity());
    }

    @Override
    public void resumeLogging() {
        GPSLoggerServiceManager.resumeGPSLogging(getActivity());
    }
}
