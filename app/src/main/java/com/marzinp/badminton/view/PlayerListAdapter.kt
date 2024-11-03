/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marzinp.badminton.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marzinp.badminton.R
import com.marzinp.badminton.viewmodel.PlayerViewModel
import com.marzinp.badminton.model.Player

class PlayerListAdapter(
    private val onItemLongClick: (Player) -> Unit,
    private val playerViewModel: PlayerViewModel
) : ListAdapter<Player, PlayerListAdapter.PlayerViewHolder>(PlayerDiffCallback) {

    private var isAdmin: Boolean = false

    // Function to update admin visibility in the adapter
    fun setAdminVisibility(isAdmin: Boolean) {
        this.isAdmin = isAdmin
        notifyDataSetChanged() // Refresh to apply visibility changes
    }

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.textViewName)
        private val playerSkill: TextView = itemView.findViewById(R.id.textViewSkill)
        private val playerPresenceCheckbox: CheckBox = itemView.findViewById(R.id.checkboxPresent)

        fun bind(player: Player) {
            // Remove any previous listeners to avoid triggering them on rebind
            playerPresenceCheckbox.setOnCheckedChangeListener(null)

            // Set checkbox state based on player's presence
            playerPresenceCheckbox.isChecked = player.isPresent

            // Reattach the listener for changes
            playerPresenceCheckbox.setOnCheckedChangeListener { _, isChecked ->
                playerViewModel.togglePlayerPresence(player.id, isChecked)
            }
            playerName.text = player.name
            playerSkill.text = player.skill.toString()
            playerPresenceCheckbox.isChecked = player.isPresent

            // Conditionally display skill based on admin status
            playerSkill.visibility = if (isAdmin) View.VISIBLE else View.GONE

            playerPresenceCheckbox.setOnCheckedChangeListener { _, isChecked ->
                playerViewModel.togglePlayerPresence(player.id, isChecked)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(player)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val PlayerDiffCallback = object : DiffUtil.ItemCallback<Player>() {
            override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
                return oldItem == newItem
            }
        }
    }
}
