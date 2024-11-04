package com.marzinp.badminton.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.model.Team
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM team_table")
    fun getAllTeams():Flow<List<Team>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeam(team: Team): Long

    // Insère une liste d'équipes avec une stratégie d'ignorance des conflits
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeams(teams: List<Team>)

    @Query("SELECT * FROM team_table WHERE :playerId IN (playerIds)")
    suspend fun getTeamsForPlayer(playerId: Int): List<Team>

    @Query("DELETE FROM team_table WHERE teamId NOT IN (SELECT teamId FROM team_table ORDER BY teamId DESC LIMIT :limit)")
    suspend fun deleteOldTeams(limit: Int)

    @Query("DELETE FROM team_table")
    suspend fun deleteAllTeams()

}