package nl.sogeti.android.gpstracker.v2.sharedwear.util

import kotlin.reflect.KProperty

class Trying<T>(private val trying: () -> T) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val returnValue = value ?: trying()
        if (value == null) {
            value = returnValue
        }
        return returnValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun <S> trying(trying: () -> S): Trying<S> = Trying(trying)
