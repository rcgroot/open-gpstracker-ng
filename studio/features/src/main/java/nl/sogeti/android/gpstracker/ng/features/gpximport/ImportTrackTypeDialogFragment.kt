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
package nl.sogeti.android.gpstracker.ng.features.gpximport

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.utils.FragmentResultLambda
import nl.sogeti.android.opengpstrack.ng.features.R
import nl.sogeti.android.opengpstrack.ng.features.databinding.FragmentImportTracktypeDialogBinding

class ImportTrackTypeDialogFragment : DialogFragment() {

    private lateinit var presenter: ImportTrackTypePresenter

    fun show(manager: FragmentManager, tag: String, resultLambda: (String) -> Unit) {
        val lambdaHolder = FragmentResultLambda<String>()
        lambdaHolder.resultLambda = resultLambda
        manager.beginTransaction().add(lambdaHolder, TAG_LAMBDA_FRAGMENT).commit()
        setTargetFragment(lambdaHolder, 324)

        super.show(manager, tag)
    }

    override fun dismiss() {
        fragmentManager!!
                .beginTransaction()
                .remove(fragmentManager!!.findFragmentByTag(TAG_LAMBDA_FRAGMENT))
                .commit()
        super.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ViewModelProviders.of(this).get(ImportTrackTypePresenter::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentImportTracktypeDialogBinding>(inflater, R.layout.fragment_import_tracktype_dialog, container, false)
        val importTrackTypePresenter = ImportTrackTypePresenter()
        binding.presenter = importTrackTypePresenter
        binding.model = presenter.model
        presenter.model.dismiss.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (sender is ObservableBoolean && sender.get()) {
                    dismiss()
                }
            }
        })
        binding.fragmentImporttracktypeSpinner.onItemSelectedListener = importTrackTypePresenter.onItemSelectedListener
        if (targetFragment is FragmentResultLambda<*>) {
            importTrackTypePresenter.resultLambda = (targetFragment as FragmentResultLambda<String>).resultLambda
        }
        presenter = importTrackTypePresenter

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

    companion object {

        private const val TAG_LAMBDA_FRAGMENT = "TAG_LAMBDA_FRAGMENT"
    }
}
