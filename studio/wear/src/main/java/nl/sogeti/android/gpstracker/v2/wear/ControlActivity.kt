/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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
package nl.sogeti.android.gpstracker.v2.wear

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import android.support.wearable.activity.WearableActivity
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.model.StatusMessage
import nl.sogeti.android.gpstracker.v2.wear.databinding.ActivityControlBinding
import nl.sogeti.android.gpstracker.v2.wear.databinding.WearBindingComponent

private const val EXTRA_STATISTICS = "EXTRA_STATISTICS"
private const val EXTRA_STATUS = "EXTRA_STATUS"

class ControlActivity : WearableActivity() {
    private var binding: ActivityControlBinding? = null

    private lateinit var presenter: ControlPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = HolderFragment.of(this, ControlPresenter::class.java)
        presenter.viewModel.ambient.observe { sender ->
            if (sender is ObservableBoolean && sender.get()) {
                darken()
            } else if (sender is ObservableBoolean && !sender.get()) {
                brighter()
            }
        }
        presenter.viewModel.confirmAction.observe { sender ->
            if (sender is ObservableField<*> && sender.get() != null) {
                startConfirmTimer()
            } else if (sender is ObservableField<*> && sender.get() == null) {
                cancelConfirmTimer()
            }
        }
        presenter.viewModel.scrollToPage.observe { sender ->
            if (sender is ObservableInt) {
                binding?.wearControlVertical?.smoothScrollToPosition(sender.get())
            }
        }

        val binding = DataBindingUtil.setContentView<ActivityControlBinding>(this, R.layout.activity_control, WearBindingComponent())
        this.binding = binding
        binding.presenter = presenter
        binding.viewModel = presenter.viewModel
        binding.wearControlVertical.adapter = VerticalControlAdapter(presenter.viewModel, presenter)
        binding.wearControlVertical.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val pagerSnapHelper = androidx.recyclerview.widget.PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.wearControlVertical)

        setAmbientEnabled() // Enables Always-on

        if (savedInstanceState == null) {
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent) {
        if (intent.hasExtra(EXTRA_STATISTICS)) {
            presenter.didReceiveStatistics(intent.getParcelableExtra(EXTRA_STATISTICS))
        } else if (intent.hasExtra(EXTRA_STATUS)) {
            presenter.didReceiveStatus(intent.getParcelableExtra(EXTRA_STATUS))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        presenter.enterAmbient()
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
        presenter.exitAmbient()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    //region View

    fun darken() {
        binding?.wearControlBackground?.setBackgroundColor(getColor(R.color.black))
    }

    fun brighter() {
        binding?.wearControlBackground?.setBackgroundColor(getColor(R.color.dark_grey))
    }

    fun startConfirmTimer() {
        binding?.circularProgress?.totalTime = 2000
        binding?.circularProgress?.startTimer()
    }

    fun cancelConfirmTimer() {
        binding?.circularProgress?.stopTimer()
    }

    //endregion

    companion object {
        @JvmStatic
        fun createIntent(context: Context, statistics: StatisticsMessage): Intent {
            val intent = Intent(context, ControlActivity::class.java)
            intent.putExtra(EXTRA_STATISTICS, statistics)

            return intent
        }

        @JvmStatic
        fun createIntent(context: Context, status: StatusMessage): Intent {
            val intent = Intent(context, ControlActivity::class.java)
            intent.putExtra(EXTRA_STATUS, status)

            return intent
        }
    }
}
