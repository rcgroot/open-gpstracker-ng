package nl.sogeti.android.gpstracker.ng.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import nl.sogeti.android.gpstracker.ng.utils.PermissionChecker

class MockPermissionChecker : PermissionChecker() {

    override fun checkSelfPermission(context: Context, permission: String)
            = PackageManager.PERMISSION_GRANTED

    override fun shouldShowRequestPermissionRationale(activity: Activity, permission: String)
            = false
}