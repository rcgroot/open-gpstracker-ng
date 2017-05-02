package nl.sogeti.android.gpstracker.ng.graphs

import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.utils.PermissionRequester
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentGraphsBinding

class GraphsFragment : Fragment() {

    private lateinit var viewModel: GraphsViewModel
    private lateinit var graphPresenter: GraphsPresenter
    private var permissionRequester = PermissionRequester()

    companion object {
        private val ARG_TRACK_URI = "ARG_TRACK_URI"

        fun newInstance(trackUri: Uri): Fragment {
            val fragment = GraphsFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARG_TRACK_URI, trackUri)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trackUri = arguments.getParcelable<Uri>(ARG_TRACK_URI)
        viewModel = GraphsViewModel(trackUri)
        graphPresenter = GraphsPresenter(viewModel)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentGraphsBinding>(inflater, R.layout.fragment_graphs, container, false)
        binding.viewModel = viewModel
        binding.presenter = graphPresenter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        permissionRequester.start(this, { graphPresenter.start(activity) })
    }

    override fun onStop() {
        super.onStop()
        graphPresenter.stop()
        permissionRequester.stop()
    }
}
