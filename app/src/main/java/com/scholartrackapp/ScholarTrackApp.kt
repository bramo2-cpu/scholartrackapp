package com.scholartrackapp

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.scholartrackapp.data.AppContainer
import com.scholartrackapp.worker.DeadlineWorker
import com.scholartrackapp.worker.NotificationHelper
import java.util.concurrent.TimeUnit

class ScholarTrackApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
        scheduleDeadlineReminders()
    }

    private fun scheduleDeadlineReminders() {
        val request = PeriodicWorkRequestBuilder<DeadlineWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "deadline_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

