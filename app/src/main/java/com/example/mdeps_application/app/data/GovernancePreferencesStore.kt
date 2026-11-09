package com.example.mdeps_application.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val GOVERNANCE_PREFS = "governance_preferences"

val Context.governanceDataStore: DataStore<Preferences> by preferencesDataStore(name = GOVERNANCE_PREFS)

data class GovernancePolicies(
    val soc2Enabled: Boolean = true,
    val gateReviewsEnabled: Boolean = true,
    val budgetGuardrailsEnabled: Boolean = true,
)

class GovernancePreferencesStore(
    private val dataStore: DataStore<Preferences>,
) {
    fun observePolicies(): Flow<GovernancePolicies> =
        dataStore.data.map { prefs ->
            GovernancePolicies(
                soc2Enabled = prefs[KEY_SOC2] ?: true,
                gateReviewsEnabled = prefs[KEY_GATES] ?: true,
                budgetGuardrailsEnabled = prefs[KEY_BUDGET] ?: true,
            )
        }

    fun observeLastAuditAt(): Flow<Long?> =
        dataStore.data.map { prefs -> prefs[KEY_LAST_AUDIT] }

    suspend fun setSoc2Enabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SOC2] = enabled }
    }

    suspend fun setGateReviewsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_GATES] = enabled }
    }

    suspend fun setBudgetGuardrailsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BUDGET] = enabled }
    }

    suspend fun markAuditCompleted(atMillis: Long) {
        dataStore.edit { it[KEY_LAST_AUDIT] = atMillis }
    }

    private companion object {
        val KEY_SOC2 = booleanPreferencesKey("policy_soc2")
        val KEY_GATES = booleanPreferencesKey("policy_gates")
        val KEY_BUDGET = booleanPreferencesKey("policy_budget")
        val KEY_LAST_AUDIT = longPreferencesKey("last_full_audit_at")
    }
}
