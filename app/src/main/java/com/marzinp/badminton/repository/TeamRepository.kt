package com.marzinp.badminton.repository

import androidx.annotation.WorkerThread
import com.marzinp.badminton.database.TeamDao
import com.marzinp.badminton.model.Team
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class TeamRepository @Inject constructor(private val teamDao: TeamDao) {

    // Utiliser Flow pour observer les équipes en temps réel
    val allTeams: Flow<List<Team>> = teamDao.getAllTeams()

    @WorkerThread
    suspend fun insertTeam(team: Team) {
        teamDao.insertTeam(team)
    }

    @WorkerThread
    suspend fun insertTeams(teams: List<Team>) {
        teamDao.insertTeams(teams)
    }

    // Nouvelle méthode pour obtenir les équipes d'un joueur spécifique
    suspend fun getTeamsForPlayer(playerId: Int): List<Team> {
        return teamDao.getTeamsForPlayer(playerId)
    }

    @WorkerThread
    suspend fun deleteAllTeams() {
        teamDao.deleteAllTeams()
    }
}

