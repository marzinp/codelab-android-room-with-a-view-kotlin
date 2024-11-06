package com.marzinp.badminton.view

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.marzinp.badminton.R
import com.marzinp.badminton.UserSession
import com.marzinp.badminton.databinding.FragmentMatchBinding
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.model.Team
import com.marzinp.badminton.viewmodel.MatchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MatchFragment : Fragment() {
    private val matchViewModel: MatchViewModel by viewModels()
    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("MatchFragment", "onCreateView called")
        setHasOptionsMenu(true)  // Notify that this fragment has an options menu
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve `numCourts` from the Bundle or set default to 3
        val numCourts = arguments?.getInt("numCourts") ?: 3

        setupShuffleButton(numCourts) // Setup shuffle with the numCourts argument

        // Observe `shuffledTeams` and update display every time it changes
        matchViewModel.shuffledTeams.observe(viewLifecycleOwner) { teams ->
            displayCourtsAndOffPlayers(teams, numCourts)
        }

        matchViewModel.shuffleTeams(numCourts)

        matchViewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Log.d("MatchFragment", "Off count incremented successfully")
            } else {
                Log.e("MatchFragment", "Failed to increment off count")
            }
        }
        matchViewModel.updateSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (!isSuccess) {
                Toast.makeText(requireContext(), "Failed to update off count", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupShuffleButton(numCourts: Int) {
        binding.let { safeBinding ->
            // Utilisation sécurisée de safeBinding
            binding.buttonShuffleMatch.setOnClickListener {
                matchViewModel.setTeamsShuffled(false)
                matchViewModel.shuffleTeams(numCourts) // Trigger the shuffle in ViewModel

            }
        }
    }

    private fun displayCourtsAndOffPlayers(teams: List<Team>, numCourts: Int) {
        Log.d("MatchFragment", "Requested to display $numCourts courts with teams: $teams")
        binding.let { safeBinding ->
            binding.courtsContainer.removeAllViews()
        }
        val playersPerCourt = 4
        var maxPlayersForCourts = numCourts * playersPerCourt
        val allPlayerIds = teams.flatMap { it.playerIds }

        // Ajustement si un court reste avec un seul joueur
        if (allPlayerIds.size == maxPlayersForCourts - 1) maxPlayersForCourts -= 2

        val playersForCourtsIds = allPlayerIds.take(maxPlayersForCourts)
        val offPlayerIds = allPlayerIds.drop(maxPlayersForCourts).toMutableList()

        viewLifecycleOwner.lifecycleScope.launch {
            // Boucle pour chaque court avec les joueurs récupérés
            playersForCourtsIds.chunked(playersPerCourt).take(numCourts)
                .forEachIndexed { index, courtPlayerIds ->
                    Log.d(
                        "MatchFragment",
                        "Creating view for court $index with players: $courtPlayerIds"
                    )

                    val courtContainer = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 16, 0, 16) // Espace entre les courts
                        }
                    }

                    val team1PlayerIds: List<Int>
                    val team2PlayerIds: List<Int>

                    when (courtPlayerIds.size) {
                        4 -> {
                            team1PlayerIds = courtPlayerIds.take(2)
                            team2PlayerIds = courtPlayerIds.takeLast(2)
                        }

                        3 -> {
                            team1PlayerIds = courtPlayerIds.take(1)
                            team2PlayerIds = courtPlayerIds.drop(1).take(1)
                            offPlayerIds += courtPlayerIds.last()
                        }

                        2 -> {
                            team1PlayerIds = courtPlayerIds.take(1)
                            team2PlayerIds = courtPlayerIds.drop(1).take(1)
                        }

                        1 -> {
                            offPlayerIds += courtPlayerIds.first()
                            Log.d(
                                "MatchFragment", "Skipping court $index as it has only one player."
                            )
                            return@forEachIndexed // Passer ce court s'il n'y a qu'un joueur
                        }

                        else -> {
                            team1PlayerIds = emptyList()
                            team2PlayerIds = emptyList()
                        }
                    }

                    // Récupérer les joueurs pour les deux équipes de chaque court
                    val team1Players = matchViewModel.getPlayersForTeam(team1PlayerIds)
                    val team2Players = matchViewModel.getPlayersForTeam(team2PlayerIds)

                    Log.d("MatchFragment", "Team 1 for court $index: $team1Players")
                    Log.d("MatchFragment", "Team 2 for court $index: $team2Players")

                    val team1Container = createTeamCard(team1Players, R.color.team1)
                    val team2Container = createTeamCard(team2Players, R.color.team2)

                    courtContainer.addView(team1Container)
                    courtContainer.addView(team2Container)

                    // Ajouter le court au conteneur principal
                    binding.let { safeBinding ->
                        binding.courtsContainer.addView(courtContainer)
                    }
                    Log.d("MatchFragment", "Court $index added to courtsContainer.")
                }

            // Afficher les joueurs "off" après tous les courts
            val offPlayers = matchViewModel.getPlayersForTeam(offPlayerIds)
            if (offPlayers.isNotEmpty()) {
                // Increment off count for these players
                matchViewModel.incrementPlayersOffcount(offPlayerIds)
                displayOffPlayers(offPlayers)
                Log.d("MatchFragment", "Off players displayed after courts.")
            }
        }
    }


    // Helper to create team cards
    private fun createTeamCard(players: List<Player>, colorRes: Int): MaterialCardView {
        return MaterialCardView(requireContext()).apply {
            setContentPadding(16, 16, 16, 16)
            radius = 8f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            cardElevation = 4f
            strokeColor = getColor(requireContext(), colorRes)
            strokeWidth = 4
            addView(createTeamView(players, colorRes, "Skill Sum: ${players.sumOf { it.skill }}"))
        }
    }

    // Helper to display off players
    private fun displayOffPlayers(offPlayers: List<Player>) {
        val offPlayersTitle = TextView(requireContext()).apply {
            text = context.getString(R.string.off_players) + ":"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        binding.let { safeBinding ->
            binding.courtsContainer.addView(offPlayersTitle)
        }

        val offPlayersLayout = FlexboxLayout(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        offPlayers.forEach { player ->
            val playerView = TextView(requireContext()).apply {
                text = player.name
                textSize = 18f
                setPadding(8, 8, 8, 8)
            }
            offPlayersLayout.addView(playerView)
        }
        binding.let { safeBinding ->
            binding.courtsContainer.addView(offPlayersLayout)
        }

        Log.d("MatchFragment", "Added layout for OffPlayers with ${offPlayers.size} players.")
    }

    private fun createTeamView(
        players: List<Player>, colorRes: Int, skillSumText: String
    ): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL

            // Add player names
            players.forEach { player ->
                val playerView = TextView(requireContext()).apply {
                    text = player.name
                    textSize = 18f
                    setTextColor(getColor(requireContext(), colorRes))
                    gravity = Gravity.CENTER
                }
                addView(playerView)
            }

            // Add skill sum at the bottom
            val skillSumView = TextView(requireContext()).apply {
                text = skillSumText
                textSize = 16f
                setTextColor(getColor(requireContext(), colorRes))
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 8, 0, 0)
            }
            if(UserSession.isAdmin.value==true)addView(skillSumView)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp() // This handles back navigation
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        _binding = null
    }
}
