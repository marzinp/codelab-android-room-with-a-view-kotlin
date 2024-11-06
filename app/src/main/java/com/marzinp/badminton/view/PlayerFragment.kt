package com.marzinp.badminton.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.marzinp.badminton.R
import com.marzinp.badminton.UserSession
import com.marzinp.badminton.viewmodel.MatchViewModel
import com.marzinp.badminton.viewmodel.PlayerViewModel
import com.marzinp.badminton.viewmodel.SortColumn
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val matchViewModel: MatchViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

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

        // Use viewLifecycleOwner.lifecycleScope for coroutine collection
        viewLifecycleOwner.lifecycleScope.launch {
            playerViewModel.allPlayers.collect { players ->
                binding?.let { safeBinding ->
                    if(safeBinding.editTextNumCourts.text.isEmpty())safeBinding.editTextNumCourts.setText("3")
                    safeBinding.checkBoxPresent.setOnCheckedChangeListener(null)
                    safeBinding.checkBoxPresent.isChecked = players.all { it.isPresent }
                    safeBinding.checkBoxPresent.setOnCheckedChangeListener { _, isChecked ->
                        playerViewModel.setAllPlayersPresent(isChecked)
                    }
                }
            }
        }

        binding.buttonShufflePlayer.setOnClickListener {
            binding?.let { safeBinding ->
                val numCourts = safeBinding.editTextNumCourts.text.toString().toIntOrNull() ?: 3
                val bundle = Bundle().apply { putInt("numCourts", numCourts) }
                findNavController().navigate(R.id.action_playerFragment_to_matchFragment, bundle)
                matchViewModel.shuffleTeams(numCourts)
            }
        }

        binding.buttonResetOffCount.setOnClickListener {
            playerViewModel.resetOffCountForAllPlayers()
            Toast.makeText(requireContext(), "Off count reset for all players", Toast.LENGTH_SHORT).show()
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

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_player, null)
            builder.setView(dialogView)

            val nameInput = dialogView.findViewById<EditText>(R.id.editTextPlayerName)
            val skillInput = dialogView.findViewById<EditText>(R.id.editTextPlayerSkill)

            if (player != null) {
                nameInput.setText(player.name)
                skillInput.setText(player.skill.toString())
            }

            builder.setPositiveButton(if (player == null) "Add" else "Update") { _, _ ->
                val name = nameInput.text.toString()
                val skill = skillInput.text.toString().toIntOrNull() ?: 0

                if (player == null) {
                    val newPlayer = Player(name = name, skill = skill, isPresent = true)
                    playerViewModel.addPlayer(newPlayer)
                } else {
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
                playerViewModel.deletePlayer(player)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateVisibility(isAdmin: Boolean) {
        Log.d("PlayerFragment", "Admin status updated: $isAdmin")
        binding?.let { safeBinding ->
            safeBinding.fabAddPlayer.visibility = if (isAdmin) View.VISIBLE else View.GONE
            safeBinding.buttonResetOffCount.visibility= if (isAdmin) View.VISIBLE else View.GONE
            (safeBinding.recyclerviewPlayer.adapter as? PlayerListAdapter)?.setAdminVisibility(isAdmin)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
