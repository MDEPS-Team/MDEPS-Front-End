package com.example.mdeps_application.gro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RiskEntity::class,
        MilestoneEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(GroTypeConverters::class)
abstract class GroDatabase : RoomDatabase() {

    abstract fun riskDao(): RiskDao

    abstract fun milestoneDao(): MilestoneDao

    companion object {
        private const val DATABASE_NAME = "gro_database"

        fun create(context: Context): GroDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                GroDatabase::class.java,
                DATABASE_NAME,
            ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
