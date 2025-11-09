package com.example.levelup

import android.app.Application
import androidx.room.Room
import com.example.levelup.data.local.db.AppDatabase

class LevelUpApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializamos Room una sola vez (singleton global)
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "levelup_db"
        )
            .fallbackToDestructiveMigration() // útil en desarrollo si cambias entidades
            .build()
    }
}
