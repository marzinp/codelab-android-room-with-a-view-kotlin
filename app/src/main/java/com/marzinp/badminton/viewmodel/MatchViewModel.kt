package com.marzinp.badminton.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.model.Team
import com.marzinp.badminton.repository.PlayerRepository
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

    private val previousTeams: MutableSet<List<Int>> = mutableSetOf()
    private val previousOffTeam: MutableList<Int> = mutableListOf()

    // Ensemble pour gérer la rotation des joueurs "off"
    private val offRotationSet: MutableSet<Int> = mutableSetOf()

    suspend fun getPlayersForTeam(playerIds: List<Int>): List<Player> {
        return playerRepository.getPlayersByIds(playerIds)
    }

    fun shuffleTeams(numCourts: Int) {
        viewModelScope.launch {
// Fetch players and sort by `offcount` in descending order
            val presentPlayers = playerRepository.getPresentPlayers()
                .firstOrNull()?.sortedByDescending { it.offcount } ?: emptyList()

            if (presentPlayers.isEmpty()) {
                Log.d("MatchViewModel", "No players are marked as present.")
                return@launch
            }

            val playersPerCourt = 4
            val playersForCourts = presentPlayers.take(numCourts * playersPerCourt) // Highest offcount players prioritized
            val offPlayers = presentPlayers.drop(numCourts * playersPerCourt) // Remaining players


            val maxSkillDifference = 2
            val playersPerTeam = 2
            val maxAttempts = 10
            var bestTeams: List<Team> = emptyList()
            var bestOffTeam: List<Int> = emptyList()
            var minSkillDifference = Int.MAX_VALUE

            // Calculate the exact number of players for courts and off-players
            val maxPlayersForCourts = numCourts * playersPerTeam * 2
            val requiredOffPlayers = presentPlayers.size - maxPlayersForCourts

            suspend fun calculateTeamSkill(playerIds: List<Int>): Int {
                val players = playerRepository.getPlayersByIds(playerIds)
                return players.sumOf { it.skill }
            }

            repeat(maxAttempts) {
                val shuffledPlayers = playersForCourts.shuffled()
                val teams = mutableListOf<Team>()

                // Form court teams with the highest offcount players
                for (i in shuffledPlayers.indices step playersPerTeam) {
                    val teamPlayers = shuffledPlayers.slice(i until minOf(i + playersPerTeam, shuffledPlayers.size))
                    teams.add(Team(teamId = i / playersPerTeam, playerIds = teamPlayers.map { it.id }))
                }

                // Form the off-team
                val offTeam = offPlayers.take(requiredOffPlayers).map { it.id }
                val teamSkills = teams.map { calculateTeamSkill(it.playerIds) }
                val maxSkill = teamSkills.maxOrNull() ?: 0
                val minSkill = teamSkills.minOrNull() ?: 0
                val skillDifference = maxSkill - minSkill

                if (skillDifference <= maxSkillDifference && offTeam.size == requiredOffPlayers) {
                    _shuffledTeams.value = teams + Team(teamId = -1, playerIds = offTeam)
                    saveTeamsToHistory()
                    return@launch
                }

                if (skillDifference < minSkillDifference) {
                    minSkillDifference = skillDifference
                    bestTeams = teams
                    bestOffTeam = offTeam
                }
            }

            // Use the best attempt if no perfect balance was found
            _shuffledTeams.value = bestTeams + Team(teamId = -1, playerIds = bestOffTeam)
            saveTeamsToHistory()
        }
    }


    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess
    fun incrementPlayersOffcount(offPlayerIds: List<Int>) {
        viewModelScope.launch {
            try {
                playerRepository.incrementPlayersOffcount(offPlayerIds)
                Log.d("OffPlayersIncrement","PlayersIds Incremented : ${offPlayerIds}")
                _updateSuccess.value = true // Set success status
            } catch (e: Exception) {
                _updateSuccess.value = false // Set failure status
                Log.e("MatchViewModel", "Error updating off count", e)
            }
        }
    }
    fun saveTeamsToHistory() {
        viewModelScope.launch {
            if (previousTeams.size > 100) {
                previousTeams.remove(previousTeams.first()) // Limite l'historique des équipes
            }
            _shuffledTeams.value?.forEach { team ->
                if (team.teamId != -1) { // Sauvegarder seulement les équipes normales, pas l'équipe Off
                    teamRepository.insertTeam(Team(playerIds = team.playerIds))
                }
                           }
            Log.d("MatchViewModel", "Teams saved to Team table with limit of 500 records.")
        }
    }
}

