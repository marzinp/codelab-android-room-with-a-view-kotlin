package com.marzinp.badminton.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.repository.PlayerRepository
import com.marzinp.badminton.model.Team
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

    // Ensemble pour stocker les combinaisons d'équipes précédentes
    private val _shuffledTeams = MutableLiveData<List<Team>>()
    val shuffledTeams: LiveData<List<Team>> get() = _shuffledTeams

    private val previousTeams: MutableSet<List<Int>> = mutableSetOf()
    private val previousOffTeam: MutableList<Int> = mutableListOf()

    suspend fun getPlayersForTeam(playerIds: List<Int>): List<Player> {
        return playerRepository.getPlayersByIds(playerIds)
    }
    fun shuffleTeams(numCourts: Int) {
        viewModelScope.launch {
            val presentPlayers = playerRepository.getPresentPlayers().firstOrNull()?.shuffled() ?: emptyList()
            if (presentPlayers.isEmpty()) {
                Log.d("MatchViewModel", "No players are marked as present.")
                return@launch
            }

            val maxSkillDifference = 2
            val playersPerTeam = 2
            val maxAttempts = 10
            var bestTeams: List<Team> = emptyList()
            var bestOffTeam: List<Int> = emptyList()
            var minSkillDifference = Int.MAX_VALUE

            // Calculer le nombre exact de joueurs nécessaires pour les courts et les "off"
            val maxPlayersForCourts = numCourts * playersPerTeam * 2
            val requiredOffPlayers = presentPlayers.size - maxPlayersForCourts

            suspend fun calculateTeamSkill(playerIds: List<Int>): Int {
                val players = playerRepository.getPlayersByIds(playerIds)
                return players.sumOf { it.skill }
            }

            repeat(maxAttempts) {
                val shuffledPlayers = presentPlayers.shuffled()
                val teams = mutableListOf<Team>()

                // Assigner les joueurs pour les courts
                val playersForCourts = shuffledPlayers.take(maxPlayersForCourts)
                val remainingPlayers = shuffledPlayers.drop(maxPlayersForCourts)

                // Former les équipes sur les courts
                for (i in playersForCourts.indices step playersPerTeam) {
                    val teamPlayers = playersForCourts.slice(i until minOf(i + playersPerTeam, playersForCourts.size))
                    val playerIds = teamPlayers.map { it.id }
                    teams.add(Team(teamId = i / playersPerTeam, playerIds = playerIds))
                }

                // Former l'équipe Off avec le nombre exact requis de joueurs
                var offTeam = remainingPlayers.map { it.id }
                if (offTeam.size != requiredOffPlayers || offTeam.any { it in previousOffTeam }) {
                    // Filtrer les joueurs "off" précédents et ajuster la taille pour correspondre exactement au nombre requis
                    offTeam = remainingPlayers
                        .filterNot { it.id in previousOffTeam }
                        .map { it.id }
                        .take(requiredOffPlayers)

                    // Si le filtrage n'a pas abouti au nombre exact de joueurs, ajuster
                    if (offTeam.size < requiredOffPlayers) {
                        // Ajouter des joueurs restants pour atteindre le nombre requis
                        offTeam += remainingPlayers
                            .map { it.id }
                            .filterNot { it in offTeam }
                            .take(requiredOffPlayers - offTeam.size)
                    } else if (offTeam.size > requiredOffPlayers) {
                        // Réduire la taille de `offTeam` pour correspondre au nombre requis
                        offTeam = offTeam.take(requiredOffPlayers)
                    }
                }

                // Calculer l'équilibre des compétences des équipes
                val teamSkills = teams.map { calculateTeamSkill(it.playerIds) }
                val maxSkill = teamSkills.maxOrNull() ?: 0
                val minSkill = teamSkills.minOrNull() ?: 0
                val skillDifference = maxSkill - minSkill

                if (skillDifference <= maxSkillDifference && offTeam.size == requiredOffPlayers) {
                    // Ajouter l'équipe Off comme une équipe spéciale
                    _shuffledTeams.value = teams + Team(teamId = -1, playerIds = offTeam)
                    previousTeams.addAll(teams.map { it.playerIds.sorted() })
                    previousOffTeam.clear()
                    previousOffTeam.addAll(offTeam)
                    Log.d("MatchViewModel", "Balanced teams found with unique Off team.")
                    saveTeamsToHistory()
                    return@launch
                }

                // Enregistrer la meilleure tentative si aucune combinaison parfaite n'est trouvée
                if (skillDifference < minSkillDifference) {
                    minSkillDifference = skillDifference
                    bestTeams = teams
                    bestOffTeam = offTeam
                }
            }

            // Utiliser la meilleure tentative si aucune combinaison unique n'a été trouvée
            _shuffledTeams.value = bestTeams + Team(teamId = -1, playerIds = bestOffTeam)
            previousOffTeam.clear()
            previousOffTeam.addAll(bestOffTeam)
            Log.d("MatchViewModel", "Best attempt used with skill difference: $minSkillDifference.")
            saveTeamsToHistory()
        }
    }


    fun saveTeamsToHistory() {
        viewModelScope.launch {
            // Limiter `previousTeams` à un maximum de 100 combinaisons
            if (previousTeams.size > 100) {
                previousTeams.remove(previousTeams.first()) // Retire l'élément le plus ancien
            }

            // Enregistrer les équipes dans l'historique
            _shuffledTeams.value?.forEach { team ->
                teamRepository.insertTeam(Team(playerIds = team.playerIds))
            }

            Log.d("MatchViewModel", "Teams saved to Team table with limit of 500 records.")
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

