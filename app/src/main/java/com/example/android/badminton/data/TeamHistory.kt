package com.example.android.badminton.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TeamHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val player1Id: Int,
    val player2Id: Int
)