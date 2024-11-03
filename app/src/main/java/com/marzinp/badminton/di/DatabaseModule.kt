package com.marzinp.badminton.di

import android.app.Application
import androidx.room.Room
import com.marzinp.badminton.database.PlayerDao
import com.marzinp.badminton.repository.PlayerRepository
import com.marzinp.badminton.database.PlayerRoomDatabase
import com.marzinp.badminton.database.TeamHistoryDao
import com.marzinp.badminton.repository.TeamHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePlayerRepository(playerDao: PlayerDao): PlayerRepository {
        return PlayerRepository(playerDao)
    }

    @Provides
    @Singleton
    fun providePlayerDao(db: PlayerRoomDatabase): PlayerDao {
        return db.playerDao()
    }

    @Provides
    @Singleton
    fun provideTeamHistoryRepository(teamHistoryDao: TeamHistoryDao): TeamHistoryRepository {
        return TeamHistoryRepository(teamHistoryDao)
    }

    @Provides
    fun provideTeamHistoryDao(db: PlayerRoomDatabase): TeamHistoryDao {
        return db.teamHistoryDao()
    }
    @Provides
    @Singleton
    fun provideDatabase(app: Application): PlayerRoomDatabase {
        return Room.databaseBuilder(app, PlayerRoomDatabase::class.java, "player_database")
            .build()
    }
}