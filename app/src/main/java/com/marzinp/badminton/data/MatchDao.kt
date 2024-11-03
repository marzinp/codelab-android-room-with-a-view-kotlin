package com.marzinp.badminton.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMatch(match: Match)

    @Query("SELECT * FROM match_table")
    fun getAllMatches(): Flow<List<Match>>

    @Query("DELETE FROM match_table")
    suspend fun deleteAllMatches() // `suspend` modifier added here
}

