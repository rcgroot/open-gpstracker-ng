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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.v2.wear.databinding.WearBindingComponent
import nl.sogeti.android.gpstracker.v2.wear.databinding.ItemControlsBinding
import nl.sogeti.android.gpstracker.v2.wear.databinding.ItemStatisticsBinding

class VerticalControlAdapter(private val model: ControlViewModel, private val presenter: ControlPresenter) : RecyclerView.Adapter<VerticalControlAdapter.VerticalViewHolder>() {

    override fun getItemViewType(position: Int) = if (position == 0) 0 else 1

    override fun getItemCount() = 2

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VerticalViewHolder? {
        val inflater = LayoutInflater.from(parent?.context)
        return when (viewType) {
            0 -> {
                val binding = DataBindingUtil.inflate<ItemStatisticsBinding>(inflater, R.layout.item_statistics, parent, false, WearBindingComponent())
                binding.presenter = presenter
                VerticalViewHolder.StatisticsViewHolder(binding)
            }
            1 -> {
                val binding = DataBindingUtil.inflate<ItemControlsBinding>(inflater, R.layout.item_controls, parent, false, WearBindingComponent())
                binding.presenter = presenter
                binding.viewModel = model
                VerticalViewHolder.ControlsViewHolder(binding)
            }
            else -> throw IllegalStateException("Unknown viewType $viewType cannot be created")
        }
    }

    override fun onBindViewHolder(holder: VerticalViewHolder, position: Int) =
            when (holder) {
                is VerticalViewHolder.StatisticsViewHolder -> {
                    holder.binding.viewModel = model
                }
                is VerticalViewHolder.ControlsViewHolder -> Unit
            }

    sealed class VerticalViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        class StatisticsViewHolder(val binding: ItemStatisticsBinding) : VerticalViewHolder(binding.root)
        class ControlsViewHolder(val binding: ItemControlsBinding) : VerticalViewHolder(binding.root)
    }
}
