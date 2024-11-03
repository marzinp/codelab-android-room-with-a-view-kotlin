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

package com.marzinp.badminton.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View Model to keep a reference to the player repository and
 * an up-to-date list of all players.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(private val repository: PlayerRepository) : ViewModel() {
    val allPlayers: Flow<List<Player>> =repository.allPlayers
    // Holds the current sort order
    private val _currentSortOrder = MutableLiveData(SortOrder.BY_SKILL_ASC)
    val players: LiveData<List<Player>> = _currentSortOrder.switchMap { sortOrder ->
        when (sortOrder) {
            SortOrder.BY_NAME_ASC -> repository.getPlayersSortedByNameAsc().asLiveData()
            SortOrder.BY_NAME_DESC -> repository.getPlayersSortedByNameDesc().asLiveData()
            SortOrder.BY_SKILL_ASC -> repository.getPlayersSortedBySkillAsc().asLiveData()
            SortOrder.BY_SKILL_DESC -> repository.getPlayersSortedBySkillDesc().asLiveData()
        }
    }
    fun togglePlayerPresence(playerId: Long, isPresent: Boolean) {
        viewModelScope.launch {
            repository.updatePlayerPresence(playerId, isPresent)
        }
    }
    private val _updatingAllPlayers = MutableLiveData(false)

    // Inside PlayerViewModel
    // Inside PlayerViewModel
    fun setAllPlayersPresent(isChecked: Boolean) {
        viewModelScope.launch {
            // Collect the first list of players emitted by the Flow
            val players = allPlayers.first() // This collects the latest list of players once

            players.forEach { player ->
                if (player.isPresent != isChecked) {  // Update only if needed
                    repository.updatePlayerPresence(player.id, isChecked)
                }
            }
        }
    }
    // Function to toggle sort order based on current selection
    fun toggleSortOrder(column: SortColumn) {
        _currentSortOrder.value = when (column) {
            SortColumn.NAME -> if (_currentSortOrder.value == SortOrder.BY_NAME_ASC)
                SortOrder.BY_NAME_DESC else SortOrder.BY_NAME_ASC
            SortColumn.SKILL -> if (_currentSortOrder.value == SortOrder.BY_SKILL_ASC)
                SortOrder.BY_SKILL_DESC else SortOrder.BY_SKILL_ASC
        }
    }
    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            repository.updatePlayer(player) // Ensure this method exists in PlayerRepository
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player) // Ensure this method exists in PlayerRepository
        }
    }
    fun addPlayer(player: Player) {
        viewModelScope.launch {
            repository.addPlayer(player) // Ensure this method exists in PlayerRepository
        }
    }

}

// Enum to define sort orders
enum class SortOrder {
    BY_NAME_ASC,
    BY_NAME_DESC,
    BY_SKILL_ASC,
    BY_SKILL_DESC
}

// Enum to identify column
enum class SortColumn {
    NAME,
    SKILL
}