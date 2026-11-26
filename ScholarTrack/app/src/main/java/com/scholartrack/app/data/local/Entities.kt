package com.scholartrack.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached copy of scholarships fetched from the API (lets the app work offline). */
@Entity(tableName = "scholarships")
data class ScholarshipEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val provider: String,
    val description: String,
    val amount: String,
    val deadline: String,   // yyyy-MM-dd
    val link: String
)

/** An application the student is tracking. Created/edited by the user. */
@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val provider: String,
    val deadline: String,   // yyyy-MM-dd
    val status: String,     // ApplicationStatus.name
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
