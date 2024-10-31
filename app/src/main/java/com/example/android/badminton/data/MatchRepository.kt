package com.example.android.badminton.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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