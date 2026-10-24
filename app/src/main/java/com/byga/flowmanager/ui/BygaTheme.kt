package com.byga.flowmanager.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF080D19)
val Blue = Color(0xFF246BFD)
val Cyan = Color(0xFF16DBEE)
val Muted = Color(0xFFAAB8D0)
val Amber = Color(0xFFFFC66D)
val Panel = Color(0xFF111C30)

@Composable fun BygaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Blue, onPrimary = Color.White, secondary = Cyan, onSecondary = Navy,
        background = Navy, onBackground = Color(0xFFF3F7FF),
        surface = Panel, onSurface = Color(0xFFF3F7FF),
        surfaceVariant = Color(0xFF182740), onSurfaceVariant = Muted,
        outline = Color(0xFF52647D), error = Color(0xFFFFB4AB)
    ), content = content)
}
