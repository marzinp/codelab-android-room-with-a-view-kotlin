package com.example.android.badminton.UI

import android.graphics.Typeface
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
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
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MatchFragment", "onViewCreated called and setting observer for shuffledTeams")

        // Initialize the off players RecyclerView as before
        val offPlayersAdapter = PlayerListAdapter(playerViewModel)
        binding.offPlayersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.offPlayersRecyclerView.adapter = offPlayersAdapter

        matchViewModel.shuffledTeams.observe(viewLifecycleOwner) { teams ->
            Log.d("MatchFragment", "Observer triggered with teams: $teams")

            val numCourts = arguments?.getInt("numCourts") ?: 1
            displayCourtsAndOffPlayers(teams, numCourts)

            val offPlayers = extractOffPlayers(teams, numCourts)
            Log.d("MatchFragment", "Off players calculated: $offPlayers")
            offPlayersAdapter.submitList(offPlayers)
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
        val maxPlayersForCourts = numCourts * playersPerCourt
        val allPlayers = teams.flatMap { it.teamPlayers }

        // Limit players to max allowed for courts and split into courts with playersPerCourt each
        val playersForCourts = allPlayers.take(maxPlayersForCourts)
        val courts = playersForCourts.chunked(playersPerCourt).take(numCourts)

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
                    text = player.name
                    setPadding(8, 8, 8, 8)
                }
                row.addView(playerView)
            }
            courtLayout.addView(row)

            binding.courtsContainer.addView(courtLayout)
            Log.d("MatchFragment", "Added layout for Court ${index + 1} with ${courtPlayers.size} players.")
        }

        // Calculate and display off-players
        val offPlayers = allPlayers.drop(maxPlayersForCourts)
        Log.d("MatchFragment", "Calculated off players: $offPlayers")

        // Set off-players in the offPlayersAdapter
        (binding.offPlayersRecyclerView.adapter as PlayerListAdapter).submitList(offPlayers)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
