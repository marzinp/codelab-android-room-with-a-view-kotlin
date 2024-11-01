package com.example.android.badminton.UI

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.badminton.PlayersApplication
import com.example.android.badminton.R
import com.example.android.badminton.UserSession
import com.example.android.badminton.data.Player
import com.example.android.badminton.databinding.FragmentPlayerBinding
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

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
            onItemLongClick = { player -> showOptionsDialog(player) }
        )

        UserSession.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            updateVisibility(isAdmin)
        }

        binding.fabAddPlayer.setOnClickListener {
            showPlayerOptionsDialog(null)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            playerViewModel.allPlayers.collect { players ->
                binding.checkBoxPresent.setOnCheckedChangeListener(null)
                binding.checkBoxPresent.isChecked = players.all { it.isPresent }
                binding.checkBoxPresent.setOnCheckedChangeListener { _, isChecked ->
                    playerViewModel.setAllPlayersPresent(isChecked)
                }
            }
        }

        binding.buttonShufflePlayer.setOnClickListener {
            val numCourts = binding.editTextNumCourts.text.toString().toIntOrNull() ?: 3
            val bundle = Bundle().apply { putInt("numCourts", numCourts) }
            findNavController().navigate(R.id.action_playerFragment_to_matchFragment, bundle)
            matchViewModel.shuffleTeams(numCourts)
        }

        playerViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter.submitList(players)
        }

        binding.textViewNameTitle.setOnClickListener {
            playerViewModel.toggleSortOrder(SortColumn.NAME)
        }

        binding.textViewSkillTitle.setOnClickListener {
            playerViewModel.toggleSortOrder(SortColumn.SKILL)
        }
    }

    private fun showOptionsDialog(player: Player) {
        val options = arrayOf("Edit", "Delete")
        if (UserSession.isAdmin.value == true) {
            AlertDialog.Builder(requireContext())
                .setTitle("${player.name}'s Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showPlayerOptionsDialog(player) // Edit selected
                        1 -> confirmDeletePlayer(player)     // Delete selected
                    }
                }
                .show()
        } else {
            Toast.makeText(context, "Admin permissions required", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showPlayerOptionsDialog(player: Player?) {
        if (UserSession.isAdmin.value == true) {
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
        } else {
            Toast.makeText(context, "Admin permissions required", Toast.LENGTH_SHORT).show()
        }
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

    private fun updateVisibility(isAdmin: Boolean) {
        Log.d("PlayerFragment", "Admin status updated: $isAdmin")
        binding.fabAddPlayer.visibility = if (isAdmin) View.VISIBLE else View.GONE
        (binding.recyclerviewPlayer.adapter as? PlayerListAdapter)?.setAdminVisibility(isAdmin)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
