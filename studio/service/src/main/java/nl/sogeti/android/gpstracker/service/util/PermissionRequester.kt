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
package nl.sogeti.android.gpstracker.service.util

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import nl.sogeti.android.gpstracker.service.BuildConfig.controlPermission
import nl.sogeti.android.gpstracker.service.BuildConfig.tracksPermission
import nl.sogeti.android.gpstracker.service.R
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration
import nl.sogeti.android.gpstracker.utils.PermissionChecker
import java.util.*
import javax.inject.Inject

/**
 * Asks for Open GPS tracker permissions
 */
class PermissionRequester {

    companion object {
        private const val REQUEST_tracksPermission = 10001
        private const val INSTALL_URI = "https://play.google.com/store/apps/details?id=nl.sogeti.android.gpstracker"

        private val runnables = LinkedHashMap<PermissionRequester, () -> Unit>()
        private var permissionDialog: AlertDialog? = null
        private var installDialog: AlertDialog? = null
        private var missingDialog: AlertDialog? = null
        private var request: Array<String> = emptyArray()

        private val isShowingDialog: Boolean
            get() = arrayOf(permissionDialog, installDialog, missingDialog)
                    .fold(false) { acc, dialog -> acc || dialog != null }
    }

    @Inject
    lateinit var permissionChecker: PermissionChecker

    init {
        ServiceConfiguration.serviceComponent.inject(this)
    }

    fun start(fragment: androidx.fragment.app.Fragment, runnable: () -> Unit) {
        runnables[this] = runnable
        checkOpenGPSTrackerAccess(fragment)
    }

    fun stop() {
        runnables.remove(this)
        if (runnables.isEmpty()) {
            installDialog?.dismiss()
            installDialog = null
            permissionDialog?.dismiss()
            permissionDialog = null
        }
    }

    private fun checkOpenGPSTrackerAccess(fragment: androidx.fragment.app.Fragment) {
        val activity = fragment.activity
                ?: throw IllegalStateException("Unable to check permission in contextless fragment")
        val startRequest = DialogInterface.OnClickListener { _, _ -> showRequest(fragment) }
        val cancel = DialogInterface.OnClickListener { _, _ -> cancel() }

        if (hasPermission(activity as Context, tracksPermission)
                && hasPermission(activity, controlPermission)
                && hasPermission(activity, ACCESS_FINE_LOCATION)) {
            // Have permissions
            didReceivePermissions()
        } else if (canAsk(activity, tracksPermission)
                && canAsk(activity, controlPermission)
                && canAsk(activity, ACCESS_FINE_LOCATION)) {
            // Ask permissions
            showRequest(fragment)
        } else {
            // Explain permissions
            showRationale(activity, startRequest, cancel)
        }
    }

    fun onRequestPermissionsResult(fragment: androidx.fragment.app.Fragment, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val activity = fragment.activity
                ?: throw IllegalStateException("Unable to check permission in contextless fragment")
        if (requestCode == REQUEST_tracksPermission) {
            synchronized(request) {
                request = emptyArray()
                val grants = grantResults.indices
                        .filter { grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .map { permissions[it] }
                if (grants.contains(tracksPermission)
                        && grants.contains(controlPermission)
                        && grants.contains(ACCESS_FINE_LOCATION)) {
                    didReceivePermissions()
                } else {
                    val missing = grantResults.indices
                            .filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
                            .map { permissions[it] }
                    val ok = DialogInterface.OnClickListener { _, _ -> checkOpenGPSTrackerAccess(fragment) }
                    showMissing(activity, missing, ok)
                }
            }
        }
    }

    private fun didReceivePermissions() {
        for (runnable in runnables.values) {
            runnable()
        }
        runnables.clear()
    }

    private fun cancel() {
        installDialog = null
        permissionDialog = null
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

    private fun showRequest(fragment: androidx.fragment.app.Fragment) {
        synchronized(request) {
            permissionDialog?.dismiss()
            permissionDialog = null
            if (request.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request = arrayOf(tracksPermission, controlPermission, ACCESS_FINE_LOCATION)
                fragment.requestPermissions(request, REQUEST_tracksPermission)
            }
        }
    }

    private fun showMissing(context: Context, missing: List<String>, ok: DialogInterface.OnClickListener) {
        if (!isShowingDialog) {
            val permissions = missing.fold("") { description: String, permission: String -> description + ", $permission" }
            missingDialog = AlertDialog.Builder(context)
                    .setMessage("Missing $permissions")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, ok)
                    .setOnDismissListener { missingDialog = null }
                    .show()
        }
    }

    private fun installOpenGpsTracker(context: Context) {
        installDialog = null
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
