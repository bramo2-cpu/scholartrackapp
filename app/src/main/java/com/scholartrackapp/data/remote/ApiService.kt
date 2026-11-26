package com.scholartrackapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers

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
    @Headers(
        "Accept: application/vnd.github.raw+json",
        "Cache-Control: no-cache"
    )
    @GET("repos/bramo2-cpu/scholartrackapp/contents/scholarships.json")
    suspend fun getScholarships(): List<ScholarshipDto>
}
