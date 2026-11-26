package com.scholartrackapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scholartrackapp.ScholarTrackApp

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = (applicationContext as ScholarTrackApp).container.repository
        return if (repo.refreshScholarships().isSuccess) Result.success() else Result.retry()
    }
}
