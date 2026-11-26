package com.scholartrackapp.data

import android.content.Context
import androidx.room.Room
import com.scholartrackapp.data.local.AppDatabase
import com.scholartrackapp.data.remote.ApiService
import com.scholartrackapp.data.repository.ScholarshipRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val prefs = context.getSharedPreferences("scholartrack_session", Context.MODE_PRIVATE)

    private val database: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "scholartrack.db")
            .fallbackToDestructiveMigration()
            .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    val repository = ScholarshipRepository(
        database.userDao(),
        database.scholarshipDao(),
        database.applicationDao(),
        api,
        prefs
    )

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}
