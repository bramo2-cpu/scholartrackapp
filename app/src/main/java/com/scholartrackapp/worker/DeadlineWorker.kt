package com.scholartrackapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scholartrackapp.ScholarTrackApp
import com.scholartrackapp.util.daysLeft

/** Runs periodically in the background, even if the app is closed. */
class DeadlineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = (applicationContext as ScholarTrackApp).container.repository
        repo.getActiveApplications().forEach { app ->
            val days = daysLeft(app.deadline) ?: return@forEach
            if (days in 0..7) {
                val whenText = if (days == 0L) "today" else "in $days day(s)"
                NotificationHelper.show(
                    applicationContext, app.id,
                    "Deadline approaching",
                    "${app.title} is due $whenText"
                )
            }
        }
        return Result.success()
    }
}

