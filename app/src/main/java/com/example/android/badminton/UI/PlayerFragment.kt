package com.example.android.badminton.UI

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.badminton.PlayersApplication
import com.example.android.badminton.R
import com.example.android.badminton.data.Player
import com.example.android.badminton.databinding.FragmentPlayerBinding
import kotlinx.coroutines.launch

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
        val adapter = PlayerListAdapter(
            playerViewModel = playerViewModel,
            onItemLongClick = { player ->
                // Define what should happen when a player row is long-pressed
                showOptionsDialog(player)
            }
        )
        // Inside onViewCreated or similar in PlayerFragment
        // Inside onViewCreated or similar in PlayerFragment
        binding.fabAddPlayer.setOnClickListener {
            showPlayerOptionsDialog(null) // Passing null to indicate adding a new player
        }
        binding.recyclerviewPlayer.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        // Set up presentChecbox listener
        // In onViewCreated or a similar lifecycle method in PlayerFragment
        lifecycleScope.launch {
            playerViewModel.allPlayers.collect { players ->
                // Remove the listener temporarily
                binding.checkBoxPresent.setOnCheckedChangeListener(null)

                // Update the column checkbox based on individual player presence states
                val areAllPlayersPresent = players.all { it.isPresent }
                binding.checkBoxPresent.isChecked = areAllPlayersPresent

                // Reattach the listener
                binding.checkBoxPresent.setOnCheckedChangeListener { _, isChecked ->
                    playerViewModel.setAllPlayersPresent(isChecked)
                }
            }
        }

        // Listener for the column checkbox to update all players
        binding.checkBoxPresent.setOnCheckedChangeListener { _, isChecked ->
            playerViewModel.setAllPlayersPresent(isChecked)
        }
        // Set up Shuffle button listener
        binding.buttonShufflePlayer.setOnClickListener {
            val numCourts = binding.editTextNumCourts.text.toString().toIntOrNull() ?: 3
            val bundle = Bundle().apply {
                putInt("numCourts", numCourts)
            }
            findNavController().navigate(R.id.action_playerFragment_to_matchFragment, bundle)
            matchViewModel.shuffleTeams(numCourts)
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
    private fun showOptionsDialog(player: Player) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(requireContext())
            .setTitle("${player.name}'s Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPlayerOptionsDialog(player) // Edit selected
                    1 -> confirmDeletePlayer(player)     // Delete selected
                }
            }
            .show()
    }
    private fun confirmDeletePlayer(player: Player) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete ${player.name}?")
            .setMessage("Are you sure you want to delete this player?")
            .setPositiveButton("Delete") { _, _ ->
                playerViewModel.deletePlayer(player) // Assuming deletePlayer is defined in your ViewModel
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showPlayerOptionsDialog(player: Player?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (player == null) "Add Player" else "Edit Player")

        // Inflate the dialog layout and set up the fields
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_player, null)
        builder.setView(dialogView)

        // Get references to the input fields in dialog_player layout
        val nameInput = dialogView.findViewById<EditText>(R.id.editTextPlayerName)
        val skillInput = dialogView.findViewById<EditText>(R.id.editTextPlayerSkill)

        if (player != null) {
            // Pre-fill the fields with player data if editing
            nameInput.setText(player.name)
            skillInput.setText(player.skill.toString())
        }

        // Set up the dialog buttons
        builder.setPositiveButton(if (player == null) "Add" else "Update") { _, _ ->
            val name = nameInput.text.toString()
            val skill = skillInput.text.toString().toIntOrNull() ?: 0

            if (player == null) {
                // Add new player logic
                val newPlayer = Player(name = name, skill = skill, isPresent = true)
                playerViewModel.addPlayer(newPlayer)
            } else {
                // Update existing player logic
                val updatedPlayer = player.copy(name = name, skill = skill)
                playerViewModel.updatePlayer(updatedPlayer)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


    private fun editPlayer(player: Player) {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_player, null)
        val playerNameEditText = dialogView.findViewById<EditText>(R.id.editTextPlayerName)
        val playerSkillEditText = dialogView.findViewById<EditText>(R.id.editTextPlayerSkill)

        // Set existing values in the dialog
        playerNameEditText.setText(player.name)
        playerSkillEditText.setText(player.skill.toString())

        // Build the dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Player")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save the updated player information
                val newName = playerNameEditText.text.toString()
                val newSkill = playerSkillEditText.text.toString().toIntOrNull() ?: player.skill
                val updatedPlayer = player.copy(name = newName, skill = newSkill)
                playerViewModel.updatePlayer(updatedPlayer)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlayer(player: Player) {
        playerViewModel.deletePlayer(player)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
