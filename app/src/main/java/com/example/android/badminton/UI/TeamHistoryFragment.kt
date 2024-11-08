package com.example.android.badminton.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.badminton.PlayersApplication
import com.example.android.badminton.databinding.FragmentTeamHistoryBinding
import com.example.android.badminton.ui.TeamHistoryViewModel
import com.example.android.badminton.ui.TeamHistoryViewModelFactory
import kotlinx.coroutines.launch

class TeamHistoryFragment : Fragment() {

    private var _binding: FragmentTeamHistoryBinding? = null
    private val binding get() = _binding!!
    private val teamHistoryViewModel: TeamHistoryViewModel by viewModels {
        TeamHistoryViewModelFactory((requireActivity().application as PlayersApplication).teamHistoryRepository)
    }


    private lateinit var adapter: TeamHistoryListAdapter // Placeholder for list adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter and RecyclerView
        adapter = TeamHistoryListAdapter()
        binding.recyclerViewTeamHistory.adapter = adapter
        binding.recyclerViewTeamHistory.layoutManager = LinearLayoutManager(requireContext())

        // Load and display team history entries
        loadTeamHistory()
    }

    private fun loadTeamHistory() {
        // Launch coroutine to retrieve and update the team history
        lifecycleScope.launch {
            // Example to fetch a specific team's history (replace `teamId` with actual id when needed)
            val teamId = 1
            val teamHistory = teamHistoryViewModel.getTeamHistory(teamId)
            teamHistory?.let {
                // Update adapter or UI with fetched team history data
                adapter.submitList(listOf(it))  // Display single team history for simplicity
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
