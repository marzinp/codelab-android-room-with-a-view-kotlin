package com.example.android.badminton.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    // The flow always holds/caches latest version of data. Notifies its observers when the
    // data has changed.

    @Query("SELECT * FROM player_table")
    fun getAllPlayers():Flow<List<Player>>

    @Query("SELECT * FROM player_table ORDER BY name ASC")
    fun getPlayersSortedByNameAsc(): Flow<List<Player>>

    @Query("SELECT * FROM player_table ORDER BY name DESC")
    fun getPlayersSortedByNameDesc(): Flow<List<Player>>

    @Query("SELECT * FROM player_table ORDER BY skill ASC")
    fun getPlayersSortedBySkillAsc(): Flow<List<Player>>

    @Query("SELECT * FROM player_table ORDER BY skill DESC")
    fun getPlayersSortedBySkillDesc(): Flow<List<Player>>

    @Query("SELECT * FROM player_table WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Player?

    @Query("SELECT * FROM player_table WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Player?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayer(player: Player)

    @Query("DELETE FROM player_table")
    suspend fun deleteAllPlayers()

    @Query("DELETE FROM player_table WHERE name = :name")
    suspend fun deletePlayerByName(name:String)

    // Get all players marked as present
    @Query("SELECT * FROM player_table WHERE isPresent = 1")
    fun getPresentPlayers(): Flow<List<Player>>

    // Update player's presence status
    @Query("UPDATE player_table SET isPresent = :isPresent WHERE id = :playerId")
    suspend fun updatePlayerPresence(playerId: Long, isPresent: Boolean)
    // Other existing methods
}