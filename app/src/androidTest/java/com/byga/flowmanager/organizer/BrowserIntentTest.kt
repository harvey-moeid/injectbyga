package com.byga.flowmanager.organizer

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byga.flowmanager.launcher.FlowLauncher
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrowserIntentTest {
    @Test fun flowIntentPinsOneProviderAndNeverIncludesEmailOrAuthenticationData() {
        val intent = FlowLauncher.externalBrowserIntent("com.android.chrome")
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("com.android.chrome", intent.`package`)
        assertEquals(FlowLauncher.FLOW_URL, intent.data.toString())
        assertTrue(intent.hasCategory(Intent.CATEGORY_BROWSABLE))
        assertNull(intent.extras)
        assertNull(intent.data?.query)
    }
}
