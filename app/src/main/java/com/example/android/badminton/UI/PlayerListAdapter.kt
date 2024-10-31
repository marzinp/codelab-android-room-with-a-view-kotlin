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

class PlayerListAdapter(private val viewModel: PlayerViewModel) :
    ListAdapter<Player, PlayerListAdapter.PlayerViewHolder>(PLAYERS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val currentPlayer = getItem(position)
        holder.bind(currentPlayer)
    }

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameView: TextView = itemView.findViewById(R.id.textViewName)
        private val playerSkillView: TextView = itemView.findViewById(R.id.textViewSkill)
        private val checkboxPresent: CheckBox = itemView.findViewById(R.id.checkboxPresent)

        fun bind(player: Player) {
            playerNameView.text = player.name
            playerSkillView.text = "Skill: ${player.skill}"
            checkboxPresent.isChecked = player.isPresent

            // Update presence status on checkbox change
            checkboxPresent.setOnCheckedChangeListener { _, isChecked ->
                viewModel.togglePlayerPresence(player.id, isChecked)
            }
        }
    }

    companion object {
        private val PLAYERS_COMPARATOR = object : DiffUtil.ItemCallback<Player>() {
            override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
                return oldItem == newItem
            }
        }
    }
}

