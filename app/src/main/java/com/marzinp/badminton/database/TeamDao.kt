package com.marzinp.badminton.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marzinp.badminton.model.Team
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeam(team: Team): Long

    @Query("SELECT playerIds FROM team_table WHERE :playerId IN (playerIds)")
    suspend fun getTeammatesForPlayer(playerId: Int): List<String>

    @Query("DELETE FROM team_table WHERE teamId NOT IN (SELECT teamId FROM team_table ORDER BY teamId DESC LIMIT :limit)")
    suspend fun deleteOldTeams(limit: Int)
}