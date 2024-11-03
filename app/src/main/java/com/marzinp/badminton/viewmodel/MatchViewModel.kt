package com.marzinp.badminton.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.repository.PlayerRepository
import com.marzinp.badminton.model.Team
import com.marzinp.badminton.model.TeamHistory
import com.marzinp.badminton.repository.TeamHistoryRepository
import com.marzinp.badminton.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val teamRepository: TeamRepository,
) : ViewModel() {

    private val _shuffledTeams = MutableLiveData<List<Team>>()
    val shuffledTeams: LiveData<List<Team>> get() = _shuffledTeams
    private val _offPlayers = MutableLiveData<List<Player>>()

    fun shuffleTeams(numCourts: Int) {
        viewModelScope.launch {
            val presentPlayers = playerRepository.getPresentPlayers().firstOrNull()?.shuffled() ?: emptyList()
            Log.d("MatchViewModel", "Shuffled present players: $presentPlayers")

            if (presentPlayers.isEmpty()) {
                Log.d("MatchViewModel", "No players are marked as present.")
                return@launch
            }

            val maxSkillDifference = 2
            val playersPerTeam = 2
            val maxAttempts = 10
            var bestTeams: List<Team> = emptyList()
            var minSkillDifference = Int.MAX_VALUE

            fun calculateTeamSkill(team: List<Player>): Int {
                return team.sumOf { it.skill }
            }

            repeat(maxAttempts) {
                val shuffledPlayers = presentPlayers.shuffled()
                val teams = mutableListOf<Team>()

                // Répartit les joueurs par équipes de deux
                for (i in shuffledPlayers.indices step playersPerTeam) {
                    val teamPlayers = shuffledPlayers.slice(i until minOf(i + playersPerTeam, shuffledPlayers.size))
                    teams.add(Team(teamId = i / playersPerTeam, teamPlayers = teamPlayers))
                }

                // Calculer les compétences des équipes
                val teamSkills = teams.map { calculateTeamSkill(it.teamPlayers) }
                val maxSkill = teamSkills.maxOrNull() ?: 0
                val minSkill = teamSkills.minOrNull() ?: 0
                val skillDifference = maxSkill - minSkill

                // Vérifiez si cette distribution satisfait la contrainte
                if (skillDifference <= maxSkillDifference) {
                    _shuffledTeams.value = teams
                    Log.d("MatchViewModel", "Found balanced teams with skill difference: $skillDifference")
                    return@launch
                }

                // Mémoriser la meilleure répartition trouvée
                if (skillDifference < minSkillDifference) {
                    minSkillDifference = skillDifference
                    bestTeams = teams
                }
            }

            // Si aucune répartition parfaite n'est trouvée, utilise la meilleure tentative
            _shuffledTeams.value = bestTeams
            Log.d("MatchViewModel", "No perfect balance found. Using best attempt with skill difference: $minSkillDifference")
            saveTeamsToHistory()
        }
    }
    fun saveTeamsToHistory() {
        viewModelScope.launch {
            _shuffledTeams.value?.forEach { team ->
                val playerIds = team.teamPlayers.map { it.id }
                teamRepository.insertTeam(Team(playerIds = playerIds))
            }
            Log.d("MatchViewModel", "Teams saved to Team table")
        }
    }

    /*fun shuffleTeams(numCourts: Int) {
        viewModelScope.launch {
            val presentPlayers =
                playerRepository.getPresentPlayers().firstOrNull()?.shuffled() ?: emptyList()
            Log.d("MatchViewModel", "Shuffled present players: $presentPlayers")

            playerRepository.getPresentPlayers().collect { presentPlayers ->
                val playersList = presentPlayers.toList()
                if (playersList.isEmpty()) {
                    Log.d("MatchViewModel", "No players are marked as present.")
                    return@collect
                }

                val shuffledPlayers = playersList.shuffled()
                val teams = mutableListOf<Team>()
                val playersPerTeam = 2

                for (i in shuffledPlayers.indices step playersPerTeam) {
                    val teamPlayers = shuffledPlayers.slice(
                        i until minOf(i + playersPerTeam, shuffledPlayers.size)
                    )
                    teams.add(Team(teamId = i / playersPerTeam, teamPlayers = teamPlayers))
                }

                _shuffledTeams.value = teams
                Log.d("MatchViewModel", "shuffleTeams updated _shuffledTeams with: $teams")
            }
        }
    }*/
}

