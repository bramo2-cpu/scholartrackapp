package com.scholartrack.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScholarshipEntity::class, ApplicationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scholarshipDao(): ScholarshipDao
    abstract fun applicationDao(): ApplicationDao
}
