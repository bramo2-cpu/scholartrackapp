package com.scholartrack.app.data.repository

import com.scholartrack.app.data.local.*
import com.scholartrack.app.data.remote.ApiService
import com.scholartrack.app.data.remote.ScholarshipDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class ScholarshipRepository(
    private val scholarshipDao: ScholarshipDao,
    private val applicationDao: ApplicationDao,
    private val api: ApiService
) {
    val scholarships: Flow<List<ScholarshipEntity>> = scholarshipDao.observeAll()
    val applications: Flow<List<ApplicationEntity>> = applicationDao.observeAll()

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

    /** Returns false if an application with the same title already exists. */
    suspend fun addApplication(app: ApplicationEntity): Boolean {
        if (applicationDao.countByTitle(app.title, 0) > 0) return false
        applicationDao.insert(app)
        return true
    }

    suspend fun updateApplication(app: ApplicationEntity): Boolean {
        if (applicationDao.countByTitle(app.title, app.id) > 0) return false
        applicationDao.update(app)
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
        link = link ?: ""
    )
}
