package com.marzinp.badminton.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marzinp.badminton.R
import com.marzinp.badminton.data.TeamHistory

class TeamHistoryListAdapter :
    ListAdapter<TeamHistory, TeamHistoryListAdapter.TeamHistoryViewHolder>(TeamHistoryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_team_history_item, parent, false)
        return TeamHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TeamHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teamIdTextView: TextView = itemView.findViewById(R.id.textViewTeamId)

        fun bind(teamHistory: TeamHistory) {
            teamIdTextView.text = "Team ID: ${teamHistory.teamId}"
        }
    }

    companion object {
        private val TeamHistoryDiffCallback = object : DiffUtil.ItemCallback<TeamHistory>() {
            override fun areItemsTheSame(oldItem: TeamHistory, newItem: TeamHistory): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TeamHistory, newItem: TeamHistory): Boolean {
                return oldItem == newItem
            }
        }
    }
}
