package com.sakana.he.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HeTheme(
    darkTheme: Boolean = false,
    primaryColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            secondary = primaryColor.copy(alpha = 0.7f),
            tertiary = primaryColor.copy(alpha = 0.5f),
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1E1E1E),
            onSurfaceVariant = Color(0xFFCCCCCC),
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = primaryColor.copy(alpha = 0.7f),
            tertiary = primaryColor.copy(alpha = 0.5f),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF444444),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
