package com.example.android.badminton.UI

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.badminton.PlayersApplication
import com.example.android.badminton.R
import com.example.android.badminton.data.Player
import com.example.android.badminton.data.Team
import com.example.android.badminton.databinding.FragmentMatchBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap

class MatchFragment : Fragment() {

    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((requireActivity().application as PlayersApplication).playerRepository)
    }
    private val matchViewModel: MatchViewModel by viewModels {
        MatchViewModelFactory(
            (requireActivity().application as PlayersApplication).matchRepository,
            (requireActivity().application as PlayersApplication).playerRepository,
            (requireActivity().application as PlayersApplication).teamRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

        // Perform initial shuffle to populate `shuffledTeams` with data
        matchViewModel.shuffleTeams(numCourts)
    }
    private fun setupShuffleButton(numCourts: Int) {
        binding.buttonShuffleMatch.setOnClickListener {
            matchViewModel.shuffleTeams(numCourts) // Trigger the shuffle in ViewModel
        }
    }

    private fun extractOffPlayers(teams: List<Team>, numCourts: Int): List<Player> {
        val playersPerCourt = 4
        val totalPlayers = teams.flatMap { it.teamPlayers }
        val offPlayers = totalPlayers.drop(numCourts * playersPerCourt)
        Log.d("MatchFragment", "Calculated off players: $offPlayers")
        return offPlayers
    }
    /*private fun displayCourtsAndOffPlayers(teams: List<Team>, numCourts: Int) {
        Log.d("MatchFragment", "Requested to display $numCourts courts with teams: $teams")
        binding.courtsContainer.removeAllViews()

        val playersPerCourt = 4
        var maxPlayersForCourts = numCourts * playersPerCourt
        val allPlayers = teams.flatMap { it.teamPlayers }
        if (allPlayers.size == maxPlayersForCourts - 1) maxPlayersForCourts -= 2
        val playersForCourts = allPlayers.take(maxPlayersForCourts)
        val courts = playersForCourts.chunked(playersPerCourt).take(numCourts)

        courts.forEachIndexed { index, courtPlayers ->
            val courtContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16) // Add space between courts
                }
            }

            val team1Players = courtPlayers.take(playersPerCourt / 2)
            val team2Players = courtPlayers.drop(playersPerCourt / 2)

            // Team 1 card with skill sum
            val team1Container = MaterialCardView(requireContext()).apply {
                setContentPadding(16, 16, 16, 16)
                radius = 8f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                cardElevation = 4f
                strokeColor = getColor(requireContext(), R.color.team1)
                strokeWidth = 4
                addView(createTeamView(team1Players, R.color.team1, "Skill Sum: ${team1Players.sumOf { it.skill }}"))
            }

            // Team 2 card with skill sum
            val team2Container = MaterialCardView(requireContext()).apply {
                setContentPadding(16, 16, 16, 16)
                radius = 8f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                cardElevation = 4f
                strokeColor = getColor(requireContext(), R.color.team2)
                strokeWidth = 4
                addView(createTeamView(team2Players, R.color.team2, "Skill Sum: ${team2Players.sumOf { it.skill }}"))
            }

            courtContainer.addView(team1Container)
            courtContainer.addView(team2Container)

            binding.courtsContainer.addView(courtContainer)
        }

        // Handle Off Players
        val offPlayers = allPlayers.drop(maxPlayersForCourts)
        if (offPlayers.isNotEmpty()) {
            val offPlayersTitle = TextView(requireContext()).apply {
                text = "Off Players:"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            binding.courtsContainer.addView(offPlayersTitle)

            // Use FlowLayout to wrap "Off Players" names
            val offPlayersLayout = FlexboxLayout(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
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

            binding.courtsContainer.addView(offPlayersLayout)
            Log.d("MatchFragment", "Added layout for OffPlayers with ${offPlayers.size} players.")
        }
    }*/
    private fun displayCourtsAndOffPlayers(teams: List<Team>, numCourts: Int) {
        Log.d("MatchFragment", "Requested to display $numCourts courts with teams: $teams")
        binding.courtsContainer.removeAllViews()

        val playersPerCourt = 4
        var maxPlayersForCourts = numCourts * playersPerCourt
        val allPlayers = teams.flatMap { it.teamPlayers }

        // Adjust max players for courts if there's a case where one court would be left with only 1 player
        if (allPlayers.size == maxPlayersForCourts - 1) maxPlayersForCourts -= 2

        val playersForCourts = allPlayers.take(maxPlayersForCourts)
        var offPlayers = allPlayers.drop(maxPlayersForCourts)

        // Split players into courts
        val courts = playersForCourts.chunked(playersPerCourt).take(numCourts)

        courts.forEachIndexed { index, courtPlayers ->
            val courtContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16) // Add space between courts
                }
            }

            // Divide court players into two teams, handling cases with fewer players
            val team1Players: List<Player>
            val team2Players: List<Player>

            when (courtPlayers.size) {
                4 -> {
                    team1Players = courtPlayers.take(2)
                    team2Players = courtPlayers.takeLast(2)
                }
                3 -> {
                    team1Players = courtPlayers.take(1)
                    team2Players = courtPlayers.drop(1).take(1)
                    offPlayers += courtPlayers.last() // Last player is left as off-player
                }
                2 -> {
                    team1Players = courtPlayers.take(1)
                    team2Players = courtPlayers.drop(1).take(1)
                }
                1 -> {
                    offPlayers += courtPlayers.first()
                    return@forEachIndexed // Skip adding this court
                }
                else -> {
                    team1Players = emptyList()
                    team2Players = emptyList()
                }
            }

            // Add each team to the court
            val team1Container = createTeamCard(team1Players, R.color.team1)
            val team2Container = createTeamCard(team2Players, R.color.team2)

            courtContainer.addView(team1Container)
            courtContainer.addView(team2Container)
            binding.courtsContainer.addView(courtContainer)
        }

        // Display off players
        if (offPlayers.isNotEmpty()) displayOffPlayers(offPlayers)
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
            text = "Off Players:"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        binding.courtsContainer.addView(offPlayersTitle)

        val offPlayersLayout = FlexboxLayout(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
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
        binding.courtsContainer.addView(offPlayersLayout)
        Log.d("MatchFragment", "Added layout for OffPlayers with ${offPlayers.size} players.")
    }

    private fun createTeamView(players: List<Player>, colorRes: Int, skillSumText: String): LinearLayout {
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
            addView(skillSumView)
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
    }
}
