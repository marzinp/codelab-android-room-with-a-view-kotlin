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

package com.example.android.badminton.UI

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.badminton.R
import com.example.android.badminton.data.Player

class PlayerListAdapter(
    private val onItemLongClick: (Player) -> Unit,
    private val playerViewModel: PlayerViewModel
) : ListAdapter<Player, PlayerListAdapter.PlayerViewHolder>(PlayerDiffCallback) {

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.textViewName)
        private val playerSkill: TextView = itemView.findViewById(R.id.textViewSkill)
        private val playerPresenceCheckbox: CheckBox = itemView.findViewById(R.id.checkboxPresent)

        fun bind(player: Player) {
            playerName.text = player.name
            playerSkill.text = player.skill.toString()
            playerPresenceCheckbox.isChecked = player.isPresent

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
