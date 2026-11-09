package com.example.mdeps_application.iam.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val IAM_AUTH_DATA_STORE = "iam_auth_preferences"

val Context.iamAuthDataStore: DataStore<Preferences> by preferencesDataStore(name = IAM_AUTH_DATA_STORE)
