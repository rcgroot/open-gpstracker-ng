package nl.sogeti.android.gpstracker.v2.sharedwear.util

import android.content.res.Resources
import androidx.annotation.StringRes

fun Resources.getFloat(@StringRes resourceId: Int): Float {
    val stringValue: String? = this.getString(resourceId)
    checkNotNull(stringValue)

    return stringValue.toFloat()
}
