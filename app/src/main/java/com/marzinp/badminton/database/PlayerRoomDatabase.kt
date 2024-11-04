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

package com.marzinp.badminton.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.marzinp.badminton.PlayersApplication
import com.marzinp.badminton.model.Match
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.model.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */
@TypeConverters(
    Converters::class,
   // builtInTypeConverters = BuiltInTypeConverters(
    //    enums = BuiltInTypeConverters.State.DISABLED
   // )
)
@Database(entities = [Player::class, Team::class, Match::class], version = 6, exportSchema = false)
abstract class PlayerRoomDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: PlayerRoomDatabase? = null

        fun getDatabase(context: Context,scope: CoroutineScope ): PlayerRoomDatabase {// Accept CoroutineScope
            val application: PlayersApplication = context.applicationContext as PlayersApplication
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayerRoomDatabase::class.java,
                    "player_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // For debugging only
                    .addCallback(PlayerDatabaseCallback(application.applicationScope))
                    .build()

                Log.d("RoomDatabase", "Database instance created: $instance")
                INSTANCE = instance
                instance
            }
        }

        private class PlayerDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("RoomDatabase", "Database onCreate triggered")
                scope.launch {
                    Log.d("RoomDatabase", "Starting to populate database")
                    INSTANCE?.let { database ->
                        populateDatabase(database.playerDao(), database.teamDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(playerDao: PlayerDao, teamDao: TeamDao) {
            Log.d("RoomDatabase", "Populating database with initial data")
            try {
                playerDao.deleteAllPlayers()
                Log.d("RoomDatabase", "Deleted all players")

                val players = listOf(
                    Player(0,"Prince", 3), Player(0,"Pierre", 2),
                    Player(0,"Véro", 3), Player(0,"Charline", 2),
                    Player(0,"Milan", 2), Player(0,"Neha", 1),
                    Player(0,"Corentin", 3), Player(0,"Céline", 2),
                    Player(0,"Izia", 1), Player(0,"Elio", 3),
                    Player(0,"Alex", 3), Player(0,"Sylvie", 2),
                    Player(0,"Thierry", 3), Player(0,"Jean-Paul", 2),
                    Player(0,"Keith", 2), Player(0,"Harry", 1),
                    Player(0,"Peter", 3), Player(0,"Molly", 2),
                    Player(0,"Chloe", 1), Player(0,"Olivia", 3)
                )

                for (player in players) {
                    Log.d("RoomDatabase", "Inserting player: ${player.name}")
                    playerDao.addPlayer(player)
                }

                teamDao.deleteAllTeams()
                Log.d("RoomDatabase", "Deleted all teams")

            } catch (e: Exception) {
                Log.e("RoomDatabase", "Error populating database", e)
            }
        }
    }
}