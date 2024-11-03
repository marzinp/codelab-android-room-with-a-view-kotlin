package com.marzinp.badminton.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teamhistory_table")
data class TeamHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerIds: List<Int>  // Liste des IDs des joueurs de l'équipe
)