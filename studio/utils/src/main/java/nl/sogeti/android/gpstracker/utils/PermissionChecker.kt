package nl.sogeti.android.gpstracker.utils

import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * replaces some framework static calls related to permission with injectable instance object
 */
open class PermissionChecker {

    open fun checkSelfPermission(context: Context, permission: String) =
            ContextCompat.checkSelfPermission(context, permission)

    open fun shouldShowRequestPermissionRationale(activity: Activity, permission: String) =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}
