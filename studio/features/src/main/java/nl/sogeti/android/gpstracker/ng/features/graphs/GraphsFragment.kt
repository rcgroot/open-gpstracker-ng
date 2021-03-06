/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.features.graphs

import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.service.util.PermissionRequester
import nl.sogeti.android.opengpstrack.ng.features.R
import nl.sogeti.android.opengpstrack.ng.features.databinding.FragmentGraphsBinding
import javax.inject.Inject

class GraphsFragment : Fragment() {

    @Inject
    lateinit var permissionRequester: PermissionRequester
    private lateinit var presenter: GraphsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter = ViewModelProviders.of(this).get(GraphsPresenter::class.java)
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentGraphsBinding>(inflater, R.layout.fragment_graphs, container, false)
        binding.viewModel = presenter.viewModel
        binding.presenter = presenter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        permissionRequester.start(this) { presenter.start() }
    }

    override fun onStop() {
        presenter.stop()
        permissionRequester.stop()
        super.onStop()
    }

    companion object {

        @JvmStatic
        fun newInstance(): GraphsFragment {
            return GraphsFragment()
        }
    }
}
