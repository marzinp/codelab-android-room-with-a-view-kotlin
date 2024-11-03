// TeamHistoryViewModel.kt
package com.marzinp.badminton.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marzinp.badminton.data.TeamHistory
import com.marzinp.badminton.data.TeamHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamHistoryViewModel @Inject constructor(private val teamHistoryRepository: TeamHistoryRepository) : ViewModel() {

    fun insertTeamHistory(teamHistory: TeamHistory) {
        viewModelScope.launch {
            teamHistoryRepository.insertTeamHistory(teamHistory)
        }
    }

    suspend fun getTeamHistory(teamId: Int): TeamHistory? {
        return teamHistoryRepository.getTeamHistory(teamId)
    }
}
