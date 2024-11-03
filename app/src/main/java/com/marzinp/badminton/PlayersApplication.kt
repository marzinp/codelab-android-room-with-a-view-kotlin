/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.marzinp.badminton

import android.app.Application
import android.util.Log
import com.marzinp.badminton.data.MatchRepository
import com.marzinp.badminton.data.Player
import com.marzinp.badminton.data.PlayerRepository
import com.marzinp.badminton.data.PlayerRoomDatabase
import com.marzinp.badminton.data.TeamHistoryRepository
import com.marzinp.badminton.data.TeamRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
@HiltAndroidApp
class PlayersApplication : Application() {

    // Define a scope that lives as long as the application
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Initialize the database immediately
    val database: PlayerRoomDatabase by lazy { PlayerRoomDatabase.getDatabase(this, applicationScope) }

    // Declare repositories as properties accessible across the application
    val matchRepository: MatchRepository by lazy { MatchRepository(database.matchDao()) }
    val playerRepository: PlayerRepository by lazy { PlayerRepository(database.playerDao()) }
    val teamRepository: TeamRepository by lazy { TeamRepository(database.teamDao()) }
    val teamHistoryRepository by lazy { TeamHistoryRepository(database.teamHistoryDao()) }

    override fun onCreate() {
        super.onCreate()

        // Trigger a write operation to finalize the database creation
        applicationScope.launch {
            database.playerDao().addPlayer(Player(0,"InitializationCheck", 0,true)) // Insert a temporary player
            database.playerDao().deletePlayerByName("InitializationCheck") // Clear this initial data
            Log.d("RoomDatabase", "Forced database creation with a write operation.")
        }

        Log.d("RoomDatabaseApp", "Application started, database initialized.")
    }
}
