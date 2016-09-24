package nl.sogeti.android.gpstracker.ng.util

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import nl.sogeti.android.gpstracker.v2.R

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val frameLayout = FrameLayout(this)
        frameLayout.id = R.id.title
        setContentView(frameLayout)
    }
}