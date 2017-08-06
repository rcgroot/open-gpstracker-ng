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
package nl.sogeti.android.gpstracker.ng.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import nl.sogeti.android.gpstracker.integration.ServiceConstants.permission.TRACKING_CONTROL
import nl.sogeti.android.gpstracker.integration.ServiceConstants.permission.TRACKING_HISTORY
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.v2.R
import java.util.*
import javax.inject.Inject

/**
 * Asks for Open GPS tracker permissions
 */
class PermissionRequester {

    companion object shared {
        private val runnables = LinkedHashMap<PermissionRequester, () -> Unit>()
        private var permissionDialog: AlertDialog? = null
        private var installDialog: AlertDialog? = null
        private var request: Array<String> = emptyArray()

        private val isShowingDialog: Boolean
            get() = permissionDialog != null || installDialog != null
    }

    private val REQUEST_TRACKING_CONTROL = 10001
    private val INSTALL_URI = "https://play.google.com/store/apps/details?id=nl.sogeti.android.gpstracker"

    @Inject
    lateinit var serviceManager: ServiceManagerInterface
    @Inject
    lateinit var permissionChecker: PermissionChecker

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun start(fragment: Fragment, runnable: () -> Unit) {
        shared.runnables.put(this, runnable)
        checkOpenGPSTrackerAccess(fragment)
    }

    fun stop() {
        shared.runnables.remove(this)
        if (runnables.isEmpty()) {
            shared.installDialog?.dismiss()
            shared.installDialog = null
            shared.permissionDialog?.dismiss()
            shared.permissionDialog = null
        }
    }

    fun checkOpenGPSTrackerAccess(fragment: Fragment) {
        val startRequest = DialogInterface.OnClickListener { _, _ -> showRequest(fragment) }
        val install = DialogInterface.OnClickListener { _, _ -> installOpenGpsTracker(fragment.context) }
        val cancel = DialogInterface.OnClickListener { _, _ -> cancel() }

        if (serviceManager.isPackageInstalled(fragment.context)) {
            // Installed, check permissions
            if (hasPermission(fragment.context, TRACKING_CONTROL) && hasPermission(fragment.context, TRACKING_HISTORY)) {
                // Have permissions
                didReceivePermissions()
            } else if (canAsk(fragment.activity, TRACKING_CONTROL)) {
                // Ask permissions
                showRequest(fragment)
            } else {
                // Explain permissions
                showRationale(fragment.context, startRequest, cancel)
            }
        } else if (!shared.isShowingDialog) {
            showInstallLink(fragment.context, cancel, install)
        }
    }

    fun onRequestPermissionsResult(fragment: Fragment, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_TRACKING_CONTROL) {
            synchronized(request, {
                request = emptyArray()
                val grants = grantResults.indices
                        .filter { grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .map { permissions[it] }
                if (grants.contains(TRACKING_CONTROL) && grants.contains(TRACKING_HISTORY)) {
                    didReceivePermissions()
                } else {
                    checkOpenGPSTrackerAccess(fragment)
                }
            })
        }
    }

    private fun didReceivePermissions() {
        for (runnable in shared.runnables.values) {
            runnable()
        }
        shared.runnables.clear()
    }

    private fun cancel() {
        shared.installDialog = null
        shared.permissionDialog = null
    }

    private fun showInstallLink(context: Context, cancel: DialogInterface.OnClickListener, okListener: DialogInterface.OnClickListener) {
        installDialog = AlertDialog.Builder(context)
                .setTitle(R.string.permission_missing_title)
                .setMessage(R.string.permission_missing_message)
                .setNegativeButton(android.R.string.cancel, cancel)
                .setPositiveButton(R.string.permission_button_install, okListener)
                .show()
    }

    private fun showRationale(context: Context, okListener: DialogInterface.OnClickListener, cancel: DialogInterface.OnClickListener) {
        if (!isShowingDialog) {
            permissionDialog = AlertDialog.Builder(context)
                    .setMessage(R.string.permission_explain_need_control)
                    .setNegativeButton(android.R.string.cancel, cancel)
                    .setPositiveButton(android.R.string.ok, okListener)
                    .show()
        }
    }

    private fun showRequest(fragment: Fragment) {
        synchronized(request, {
            shared.permissionDialog = null
            if (request.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request = arrayOf(TRACKING_CONTROL, TRACKING_HISTORY)
                fragment.requestPermissions(request, REQUEST_TRACKING_CONTROL)
            }
        })
    }

    private fun installOpenGpsTracker(context: Context) {
        shared.installDialog = null
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTALL_URI))
        context.startActivity(intent)
    }

    private fun canAsk(activity: Activity, permission: String): Boolean {
        return !permissionChecker.shouldShowRequestPermissionRationale(activity, permission)
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return permissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
