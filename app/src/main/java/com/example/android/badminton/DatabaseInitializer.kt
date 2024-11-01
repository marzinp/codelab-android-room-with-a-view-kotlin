package com.example.android.badminton

import android.content.Context
import android.util.Log
import com.example.android.badminton.data.Player
import com.example.android.badminton.data.PlayerRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object DatabaseInitializer {
    fun initializeDatabase(context: Context, scope: CoroutineScope): PlayerRoomDatabase {
        val db = PlayerRoomDatabase.getDatabase(context, scope)
        val dbFile = context.getDatabasePath("player_database")

        // Check if the database file exists and log it
        if (!dbFile.exists()) {
            Log.d("RoomDatabase", "Database file does not exist; will be created by Room.")

            // Trigger a write operation to finalize database creation
            scope.launch {
                db.playerDao().addPlayer(Player(0,"Dummy", 0,true)) // Insert a dummy player
                Log.d("RoomDatabase", "Triggered write operation to finalize database creation.")
            }
        } else {
            Log.d("RoomDatabase", "Database file exists; Room callback will handle population.")
        }

        return db
    }
}