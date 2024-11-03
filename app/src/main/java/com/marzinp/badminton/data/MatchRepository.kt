package com.marzinp.badminton.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class MatchRepository(private val matchDao: MatchDao) {

    // Observing all matches
    val allMatches: Flow<List<Match>> = matchDao.getAllMatches()

    @WorkerThread
    suspend fun insertMatch(match: Match) {
        matchDao.insertMatch(match)
    }

    @WorkerThread
    suspend fun deleteAllMatches() {
        matchDao.deleteAllMatches()
    }
}