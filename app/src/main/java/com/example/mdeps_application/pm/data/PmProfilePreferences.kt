package com.example.mdeps_application.pm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val PROFILE_DATA_STORE = "profile_preferences"

val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = PROFILE_DATA_STORE)
