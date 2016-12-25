package nl.sogeti.android.gpstracker.ng.utils

import android.os.Handler
import android.os.Looper

fun executeOnUiThread(item: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        item()
    } else {
        Handler(Looper.getMainLooper()).post { item }
    }
}