package nl.sogeti.android.gpstracker.ng.trackdelete

import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentDeleteDialogBinding

class TrackDeleteDialogFragment : DialogFragment(), TrackDeleteModel.View {

    companion object {
        private val ARG_URI = "ARGUMENT_URI"
        fun newInstance(uri: Uri): TrackDeleteDialogFragment {
            val arguments = Bundle()
            arguments.putParcelable(ARG_URI, uri)
            val fragment = TrackDeleteDialogFragment()
            fragment.arguments = arguments

            return fragment
        }
    }

    private var presenter: TrackDeletePresenter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentDeleteDialogBinding>(inflater, R.layout.fragment_delete_dialog, container, false)

        val uri = arguments.get(TrackDeleteDialogFragment.ARG_URI) as Uri
        val model = TrackDeleteModel(uri)
        val presenter = TrackDeletePresenter(model, this)
        binding.model = model
        binding.presenter = presenter
        this.presenter = presenter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        presenter?.start(activity)
    }

    override fun onStop() {
        super.onStop()
        presenter?.stop()
    }
}