package com.marzinp.badminton

import com.marzinp.badminton.model.Player

interface MatchTypePromptListener {
    fun showMatchTypePrompt(presentPlayers: List<Player>, numCourts: Int, playersPerCourt: Int)
}