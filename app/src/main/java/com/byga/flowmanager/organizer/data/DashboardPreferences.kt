package com.byga.flowmanager.organizer.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.byga.flowmanager.organizer.domain.*
import kotlinx.coroutines.flow.map

private val Context.dashboardPreferences by preferencesDataStore("dashboard_settings")
class DashboardPreferences(context: Context) {
    private val data = context.dashboardPreferences
    private val display = stringPreferencesKey("display")
    private val sort = stringPreferencesKey("sort")
    private val group = stringPreferencesKey("group")
    private val browser = stringPreferencesKey("browser")
    private val lock = booleanPreferencesKey("lock")
    val settings = data.data.map { p -> DashboardSettings(
        DisplayMode.entries.find { it.name == p[display] } ?: DisplayMode.LIST,
        SortMode.entries.find { it.name == p[sort] } ?: SortMode.NAME,
        p[group]?.takeIf { it.isNotBlank() }, p[browser].orEmpty(), p[lock] ?: false
    ) }
    suspend fun display(value: DisplayMode) = data.edit { it[display] = value.name }
    suspend fun sort(value: SortMode) = data.edit { it[sort] = value.name }
    suspend fun group(value: String?) = data.edit { if (value == null) it.remove(group) else it[group] = value }
    suspend fun browser(value: String) = data.edit { it[browser] = value }
    suspend fun lock(value: Boolean) = data.edit { it[lock] = value }
}
