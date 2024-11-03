package com.marzinp.badminton.data


class TeamHistoryRepository(private val teamHistoryDao: TeamHistoryDao) {

    // Insert a team history entry
    suspend fun insertTeamHistory(teamHistory: TeamHistory) {
        teamHistoryDao.insertTeamHistory(teamHistory)
    }

    // Retrieve a team history entry by teamId
    suspend fun getTeamHistory(teamId: Int): TeamHistory? {
        return teamHistoryDao.getTeamHistory(teamId)
    }

    // Add any additional data operations you may need, such as deleting or updating
}
