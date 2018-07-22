package nl.sogeti.android.gpstracker.ng.features.util

import java.util.concurrent.Executor

class ImmediateExecutor : Executor {

    override fun execute(command: Runnable?) {
        command?.run()
    }

}
