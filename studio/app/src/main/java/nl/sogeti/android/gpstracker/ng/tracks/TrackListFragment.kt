package nl.sogeti.android.gpstracker.ng.tracks

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.binders.MyBindingComponent
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentTracklistBinding

/**
 * Sets up display and selection of tracks in a list style
 */
class TrackListFragment : Fragment() {

    private var tracksAdapter : TracksAdapter? = null

    private var model : TracksModel? = null

    private var binding: FragmentTracklistBinding? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val model = TracksModel()
        val tracksAdapter = TracksAdapter(model)
        val binding = DataBindingUtil.inflate<FragmentTracklistBinding>(inflater, R.layout.fragment_tracklist, container, false)
        binding.listview.adapter = tracksAdapter
        binding.listview.layoutManager = LinearLayoutManager(this.context)
        binding.model = model
        this.binding = binding
        this.model = model
        this.tracksAdapter = tracksAdapter

        return binding.root
    }
}