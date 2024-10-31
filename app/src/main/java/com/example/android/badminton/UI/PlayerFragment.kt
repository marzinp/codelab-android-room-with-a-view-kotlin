package com.example.android.badminton.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.badminton.PlayersApplication
import com.example.android.badminton.R
import com.example.android.badminton.databinding.FragmentPlayerBinding

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    // Initialize MatchViewModel and PlayerViewModel with required repositories
    private val matchViewModel: MatchViewModel by viewModels {
        MatchViewModelFactory(
            (requireActivity().application as PlayersApplication).matchRepository,
            (requireActivity().application as PlayersApplication).playerRepository,
            (requireActivity().application as PlayersApplication).teamRepository
        )
    }
    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((requireActivity().application as PlayersApplication).playerRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerviewPlayer
        val adapter = PlayerListAdapter(playerViewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Set up Shuffle button listener
        binding.buttonShuffle.setOnClickListener {
            val numCourts = binding.editTextNumCourts.text.toString().toIntOrNull() ?: 1
            Log.d("PlayerFragment", "Navigating to MatchFragment with $numCourts courts")
            matchViewModel.shuffleTeams(numCourts)
            val action = PlayerFragmentDirections.actionPlayerFragmentToMatchFragment(numCourts)
            findNavController().navigate(action)
        }

        // Observe the shuffled teams to update the UI whenever they are shuffled
        matchViewModel.shuffledTeams.observe(viewLifecycleOwner) { teams ->
            Log.d("PlayerFragment", "Observed shuffled teams: $teams")
            // Update the adapter or other UI elements with the shuffled teams if desired
        }

        // Observe sorted player data for the main player list
        playerViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter.submitList(players)
        }

        // Set up click listeners for sorting
        binding.textViewNameTitle.setOnClickListener {
            playerViewModel.toggleSortOrder(SortColumn.NAME)
        }

        binding.textViewSkillTitle.setOnClickListener {
            playerViewModel.toggleSortOrder(SortColumn.SKILL)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
