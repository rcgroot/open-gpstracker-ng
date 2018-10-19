package nl.sogeti.android.gpstracker.utils

class Consumable<T>(private var value: T?) {

    val consumed: Boolean
        get() = value != null


    fun consume(consumer: (T) -> Unit) {
        this.value?.let {
            this.value = null
            consumer(it)
        }
    }
}
