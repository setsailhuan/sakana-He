package com.sakana.he

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.sakana.he.navigation.AppNavigation
import com.sakana.he.ui.theme.DefaultThemeColor
import com.sakana.he.ui.theme.HeTheme
import com.sakana.he.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val darkMode by appViewModel.darkMode.collectAsStateWithLifecycle()
            val themeColorHex by appViewModel.themeColor.collectAsStateWithLifecycle()

            val themeColor = runCatching {
                Color(android.graphics.Color.parseColor(themeColorHex))
            }.getOrDefault(DefaultThemeColor)

            HeTheme(darkTheme = darkMode, primaryColor = themeColor) {
                AppNavigation(appViewModel = appViewModel, themeColor = themeColor)
            }
        }
    }
}
