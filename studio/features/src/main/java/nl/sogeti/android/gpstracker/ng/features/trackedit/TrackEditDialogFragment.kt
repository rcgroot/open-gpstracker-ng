package nl.sogeti.android.gpstracker.ng.features.trackedit

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.features.databinding.FeaturesBindingComponent
import nl.sogeti.android.gpstracker.v2.sharedwear.util.observe
import nl.sogeti.android.opengpstrack.ng.features.R
import nl.sogeti.android.opengpstrack.ng.features.databinding.FragmentEditDialogBinding

class TrackEditDialogFragment : DialogFragment(), TrackEditModel.View {

    private var presenter: TrackEditPresenter? = null
    private var binding: FragmentEditDialogBinding? = null

    companion object {
        private const val ARG_URI = "ARGUMENT_URI"
        fun newInstance(uri: Uri): TrackEditDialogFragment {
            val arguments = Bundle()
            arguments.putParcelable(ARG_URI, uri)
            val fragment = TrackEditDialogFragment()
            fragment.arguments = arguments

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentEditDialogBinding>(inflater, R.layout.fragment_edit_dialog, container, false, FeaturesBindingComponent())

        val uri = arguments?.get(ARG_URI) as Uri
        val presenter = ViewModelProviders.of(this, TrackEditPresenter.newFactory(uri)).get(TrackEditPresenter::class.java)
        binding.model = presenter.model
        binding.presenter = presenter
        binding.spinner.onItemSelectedListener = presenter.onItemSelectedListener
        this.presenter = presenter
        this.binding = binding
        presenter.model.dismissed.observe { sender ->
            if (sender is ObservableBoolean && sender.get()) {
                dismiss()
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        presenter?.start()
    }

    override fun onStop() {
        super.onStop()
        presenter?.stop()
    }
}
