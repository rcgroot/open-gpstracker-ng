package nl.sogeti.android.gpstracker.ng.rules

import org.mockito.Mockito


//region any() as seen on https://medium.com/@elye.project/befriending-kotlin-and-mockito-1c2e7b0ef791#.o6fwp9tpe
public fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T
//endregion
