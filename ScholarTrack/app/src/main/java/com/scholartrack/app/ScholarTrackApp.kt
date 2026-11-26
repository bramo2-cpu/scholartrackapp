package com.scholartrack.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.scholartrack.app.data.AppContainer
import com.scholartrack.app.worker.DeadlineWorker
import com.scholartrack.app.worker.NotificationHelper
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
