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

import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.v2.wear.databinding.ControlBindingComponent
import nl.sogeti.android.gpstracker.v2.wear.databinding.ItemControlBinding
import nl.sogeti.android.gpstracker.v2.wear.databinding.ItemStatisticsBinding


class VerticalControlAdapter(private val model: ControlViewModel, private val presenter: ControlPresenter) : RecyclerView.Adapter<VerticalViewHolder>() {

    init {
        model.controls.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<Control>>() {
            override fun onItemRangeChanged(p0: ObservableList<Control>?, p1: Int, p2: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeInserted(p0: ObservableList<Control>?, p1: Int, p2: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(p0: ObservableList<Control>?, p1: Int, p2: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeMoved(p0: ObservableList<Control>?, p1: Int, p2: Int, p3: Int) {
                notifyDataSetChanged()
            }

            override fun onChanged(p0: ObservableList<Control>?) {
                notifyDataSetChanged()
            }
        })
    }

    override fun getItemViewType(position: Int) = if (position == 0) 0 else 1

    override fun getItemCount() = model.controls.size + 1

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VerticalViewHolder? {
        val inflater = LayoutInflater.from(parent?.context)
        return when (viewType) {
            0 -> {
                val binding = DataBindingUtil.inflate<ItemStatisticsBinding>(inflater, R.layout.item_statistics, parent, false, ControlBindingComponent())
                binding.presenter = presenter
                VerticalViewHolder.StatisticsViewHolder(binding)
            }
            else -> {
                val binding = DataBindingUtil.inflate<ItemControlBinding>(inflater, R.layout.item_control, parent, false, ControlBindingComponent())
                binding.presenter = presenter
                VerticalViewHolder.ControlViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalViewHolder?, position: Int) {
        when (holder) {
            is VerticalViewHolder.StatisticsViewHolder -> {
                holder.binding.viewModel = model
            }
            is VerticalViewHolder.ControlViewHolder -> {
                holder.binding.viewModel = model.controls[position - 1]
            }
        }
    }
}

sealed class VerticalViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    class StatisticsViewHolder(val binding: ItemStatisticsBinding) : VerticalViewHolder(binding.root)
    class ControlViewHolder(val binding: ItemControlBinding) : VerticalViewHolder(binding.root)
}
