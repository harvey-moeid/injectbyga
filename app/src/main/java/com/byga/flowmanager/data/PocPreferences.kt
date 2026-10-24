package com.byga.flowmanager.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.byga.flowmanager.domain.Scenario
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.pocSettings by preferencesDataStore("poc_settings")
data class PocSettings(val scenario: Scenario = Scenario.ABC, val run: String = "awal", val showGuide: Boolean = true)
class PocPreferences(context: Context) {
    private val store = context.pocSettings
    private val scenarioKey = stringPreferencesKey("scenario")
    private val runKey = stringPreferencesKey("run")
    private val guideKey = booleanPreferencesKey("show_guide")
    val settings = store.data.map { data ->
        PocSettings(Scenario.entries.find { it.name == data[scenarioKey] } ?: Scenario.ABC,
            data[runKey] ?: "awal", data[guideKey] ?: true)
    }
    suspend fun newRun(scenario: Scenario) = store.edit {
        it[scenarioKey] = scenario.name
        it[runKey] = UUID.randomUUID().toString()
    }
    suspend fun showGuide(value: Boolean) = store.edit { it[guideKey] = value }
}
