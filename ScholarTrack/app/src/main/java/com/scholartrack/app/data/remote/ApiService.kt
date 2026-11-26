package com.scholartrack.app.data.remote

import retrofit2.http.GET

/** Fields are nullable because Gson ignores Kotlin null-safety if the JSON is missing a field. */
data class ScholarshipDto(
    val id: Int?,
    val name: String?,
    val provider: String?,
    val description: String?,
    val amount: String?,
    val deadline: String?,
    val link: String?
)

interface ApiService {
    @GET("scholarships.json")
    suspend fun getScholarships(): List<ScholarshipDto>
}
