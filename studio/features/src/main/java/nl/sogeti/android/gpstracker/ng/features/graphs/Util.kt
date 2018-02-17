package nl.sogeti.android.gpstracker.ng.features.graphs

inline fun <T, R> List<T>.toDeltas(delta: (T, T) -> R): List<R> {
    return (0 until count() - 1).map { delta(this[it], this[it + 1]) }
}