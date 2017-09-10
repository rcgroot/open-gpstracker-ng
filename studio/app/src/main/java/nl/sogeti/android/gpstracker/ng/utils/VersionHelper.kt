package nl.sogeti.android.gpstracker.ng.utils

import android.os.Build

class VersionHelper {

    fun isAtLeast(minimalVersion: Int): Boolean = Build.VERSION.SDK_INT >= minimalVersion
}
