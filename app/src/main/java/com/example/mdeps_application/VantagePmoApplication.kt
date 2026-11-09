package com.example.mdeps_application

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mdeps_application.gro.work.MilestoneReminderWorker
import java.util.concurrent.TimeUnit

class VantagePmoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleMilestoneReminders()
    }

    private fun scheduleMilestoneReminders() {
        val request = PeriodicWorkRequestBuilder<MilestoneReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MilestoneReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
