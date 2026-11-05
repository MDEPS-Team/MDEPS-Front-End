package com.example.mdeps_application.pto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class PtoDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        private const val DATABASE_NAME = "pto_database"

        fun create(context: Context): PtoDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                PtoDatabase::class.java,
                DATABASE_NAME,
            ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
