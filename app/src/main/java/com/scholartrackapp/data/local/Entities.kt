package com.scholartrackapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String
)

@Entity(tableName = "scholarships")
data class ScholarshipEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val provider: String,
    val description: String,
    val amount: String,
    val deadline: String,
    val link: String,
    val location: String = "Global",
    val minAge: Int = 15,
    val maxAge: Int = 99,
    val level: String = "All"
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val title: String,
    val provider: String,
    val deadline: String,
    val status: String,
    val notes: String,
    val scholarshipId: Int? = null
)

enum class ApplicationStatus(val label: String) {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    SUBMITTED("Submitted"),
    AWARDED("Awarded"),
    REJECTED("Rejected")
}
