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
package com.marzinp.badminton.data

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class PlayerRepository(private val playerDao: PlayerDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers()
    fun getPlayersSortedByNameAsc() = playerDao.getPlayersSortedByNameAsc()
    fun getPlayersSortedByNameDesc() = playerDao.getPlayersSortedByNameDesc()
    fun getPlayersSortedBySkillAsc() = playerDao.getPlayersSortedBySkillAsc()
    fun getPlayersSortedBySkillDesc() = playerDao.getPlayersSortedBySkillDesc()

    // Get players marked as present
    fun getPresentPlayers(): Flow<List<Player>> {
        val presentPlayers = playerDao.getPresentPlayers()/* Your logic to get present players */
        Log.d("PlayerRepository", "Present players fetched: $presentPlayers")
        return presentPlayers
    }

    // Update presence status
    suspend fun updatePlayerPresence(playerId: Long, isPresent: Boolean) {
        playerDao.updatePlayerPresence(playerId, isPresent)
    }
    suspend fun setAllPlayersPresence(isPresent: Boolean) {
        playerDao.updateAllPlayersPresence(isPresent)
    }
    suspend fun updatePlayer(player: Player) {
        playerDao.update(player) // Requires an @Update function in PlayerDao
    }

    suspend fun deletePlayer(player: Player) {
        playerDao.delete(player) // Requires a @Delete function in PlayerDao
    }
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addPlayer(player: Player) {
        playerDao.addPlayer(player)
    }

    suspend fun getPlayerById(player: Player): Long {
        return player.id
    }
}
