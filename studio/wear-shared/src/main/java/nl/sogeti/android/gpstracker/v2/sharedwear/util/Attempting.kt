package nl.sogeti.android.gpstracker.v2.sharedwear.util

import kotlin.reflect.KProperty

class Trying<T>(private val trying: () -> T?) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (value == null) {
            value = this.trying()
        }
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun <T> trying(trying: () -> T): Trying<T?> = Trying(trying)
