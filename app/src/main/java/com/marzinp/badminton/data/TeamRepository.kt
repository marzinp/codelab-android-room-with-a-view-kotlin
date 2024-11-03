package com.marzinp.badminton.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow


/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class TeamRepository(private val teamDao: TeamDao) {

    // Now a Flow, so it can be observed for real-time updates
    val allTeams: Flow<List<Team>> = teamDao.getAllTeams()

    @WorkerThread
    suspend fun insertTeam(team: Team) {
        teamDao.insertTeam(team)
    }

    @WorkerThread
    suspend fun insertTeams(teams: List<Team>) {
        teamDao.insertTeams(teams)
    }

    @WorkerThread
    suspend fun deleteAllTeams() {
        teamDao.deleteAllTeams()
    }
}

