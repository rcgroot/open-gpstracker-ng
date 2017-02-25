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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import static nl.sogeti.android.gpstracker.integration.ServiceConstants.permission.TRACKING_CONTROL;
import static nl.sogeti.android.gpstracker.integration.ServiceConstants.permission.TRACKING_HISTORY;

/**
 * Asks for Open GPS tracker permissions
 */
public class PermissionRequester implements DialogInterface.OnClickListener {

    private static final int REQUEST_TRACKING_CONTROL = 10000001;
    private static final String INSTALL_URI = "https://play.google.com/store/apps/details?id=nl.sogeti.android.gpstracker";

    private static boolean isShowingDialog = false;
    private AlertDialog permissionDialog;
    private AlertDialog installDialog;
    private Activity activity;
    private Runnable runnable;

    public void checkPermissions(final Activity _activity, Runnable _runnable) {
        this.activity = _activity;
        this.runnable = _runnable;

        if (new ServiceManager().isPackageInstalled(activity)) {
            if (isMissing(TRACKING_CONTROL) || isMissing(TRACKING_HISTORY)) {
                if (shouldExplain(TRACKING_CONTROL) || shouldExplain(TRACKING_HISTORY)) {
                    if (!isShowingDialog) {
                        isShowingDialog = true;
                        permissionDialog = new AlertDialog.Builder(activity)
                                .setMessage(R.string.permission_explain_need_control)
                                .setNegativeButton(android.R.string.cancel, this)
                                .setPositiveButton(android.R.string.ok, this)
                                .show();
                    }
                } else {
                    executePermissionsRequest();
                }
            } else {
                runnable.run();
            }
        } else if (!isShowingDialog) {
            isShowingDialog = true;
            installDialog = new AlertDialog.Builder(activity)
                    .setTitle(R.string.permission_missing_title)
                    .setMessage(R.string.permission_missing_message)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setPositiveButton(R.string.permission_button_install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isShowingDialog = false;
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(INSTALL_URI));
                            activity.startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    /**
     * Remove dialogs and context references
     */
    public void stop() {
        if (installDialog != null) {
            installDialog.dismiss();
            installDialog = null;
        }
        if (permissionDialog != null) {
            permissionDialog.dismiss();
            permissionDialog = null;
        }
        runnable = null;
        activity = null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        isShowingDialog = false;
        executePermissionsRequest();
    }

    private boolean shouldExplain(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    private boolean isMissing(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED;
    }

    private void executePermissionsRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{TRACKING_CONTROL, TRACKING_HISTORY}, REQUEST_TRACKING_CONTROL);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_TRACKING_CONTROL) {
            for (int i = 0; i < permissions.length; i++) {
                if (TRACKING_CONTROL.equals(permissions[i])
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    runnable.run();
                }
            }
        }
    }
}
