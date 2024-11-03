package com.marzinp.badminton.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeam(team: Team)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeams(teams: List<Team>) // Method to insert a list of teams

    @Query("SELECT * FROM team_table")
    fun getAllTeams(): Flow<List<Team>>

    @Query("DELETE FROM team_table")
    suspend fun deleteAllTeams()
}