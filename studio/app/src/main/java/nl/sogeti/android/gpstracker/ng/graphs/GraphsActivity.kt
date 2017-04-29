package nl.sogeti.android.gpstracker.ng.graphs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import nl.sogeti.android.gpstracker.v2.R

class GraphsActivity : AppCompatActivity() {

    companion object {
        private val EXTRA_TRACK_URI = "EXTRA_TRACK_URI"

        fun newIntent(context: Context, trackUri: Uri): Intent {
            val intent = Intent(context, GraphsActivity::class.java)
            intent.putExtra(EXTRA_TRACK_URI, trackUri)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val trackUri = intent.getParcelableExtra<Uri>(EXTRA_TRACK_URI)
            val fragment = GraphsFragment.newInstance(trackUri)
            supportFragmentManager.beginTransaction().replace(R.id.fragment_graphs_container, fragment).commit()
        }
    }

}
