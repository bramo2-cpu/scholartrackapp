package com.scholartrackapp

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.scholartrackapp.data.AppContainer
import com.scholartrackapp.worker.DeadlineWorker
import com.scholartrackapp.worker.NotificationHelper
import com.scholartrackapp.worker.SyncWorker
import java.util.concurrent.TimeUnit

class ScholarTrackApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
        scheduleDeadlineReminders()
        scheduleScholarshipSync()
    }

    private fun scheduleDeadlineReminders() {
        val request = PeriodicWorkRequestBuilder<DeadlineWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "deadline_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleScholarshipSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "scholarship_web_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
