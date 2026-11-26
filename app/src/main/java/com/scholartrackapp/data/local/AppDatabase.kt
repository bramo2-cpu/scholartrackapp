package com.scholartrackapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, ScholarshipEntity::class, ApplicationEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun scholarshipDao(): ScholarshipDao
    abstract fun applicationDao(): ApplicationDao
}
