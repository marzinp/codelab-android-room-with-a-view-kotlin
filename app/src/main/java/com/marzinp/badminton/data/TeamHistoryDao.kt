package com.marzinp.badminton.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marzinp.badminton.data.TeamHistory

@Dao
interface TeamHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeamHistory(teamHistory: TeamHistory)

    @Query("SELECT * FROM teamhistory_table WHERE teamId = :teamId")
    suspend fun getTeamHistory(teamId: Int): TeamHistory?
}