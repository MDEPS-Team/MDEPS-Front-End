package com.example.mdeps_application.pto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class PtoDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        private const val DATABASE_NAME = "pto_database"

        /** v2: campos del tablero Kanban. Conserva las tareas existentes (no destructiva). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT 'TODO'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'P2'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN due_date INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN assignee TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN story_points INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT")
                db.execSQL("UPDATE tasks SET status = 'DONE' WHERE is_completed = 1")
            }
        }

        fun create(context: Context): PtoDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                PtoDatabase::class.java,
                DATABASE_NAME,
            ).addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
