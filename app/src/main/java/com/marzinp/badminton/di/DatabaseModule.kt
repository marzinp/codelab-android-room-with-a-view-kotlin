package com.marzinp.badminton.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.marzinp.badminton.database.PlayerDao
import com.marzinp.badminton.repository.PlayerRepository
import com.marzinp.badminton.database.PlayerRoomDatabase
import com.marzinp.badminton.database.TeamDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    // Fournir une instance singleton de PlayerRoomDatabase
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): PlayerRoomDatabase {
        return Room.databaseBuilder(
            appContext,
            PlayerRoomDatabase::class.java,
            "player_database"
        ).build()
    }

    // Fournir une instance de TeamDao à partir de PlayerRoomDatabase
    @Provides
    fun provideTeamDao(database: PlayerRoomDatabase): TeamDao {
        return database.teamDao()
    }

}