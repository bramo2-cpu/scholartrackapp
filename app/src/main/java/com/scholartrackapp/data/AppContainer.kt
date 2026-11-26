package com.scholartrackapp.data

import android.content.Context
import androidx.room.Room
import com.scholartrackapp.data.local.AppDatabase
import com.scholartrackapp.data.remote.ApiService
import com.scholartrackapp.data.repository.ScholarshipRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Manual dependency injection: builds the database, API and repository once. */
class AppContainer(context: Context) {

    private val database: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "scholartrack.db").build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    val repository = ScholarshipRepository(
        database.scholarshipDao(),
        database.applicationDao(),
        api
    )

    companion object {
        // TODO: replace with your own GitHub raw URL. Must end with "/".
        const val BASE_URL = "https://raw.githubusercontent.com/bramo2-cpu/scholartrackapp/main/"
    }
}

