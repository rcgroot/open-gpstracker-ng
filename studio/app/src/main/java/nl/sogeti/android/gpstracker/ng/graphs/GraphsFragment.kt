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
package nl.sogeti.android.gpstracker.ng.graphs

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListFragment
import nl.sogeti.android.gpstracker.ng.utils.PermissionRequester
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentGraphsBinding

class GraphsFragment : Fragment() {

    private lateinit var graphPresenter: GraphsPresenter
    private var permissionRequester = PermissionRequester()

    companion object {
        fun newInstance(): GraphsFragment {
            return GraphsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        graphPresenter = GraphsPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentGraphsBinding>(inflater, R.layout.fragment_graphs, container, false)
        binding.viewModel = graphPresenter.viewModel
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
