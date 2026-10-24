package com.byga.flowmanager.organizer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.byga.flowmanager.organizer.data.DashboardPreferences
import com.byga.flowmanager.organizer.domain.DisplayMode
import com.byga.flowmanager.organizer.domain.SortMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardPreferencesTest {
    @Test fun oneGlobalBrowserAndWorkspacePreferencesPersistAcrossRepositoryInstances() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = DashboardPreferences(context)
        val original = prefs.settings.first()
        try {
            prefs.display(DisplayMode.GRID)
            prefs.sort(SortMode.RECENT)
            prefs.group("synthetic-group")
            prefs.browser("com.android.chrome")
            val read = DashboardPreferences(context).settings.first()
            assertEquals(DisplayMode.GRID, read.displayMode)
            assertEquals(SortMode.RECENT, read.sort)
            assertEquals("synthetic-group", read.groupId)
            assertEquals("com.android.chrome", read.browser)
        } finally {
            prefs.display(original.displayMode); prefs.sort(original.sort)
            prefs.group(original.groupId); prefs.browser(original.browser)
        }
    }
}
