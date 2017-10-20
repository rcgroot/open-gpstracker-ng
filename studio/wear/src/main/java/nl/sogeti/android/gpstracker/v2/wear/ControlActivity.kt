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
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.wearable.activity.WearableActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout.HORIZONTAL
import nl.sogeti.android.gpstracker.v2.sharedwear.StatisticsMessage
import nl.sogeti.android.gpstracker.v2.sharedwear.StatusMessage
import nl.sogeti.android.gpstracker.v2.wear.VerticalViewHolder.ControlsViewHolder
import nl.sogeti.android.gpstracker.v2.wear.VerticalViewHolder.StatisticsViewHolder
import nl.sogeti.android.gpstracker.v2.wear.databinding.ActivityControlBinding
import nl.sogeti.android.gpstracker.v2.wear.databinding.ControlBindingComponent
import nl.sogeti.android.gpstracker.v2.wear.databinding.ItemControlListBinding
import nl.sogeti.android.gpstracker.v2.wear.databinding.ItemStatisticsBinding

private const val EXTRA_STATISTICS = "EXTRA_STATISTICS"
private const val EXTRA_STATUS = "EXTRA_STATUS"

class ControlActivity : WearableActivity(), View {

    private var binding: ActivityControlBinding? = null
    private val model = ControlViewModel()
    private var presenter: ControlPresenter = ControlPresenter(model, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityControlBinding>(this, R.layout.activity_control, ControlBindingComponent())
        this.binding = binding
        binding.presenter = presenter
        binding.viewModel = model
        binding.wearControlVertical.adapter = VerticalControlAdapter(model, presenter)
        binding.wearControlVertical.layoutManager = LinearLayoutManager(this)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.wearControlVertical)
        // Enables Always-on
        setAmbientEnabled()

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

    override fun onStart() {
        super.onStart()
        presenter.start(this)
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        presenter.enterAmbient()
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
        presenter.exitAmbient()
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    //region View

    override fun darken() {
        binding?.wearControlBackground?.setBackgroundColor(getColor(R.color.black))
    }

    override fun brighter() {
        binding?.wearControlBackground?.setBackgroundColor(getColor(R.color.dark_grey))
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

class VerticalControlAdapter(private val model: ControlViewModel, private val presenter: ControlPresenter) : RecyclerView.Adapter<VerticalViewHolder>() {

    var count = 0

    init {
        model.showControls.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(showControl: Observable, id: Int) {
                val show = (showControl as ObservableBoolean).get()
                if (count == 1 && show) {
                    notifyItemInserted(1)
                }
                else if (count == 2 && !show) {
                    notifyItemRemoved(1)
                }
                else {
                    notifyDataSetChanged()
                }
            }
        })
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemCount(): Int {
        count = if (model.showControls.get()) 2 else 1
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VerticalViewHolder? {
        val inflater = LayoutInflater.from(parent?.context)
        return when (viewType) {
            0 -> {
                val binding = DataBindingUtil.inflate<ItemStatisticsBinding>(inflater, R.layout.item_statistics, parent, false, ControlBindingComponent())
                StatisticsViewHolder(binding)
            }
            1 -> {
                val binding = DataBindingUtil.inflate<ItemControlListBinding>(inflater, R.layout.item_control_list, parent, false, ControlBindingComponent())
                binding.wearControlControlList.layoutManager = LinearLayoutManager(parent?.context, HORIZONTAL, false)
                val pagerSnapHelper = PagerSnapHelper()
                pagerSnapHelper.attachToRecyclerView(binding.wearControlControlList)
                ControlsViewHolder(binding)
            }
            else -> null
        }
    }

    override fun onBindViewHolder(holder: VerticalViewHolder?, position: Int) {
        when (holder) {
            is StatisticsViewHolder -> {
                holder.binding.presenter = presenter
                holder.binding.viewModel = model
            }
            is ControlsViewHolder -> {
                holder.binding.viewModel = model
                val adapter = ControlsAdapter(model, presenter)
                holder.binding.wearControlControlList.adapter = adapter
            }
        }
    }
}

sealed class VerticalViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    class StatisticsViewHolder(val binding: ItemStatisticsBinding) : VerticalViewHolder(binding.root)
    class ControlsViewHolder(val binding: ItemControlListBinding) : VerticalViewHolder(binding.root)
}
