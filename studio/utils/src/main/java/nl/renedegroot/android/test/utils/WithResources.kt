package nl.renedegroot.android.test.utils

import java.io.Closeable

/**
 * Based on the description by Vladimir Bezhenar
 * https://discuss.kotlinlang.org/t/is-there-standard-way-to-use-multiple-resources/2613
 */
class Resources : Closeable {
    private val resources = mutableListOf<Closeable>()

    fun <T : Closeable> T.use(): T {
        resources += this
        return this
    }

    override fun close() {
        resources.reversed().forEach { resource ->
            try {
                resource.close()
            } catch (closeException: Exception) {
                //ignore
            }
        }
    }
}

inline fun <T> withResources(block: Resources.() -> T): T = Resources().use(block)
