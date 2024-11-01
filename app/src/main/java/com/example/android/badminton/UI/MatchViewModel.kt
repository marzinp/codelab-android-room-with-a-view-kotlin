package com.example.android.badminton.UI

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.badminton.data.MatchRepository
import com.example.android.badminton.data.Player
import com.example.android.badminton.data.PlayerRepository
import com.example.android.badminton.data.Team
import com.example.android.badminton.data.TeamRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/*class MatchViewModel(
    private val matchRepository: MatchRepository,
    private val playerRepository: PlayerRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _shuffledTeams = MutableLiveData<List<Team>>()
    val shuffledTeams: LiveData<List<Team>> = _shuffledTeams

    fun shuffleTeams(numCourts: Int) {
        // Generate dummy data for debugging
        val samplePlayers = listOf(
            Player("Alice", skill = 1, isPresent = true, id = 1),
            Player("Bob", skill = 2, isPresent = true, id = 2),
            Player("Charlie", skill = 1, isPresent = true, id = 3),
            Player("Daisy", skill = 3, isPresent = true, id = 4),
            Player("Eve", skill = 2, isPresent = true, id = 5),
            Player("Frank", skill = 3, isPresent = true, id = 6)
        )

        // Shuffle and distribute into teams
        val teams = samplePlayers.chunked(2).mapIndexed { index, players ->
            Team(teamId = index, teamPlayers = players)
        }

        // Update the LiveData with shuffled teams
        _shuffledTeams.value = teams
        Log.d("MatchViewModel", "shuffleTeams updated _shuffledTeams with: $teams")
    }
}*/
class MatchViewModel(
    private val matchRepository: MatchRepository,
    private val playerRepository: PlayerRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _shuffledTeams = MutableLiveData<List<Team>>()
    val shuffledTeams: LiveData<List<Team>> get() = _shuffledTeams
    private val _offPlayers = MutableLiveData<List<Player>>()

    fun shuffleTeams(numCourts: Int) {
          viewModelScope.launch {
              val presentPlayers = playerRepository.getPresentPlayers().firstOrNull()?.shuffled() ?: emptyList()
              Log.d("MatchViewModel", "Shuffled present players: $presentPlayers")
              playerRepository.getPresentPlayers().collect { presentPlayers ->
                  val playersList = presentPlayers.toList() // Convert to a list if needed
                  if (playersList.isEmpty()) {
                      Log.d("MatchViewModel", "No players are marked as present.")
                      return@collect
                  }

                  val shuffledPlayers = playersList.shuffled() // Randomize the list
                  val teams = mutableListOf<Team>()
                  val playersPerTeam = 2 // Adjust based on your team size

                  for (i in shuffledPlayers.indices step playersPerTeam) {
                      val teamPlayers = shuffledPlayers.slice(i until minOf(i + playersPerTeam, shuffledPlayers.size))
                      teams.add(Team(teamId = i / playersPerTeam, teamPlayers = teamPlayers))
                  }
                  _shuffledTeams.value = teams
                  Log.d("MatchViewModel", "shuffleTeams updated _shuffledTeams with: $teams")
              }
          }
      }
}


class MatchViewModelFactory(
    private val matchRepository: MatchRepository,
    private val playerRepository: PlayerRepository,
    private val teamRepository: TeamRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MatchViewModel(matchRepository, playerRepository, teamRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
