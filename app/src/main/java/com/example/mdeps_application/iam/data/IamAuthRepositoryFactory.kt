package com.example.mdeps_application.iam.data

import android.content.Context
import com.example.mdeps_application.VantagePmoApplication
import com.example.mdeps_application.core.network.AuthSessionCoordinator

fun Context.dataStoreAuthRepository(): DataStoreAuthRepository {
    return DataStoreAuthRepository(iamAuthDataStore, appAuthSession())
}

fun Context.appAuthSession(): AuthSessionCoordinator {
    return (applicationContext as VantagePmoApplication).authSession
}
