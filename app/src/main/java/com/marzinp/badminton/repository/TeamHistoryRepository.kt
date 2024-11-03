package com.marzinp.badminton.repository

import com.marzinp.badminton.database.TeamHistoryDao
import com.marzinp.badminton.model.TeamHistory


class TeamHistoryRepository(private val teamHistoryDao: TeamHistoryDao) {

    // Insérer une entrée dans TeamHistory
    suspend fun insertTeamHistory(teamHistory: TeamHistory) {
        teamHistoryDao.insertTeamHistory(teamHistory)
    }

    // Récupérer tous les coéquipiers pour un joueur donné
    suspend fun getTeammatesForPlayer(playerId: Int): List<List<Int>> {
        return teamHistoryDao.getAllTeammatesForPlayer(playerId)
    }

    // Optionnel : Obtenir la liste unique de coéquipiers pour un joueur
    suspend fun getUniqueTeammatesForPlayer(playerId: Int): Set<Int> {
        val allTeammates = teamHistoryDao.getAllTeammatesForPlayer(playerId)
        return allTeammates.flatten().toSet().minus(playerId) // Exclure le joueur lui-même
    }
}
