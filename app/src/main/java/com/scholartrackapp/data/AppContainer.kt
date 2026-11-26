package com.scholartrackapp.data.repository

import android.content.SharedPreferences
import com.scholartrackapp.data.local.*
import com.scholartrackapp.data.remote.ApiService
import com.scholartrackapp.data.remote.ScholarshipDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ScholarshipRepository(
    private val userDao: UserDao,
    private val scholarshipDao: ScholarshipDao,
    private val applicationDao: ApplicationDao,
    private val api: ApiService,
    private val prefs: SharedPreferences
) {
    private val _currentUserId = MutableStateFlow(prefs.getInt("user_id", -1).takeIf { it != -1 })
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    val scholarships: Flow<List<ScholarshipEntity>> = scholarshipDao.observeAll()

    @OptIn(ExperimentalCoroutinesApi::class)
    val applications: Flow<List<ApplicationEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else applicationDao.observeByUser(uid)
    }

    suspend fun register(fullName: String, email: String, password: String): Result<UserEntity> = try {
        val trimmedEmail = email.trim()
        if (userDao.findByEmail(trimmedEmail) != null) {
            Result.failure(IllegalArgumentException("An account with this email already exists."))
        } else {
            val id = userDao.insert(
                UserEntity(fullName = fullName.trim(), email = trimmedEmail, password = password)
            ).toInt()
            prefs.edit().putInt("user_id", id).apply()
            _currentUserId.value = id
            Result.success(UserEntity(id = id, fullName = fullName.trim(), email = trimmedEmail, password = password))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> = try {
        val user = userDao.login(email.trim(), password)
        if (user != null) {
            prefs.edit().putInt("user_id", user.id).apply()
            _currentUserId.value = user.id
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Invalid email or password."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() {
        prefs.edit().remove("user_id").apply()
        _currentUserId.value = null
    }

    suspend fun getCurrentUser(): UserEntity? = _currentUserId.value?.let { userDao.getById(it) }

    /** API -> DTO -> Entity -> Room. The UI observes Room, so it updates automatically. */
    suspend fun refreshScholarships(): Result<Unit> = try {
        val remote = api.getScholarships()
        scholarshipDao.replaceAll(remote.mapNotNull { it.toEntity() })
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getScholarship(id: Int) = scholarshipDao.getById(id)
    suspend fun getApplication(id: Int) = applicationDao.getById(id)
    suspend fun getActiveApplications() = applicationDao.getActive()

    /** Returns false if an application with the same title already exists for this user. */
    suspend fun addApplication(app: ApplicationEntity): Boolean {
        val uid = _currentUserId.value ?: return false
        if (applicationDao.countByTitle(uid, app.title, 0) > 0) return false
        applicationDao.insert(app.copy(userId = uid))
        return true
    }

    suspend fun updateApplication(app: ApplicationEntity): Boolean {
        val uid = _currentUserId.value ?: return false
        if (applicationDao.countByTitle(uid, app.title, app.id) > 0) return false
        applicationDao.update(app.copy(userId = uid))
        return true
    }

    suspend fun deleteApplication(app: ApplicationEntity) = applicationDao.delete(app)
}

private fun ScholarshipDto


# Finish Step 4: AppContainer.kt
@'
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
        const val BASE_URL = "https://raw.githubusercontent.com/bramo2-cpu/scholartrackapp/main/"
    }
}
