// TeamHistoryViewModel.kt
package com.example.android.badminton.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.badminton.data.TeamHistory
import com.example.android.badminton.data.TeamHistoryRepository
import kotlinx.coroutines.launch

class TeamHistoryViewModel(private val teamHistoryRepository: TeamHistoryRepository) : ViewModel() {

    fun insertTeamHistory(teamHistory: TeamHistory) {
        viewModelScope.launch {
            teamHistoryRepository.insertTeamHistory(teamHistory)
        }
    }

    suspend fun getTeamHistory(teamId: Int): TeamHistory? {
        return teamHistoryRepository.getTeamHistory(teamId)
    }
}

class TeamHistoryViewModelFactory(
    private val repository: TeamHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
