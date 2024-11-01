package com.example.android.badminton.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teamhistory_table")
data class TeamHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Int
)