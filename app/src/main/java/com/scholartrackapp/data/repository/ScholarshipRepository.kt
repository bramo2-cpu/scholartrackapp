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

private fun ScholarshipDto.toEntity(): ScholarshipEntity? {
    val id = id ?: return null
    return ScholarshipEntity(
        id = id,
        name = name ?: "Untitled",
        provider = provider ?: "Unknown provider",
        description = description ?: "",
        amount = amount ?: "Not specified",
        deadline = deadline ?: "",
        link = link ?: "",
        location = location ?: "Global",
        minAge = minAge ?: 15,
        maxAge = maxAge ?: 99,
        level = level ?: "All"
    )
}
