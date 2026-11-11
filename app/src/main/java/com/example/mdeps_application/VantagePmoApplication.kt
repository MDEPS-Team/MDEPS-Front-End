package com.example.mdeps_application

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mdeps_application.core.network.AuthSessionCoordinator
import com.example.mdeps_application.core.network.RetrofitClient
import com.example.mdeps_application.gro.work.MilestoneReminderWorker
import com.example.mdeps_application.iam.data.iamAuthDataStore
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VantagePmoApplication : Application() {

    lateinit var authSession: AuthSessionCoordinator
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        authSession = AuthSessionCoordinator(iamAuthDataStore)
        RetrofitClient.initialize(authSession)
        applicationScope.launch {
            authSession.hydrate()
        }
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
