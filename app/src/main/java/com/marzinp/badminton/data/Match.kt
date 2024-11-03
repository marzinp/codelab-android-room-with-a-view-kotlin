package com.marzinp.badminton.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "match_table")
@TypeConverters(Converters::class) // Use converters if storing list of teams
data class Match(
    @PrimaryKey(autoGenerate = true) val matchId: Int = 0,
    val teams: List<Team>, // List of teams in the match
    val timestamp: Long = System.currentTimeMillis() // Optional: timestamp for each match
)