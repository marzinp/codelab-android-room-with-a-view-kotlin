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
            val presentPlayers =
                playerRepository.getPresentPlayers().firstOrNull()?.shuffled() ?: emptyList()
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

            // Calcul du nombre exact de joueurs pour les courts et pour les "off"
            val maxPlayersForCourts = numCourts * playersPerTeam * 2
            val requiredOffPlayers = if (presentPlayers.size > maxPlayersForCourts) {
                presentPlayers.size - maxPlayersForCourts
            } else {
                0
            }

            suspend fun calculateTeamSkill(playerIds: List<Int>): Int {
                val players = playerRepository.getPlayersByIds(playerIds)
                return players.sumOf { it.skill }
            }

            repeat(maxAttempts) {
                val shuffledPlayers = presentPlayers.shuffled()
                val teams = mutableListOf<Team>()

                // Prendre les joueurs pour les courts
                val playersForCourts = shuffledPlayers.take(maxPlayersForCourts)
                val remainingPlayers = shuffledPlayers.drop(maxPlayersForCourts)

                // Former les équipes de courts
                for (i in playersForCourts.indices step playersPerTeam) {
                    val teamPlayers = playersForCourts.slice(
                        i until minOf(
                            i + playersPerTeam,
                            playersForCourts.size
                        )
                    )
                    val playerIds = teamPlayers.map { it.id }
                    teams.add(Team(teamId = i / playersPerTeam, playerIds = playerIds))
                }

                // Sélection des joueurs "off" en respectant la rotation
                var offTeam = remainingPlayers
                    .filter { it.id !in offRotationSet }
                    .map { it.id }
                    .take(requiredOffPlayers)

                // Si le nombre de joueurs off est insuffisant, compléter avec ceux qui ont déjà été off
                if (offTeam.size < requiredOffPlayers) {
                    val additionalPlayers = remainingPlayers
                        .map { it.id }
                        .filter { it !in offTeam }
                        .take(requiredOffPlayers - offTeam.size)
                    offTeam = offTeam + additionalPlayers
                }

                // Mise à jour de la rotation des joueurs Off
                offRotationSet.addAll(offTeam)
                if (offRotationSet.size >= presentPlayers.size) {
                    // Réinitialiser le cycle de rotation quand tous les joueurs sont passés en Off
                    offRotationSet.clear()
                }

                // Calcul de l'équilibre des compétences
                val teamSkills = teams.map { calculateTeamSkill(it.playerIds) }
                val maxSkill = teamSkills.maxOrNull() ?: 0
                val minSkill = teamSkills.minOrNull() ?: 0
                val skillDifference = maxSkill - minSkill

                if (skillDifference <= maxSkillDifference && offTeam.size == requiredOffPlayers) {
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

