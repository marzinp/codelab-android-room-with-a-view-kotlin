package com.marzinp.badminton.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marzinp.badminton.model.TeamHistory

@Dao
interface TeamHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeamHistory(teamHistory: TeamHistory)

    @Query("SELECT playerIds FROM teamhistory_table WHERE :playerId IN (playerIds)")
    suspend fun getAllTeammatesForPlayer(playerId: Int): List<List<Int>>
}