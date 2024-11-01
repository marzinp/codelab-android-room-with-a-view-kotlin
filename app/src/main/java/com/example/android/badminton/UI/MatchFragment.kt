package com.example.android.badminton.UI

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.badminton.PlayersApplication
import com.example.android.badminton.R
import com.example.android.badminton.data.Player
import com.example.android.badminton.data.Team
import com.example.android.badminton.databinding.FragmentMatchBinding

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
        // Enable the up button in the action bar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Log.d("MatchFragment", "onViewCreated called and setting observer for shuffledTeams")

        matchViewModel.shuffledTeams.observe(viewLifecycleOwner) { teams ->
            Log.d("MatchFragment", "Observer triggered with teams: $teams")

            val numCourts = arguments?.getInt("numCourts") ?: 1
            displayCourtsAndOffPlayers(teams, numCourts)

            val offPlayers = extractOffPlayers(teams, numCourts)
            Log.d("MatchFragment", "Off players calculated: $offPlayers")
        }

        // Request shuffling to trigger the observer
        matchViewModel.shuffleTeams(numCourts = 4)
    }

    private fun extractOffPlayers(teams: List<Team>, numCourts: Int): List<Player> {
        val playersPerCourt = 4
        val totalPlayers = teams.flatMap { it.teamPlayers }
        val offPlayers = totalPlayers.drop(numCourts * playersPerCourt)
        Log.d("MatchFragment", "Calculated off players: $offPlayers")
        return offPlayers
    }

    private fun displayCourtsAndOffPlayers(teams: List<Team>, numCourts: Int) {
        Log.d("MatchFragment", "Requested to display $numCourts courts with teams: $teams")
        binding.courtsContainer.removeAllViews()

        val playersPerCourt = 4
        var maxPlayersForCourts = numCourts * playersPerCourt
        val allPlayers = teams.flatMap { it.teamPlayers }
        if (allPlayers.size == maxPlayersForCourts - 1) maxPlayersForCourts -= 2
        // Limit players to max allowed for courts and split into courts with playersPerCourt each
        val playersForCourts = allPlayers.take(maxPlayersForCourts)
        val courts = playersForCourts.chunked(playersPerCourt).take(numCourts)
        var textcol = getColor(requireContext(), R.color.team1)
        Log.d("MatchFragment", "Teams to display after limiting to max players: $courts")

        // Add each court to the view
        courts.forEachIndexed { index, courtPlayers ->
            val courtLayout = TableLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Add court title
            val courtTitle = TextView(requireContext()).apply {
                text = "Court ${index + 1}"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
            }
            courtLayout.addView(courtTitle)

            // Add players to each court
            val row = TableRow(requireContext())
            courtPlayers.forEach { player ->
                val playerView = TextView(requireContext()).apply {
                    text = player.name + player.skill.toString()
                    textSize = 18f

                    if (courtPlayers.indexOf(player) == 0) {
                        textcol = getColor(requireContext(), R.color.team1)
                        setTextColor(textcol)
                        if (courtPlayers.size > 2) {
                            text = text as String + " and"
                        }
                    } else if (courtPlayers.indexOf(player) == 1) {
                        textcol = getColor(requireContext(), R.color.team1)
                        setTextColor(textcol)
                        if (courtPlayers.size ==2) {
                            val vs = TextView(requireContext()).apply {
                                text = " vs "
                                textSize = 18f
                            }
                            row.addView(vs)
                        }


                    } else if (courtPlayers.indexOf(player) == 2) {
                        textcol = getColor(requireContext(), R.color.team2)
                        setTextColor(textcol)
                        val vs = TextView(requireContext()).apply {
                            text = " vs "
                            textSize = 18f
                        }
                        row.addView(vs)
                        if (courtPlayers.indexOf(player) != courtPlayers.size - 1) {
                            text = text as String + " and"
                        }
                    } else {
                        textcol = getColor(requireContext(), R.color.team2)
                        setTextColor(textcol)
                    }
                    setPadding(8, 8, 8, 8)
                }
                row.addView(playerView)
            }
            courtLayout.addView(row)

            binding.courtsContainer.addView(courtLayout)
            Log.d(
                "MatchFragment",
                "Added layout for Court ${index + 1} with ${courtPlayers.size} players."
            )
        }

        // Calculate and display off-players
        val courtTitle = TextView(requireContext()).apply {
            text = "Off Players:"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }
        val courtLayout = TableLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        courtLayout.addView(courtTitle)

        val offPlayers = allPlayers.drop(maxPlayersForCourts)
        Log.d("MatchFragment", "Calculated off players: $offPlayers")
        val row = TableRow(requireContext())
        offPlayers.forEach { player ->
            val playerView = TextView(requireContext()).apply {
                text = player.name
                textSize = 18f
                if (offPlayers.indexOf(player) != offPlayers.size - 1) text = text as String + ","
                setPadding(8, 8, 8, 8)
            }
            row.addView(playerView)
        }
        courtLayout.addView(row)
        // Set off-players in the offPlayersAdapter
        binding.courtsContainer.addView(courtLayout)
        Log.d("MatchFragment", "Added layout for OffPlayers with ${offPlayers.size} players.")
        // (binding.offPlayersRecyclerView.adapter as PlayerListAdapter).submitList(offPlayers)
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
