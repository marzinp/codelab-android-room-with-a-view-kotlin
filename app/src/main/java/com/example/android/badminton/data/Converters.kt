package com.example.android.badminton.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {

    private val gson = Gson()

    // Converters for List<Player>
    @TypeConverter
    fun fromPlayerList(players: List<Player>): String {
        return gson.toJson(players)
    }

    @TypeConverter
    fun toPlayerList(playersString: String): List<Player> {
        val listType = object : TypeToken<List<Player>>() {}.type
        return gson.fromJson(playersString, listType)
    }

    // Converters for List<Team>
    @TypeConverter
    fun fromTeamList(teams: List<Team>): String {
        return gson.toJson(teams)
    }

    @TypeConverter
    fun toTeamList(teamsString: String): List<Team> {
        val listType = object : TypeToken<List<Team>>() {}.type
        return gson.fromJson(teamsString, listType)
    }
}
