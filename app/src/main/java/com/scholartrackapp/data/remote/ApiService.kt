package com.scholartrackapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers

data class ScholarshipDto(
    val id: Int?,
    val name: String?,
    val provider: String?,
    val category: String?,
    val description: String?,
    val amount: String?,
    val deadline: String?,
    val link: String?,
    val location: String?,
    val minAge: Int?,
    val maxAge: Int?,
    val level: String?
)

interface ApiService {
    @Headers(
        "Accept: application/vnd.github.raw+json",
        "Cache-Control: no-cache"
    )
    @GET("repos/bramo2-cpu/scholartrackapp/contents/scholarships.json")
    suspend fun getScholarships(): List<ScholarshipDto>
}
