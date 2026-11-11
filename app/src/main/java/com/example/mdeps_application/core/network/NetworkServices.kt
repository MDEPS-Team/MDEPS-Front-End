package com.example.mdeps_application.core.network

import com.example.mdeps_application.ass.data.remote.AssApiService
import com.example.mdeps_application.gro.data.remote.GroApiService
import com.example.mdeps_application.iam.data.remote.IamApiService
import com.example.mdeps_application.pm.data.remote.PmApiService
import com.example.mdeps_application.pm.data.remote.SupportApiService
import com.example.mdeps_application.pto.data.remote.AgendaApiService
import com.example.mdeps_application.pto.data.remote.PtoApiService

/**
 * Central registry for Retrofit API interfaces.
 *
 * Phase 0+: every new bounded context adds its service here (e.g. PtoApiService in Phase 2).
 */
object NetworkServices {

    val iam: IamApiService
        get() = RetrofitClient.createService(IamApiService::class.java)

    val pm: PmApiService
        get() = RetrofitClient.createService(PmApiService::class.java)

    val pto: PtoApiService
        get() = RetrofitClient.createService(PtoApiService::class.java)

    val ass: AssApiService
        get() = RetrofitClient.createService(AssApiService::class.java)

    val gro: GroApiService
        get() = RetrofitClient.createService(GroApiService::class.java)

    val agenda: AgendaApiService
        get() = RetrofitClient.createService(AgendaApiService::class.java)

    val support: SupportApiService
        get() = RetrofitClient.createService(SupportApiService::class.java)
}
