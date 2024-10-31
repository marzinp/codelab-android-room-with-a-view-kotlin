package com.example.android.badminton.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TeamHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTeamHistory(teamHistory: TeamHistory)

    @Query("SELECT * FROM teamhistory WHERE (player1Id = :player1Id AND player2Id = :player2Id) OR (player1Id = :player2Id AND player2Id = :player1Id)")
    fun getTeamHistory(player1Id: Int, player2Id: Int): TeamHistory?
}