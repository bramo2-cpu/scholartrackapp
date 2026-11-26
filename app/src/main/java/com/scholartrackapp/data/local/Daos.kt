package com.scholartrackapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): UserEntity?
}

@Dao
abstract class ScholarshipDao {
    @Query("SELECT * FROM scholarships ORDER BY deadline ASC")
    abstract fun observeAll(): Flow<List<ScholarshipEntity>>

    @Query("SELECT * FROM scholarships WHERE id = :id")
    abstract suspend fun getById(id: Int): ScholarshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<ScholarshipEntity>)

    @Query("DELETE FROM scholarships")
    abstract suspend fun clear()

    /** Clear + insert in one transaction so the list never flashes empty. */
    @Transaction
    open suspend fun replaceAll(items: List<ScholarshipEntity>) {
        clear()
        insertAll(items)
    }
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE userId = :userId ORDER BY deadline ASC")
    fun observeByUser(userId: Int): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getById(id: Int): ApplicationEntity?

    @Query("SELECT COUNT(*) FROM applications WHERE userId = :userId AND LOWER(title) = LOWER(:title) AND id != :excludeId")
    suspend fun countByTitle(userId: Int, title: String, excludeId: Int): Int

    @Query("SELECT * FROM applications WHERE status IN ('NOT_STARTED', 'IN_PROGRESS')")
    suspend fun getActive(): List<ApplicationEntity>

    @Insert
    suspend fun insert(app: ApplicationEntity): Long

    @Update
    suspend fun update(app: ApplicationEntity)

    @Delete
    suspend fun delete(app: ApplicationEntity)
}
