package nl.sogeti.android.gpstracker.ng.trackedit

import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentEditDialogBinding

class TrackEditDialogFragment : DialogFragment(), TrackEditModel.View {

    private var model: TrackEditModel? = null
    private var presenter: TrackEditPresenter? = null
    private var binding: FragmentEditDialogBinding? = null

    companion object {
        private val ARG_URI = "ARGUMENT_URI"
        fun newInstance(uri: Uri): TrackEditDialogFragment {
            val arguments = Bundle()
            arguments.putParcelable(ARG_URI, uri)
            val fragment = TrackEditDialogFragment()
            fragment.arguments = arguments

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentEditDialogBinding>(inflater, R.layout.fragment_edit_dialog, container, false)

        val uri = arguments.get(ARG_URI) as Uri
        val model = TrackEditModel(uri)
        val presenter = TrackEditPresenter(model, this)
        binding.model = model
        binding.presenter = presenter
        binding.spinner.onItemSelectedListener = presenter.onItemSelectedListener
        this.model = model
        this.presenter = presenter
        this.binding = binding

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