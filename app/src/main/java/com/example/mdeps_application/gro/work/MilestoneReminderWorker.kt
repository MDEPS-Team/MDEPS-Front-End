package com.example.mdeps_application.gro.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mdeps_application.gro.data.local.GroDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

class MilestoneReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val dao = GroDatabase.create(applicationContext).milestoneDao()
            val now = Instant.now()
            val horizon = now.plus(7, ChronoUnit.DAYS)
            val upcoming = dao.getAllMilestonesOnce().filter { milestone ->
                !milestone.is_reached &&
                    milestone.target_date in now.toEpochMilli()..horizon.toEpochMilli()
            }
            upcoming.size
            // Placeholder: hook notifications when a channel is configured.
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "milestone_reminder_check"
    }
}
