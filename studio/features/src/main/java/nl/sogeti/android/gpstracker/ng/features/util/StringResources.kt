package nl.sogeti.android.gpstracker.ng.features.util

import android.support.annotation.StringRes


sealed class Text {
    data class String(@StringRes val id: Int)
    data class Plural(@StringRes val id: Int)
    class StringFormat(@StringRes val id: Int, vararg formatArgs: Any )

}
