package nl.sogeti.android.gpstracker.v2.wear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class Control : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        // Enables Always-on
        setAmbientEnabled()
    }
}
