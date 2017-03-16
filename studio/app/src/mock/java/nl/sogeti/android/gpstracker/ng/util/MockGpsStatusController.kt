package nl.sogeti.android.gpstracker.ng.util

import android.os.Handler
import android.os.Looper
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController

typealias Action = (GpsStatusController.Listener) -> Unit

class MockGpsStatusController(val listener: GpsStatusController.Listener) : GpsStatusController {
    private val commands = listOf<Action>(
            { it.onStart() },
            { it.onChange(0, 0) },
            { it.onChange(0, 8) },
            { it.onChange(1, 10) },
            { it.onChange(3, 12) },
            { it.onChange(5, 11) },
            { it.onChange(7, 14) },
            { it.onChange(9, 21) },
            { it.onFirstFix() },
            { it.onStop() }
    )

    private var handler: Handler? = null

    override fun startUpdates() {
        handler = Handler(Looper.getMainLooper())
        nextCommand(0)
    }

    override fun stopUpdates() {
        handler = null
    }

    private fun scheduleNextCommand(i: Int) {
        handler?.postDelayed({ nextCommand(i) }, 1500)
    }

    private fun nextCommand(i: Int) {
        handler?.let {
            commands[i](listener)
            val next = if (i < (commands.count() - 1)) i + 1 else 0
            scheduleNextCommand(next)
        }
    }
}