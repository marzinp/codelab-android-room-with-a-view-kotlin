package com.marzinp.badminton.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.marzinp.badminton.model.Player
import com.marzinp.badminton.model.Team


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
    @TypeConverter
    fun fromPlayerIdList(value: List<Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPlayerIdList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
