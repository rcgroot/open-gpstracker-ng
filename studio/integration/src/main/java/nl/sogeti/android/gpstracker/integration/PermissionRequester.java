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
package nl.sogeti.android.gpstracker.integration;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.LinkedList;
import java.util.List;

import static nl.sogeti.android.gpstracker.integration.ServiceConstants.permission.TRACKING_CONTROL;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.permission.TRACKING_HISTORY;

/**
 * Asks for Open GPS tracker permissions
 */
public class PermissionRequester {

    private static final int REQUEST_TRACKING_CONTROL = 10001;
    private static final String INSTALL_URI = "https://play.google.com/store/apps/details?id=nl.sogeti.android.gpstracker";
    private static final PermissionRequestingState shared = new PermissionRequestingState();

    static class PermissionRequestingState {
        private List<Runnable> runnables = new LinkedList<>();
        private AlertDialog permissionDialog;
        private AlertDialog installDialog;

        private boolean isShowingDialog() {
            return permissionDialog != null || installDialog != null;
        }
    }

    public void checkPermissions(final Activity activity, Runnable runnable) {
        Runnable request = new Runnable() {
            @Override
            public void run() {
                executePermissionsRequest(activity);
            }
        };
        checkPermissions(activity, shouldExplainAny(activity), request);
    }

    public void checkPermissions(final Fragment fragment, Runnable runnable) {
        shared.runnables.add(runnable);
        Runnable request = new Runnable() {
            @Override
            public void run() {
                executePermissionsRequest(fragment);
            }
        };
        checkPermissions(fragment.getContext(), shouldExplainAny(fragment.getActivity()), request);
    }

    public void checkPermissions(final Context context, final boolean shouldExplain, final Runnable request) {
        DialogInterface.OnClickListener install = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installOpenGpsTracker(context);
            }
        };
        DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel();
            }
        };
        DialogInterface.OnClickListener startRequest = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.run();
            }
        };

        if (new ServiceManager().isPackageInstalled(context)) {
            if (isMissing(context, TRACKING_CONTROL) || isMissing(context, TRACKING_HISTORY)) {
                if (shouldExplain) {
                    if (!shared.isShowingDialog()) {
                        shared.permissionDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.permission_explain_need_control)
                                .setNegativeButton(android.R.string.cancel, cancel)
                                .setPositiveButton(android.R.string.ok, startRequest)
                                .show();
                    }
                } else {
                    request.run();
                }
            } else {
                permissionGranted();
            }
        } else if (!shared.isShowingDialog()) {
            shared.installDialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.permission_missing_title)
                    .setMessage(R.string.permission_missing_message)
                    .setNegativeButton(android.R.string.cancel, cancel)
                    .setPositiveButton(R.string.permission_button_install, install)
                    .show();
        }
    }

    private void executePermissionsRequest(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{TRACKING_CONTROL, TRACKING_HISTORY}, REQUEST_TRACKING_CONTROL);
        }
    }

    private void executePermissionsRequest(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragment.requestPermissions(new String[]{TRACKING_CONTROL, TRACKING_HISTORY}, REQUEST_TRACKING_CONTROL);
        }
    }

    private void installOpenGpsTracker(Context context) {
        shared.installDialog = null;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(INSTALL_URI));
        context.startActivity(intent);
    }

    private void permissionGranted() {
        for (Runnable runnable : shared.runnables) {
            runnable.run();
        }
        shared.runnables.clear();
    }

    private void cancel() {
        shared.installDialog = null;
        shared.permissionDialog = null;
    }

    /**
     * Remove dialogs and context references
     */
    public void stop() {
        if (shared.installDialog != null) {
            shared.installDialog.dismiss();
            shared.installDialog = null;
        }
        if (shared.permissionDialog != null) {
            shared.permissionDialog.dismiss();
            shared.permissionDialog = null;
        }
        shared.runnables.clear();
    }

    private boolean shouldExplainAny(Activity activity) {
        return shouldExplain(activity, TRACKING_CONTROL) || shouldExplain(activity, TRACKING_HISTORY);
    }

    private boolean shouldExplain(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    private boolean isMissing(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_TRACKING_CONTROL) {
            for (int i = 0; i < permissions.length; i++) {
                if (TRACKING_CONTROL.equals(permissions[i])
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted();
                }
            }
        }
    }
}
