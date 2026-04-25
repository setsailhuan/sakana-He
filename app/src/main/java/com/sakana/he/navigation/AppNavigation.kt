package com.sakana.he.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sakana.he.ui.screens.HistoryScreen
import com.sakana.he.ui.screens.HomeScreen
import com.sakana.he.ui.screens.SettingsScreen
import com.sakana.he.viewmodel.AppViewModel
import com.sakana.he.viewmodel.HistoryViewModel
import com.sakana.he.viewmodel.HomeViewModel
import com.sakana.he.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(appViewModel: AppViewModel, themeColor: Color) {
    val navController = rememberNavController()
    val homeVm: HomeViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home: exits/enters depend on which screen we're going to/from
        composable(
            route = "home",
            exitTransition = {
                when (targetState.destination.route) {
                    "history" -> slideOutHorizontally { it }   // home exits right, history comes from left
                    "settings" -> slideOutHorizontally { -it } // home exits left, settings comes from right
                    else -> slideOutHorizontally { -it }
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    "history" -> slideInHorizontally { it }    // returning from history: home enters from right
                    "settings" -> slideInHorizontally { -it }  // returning from settings: home enters from left
                    else -> slideInHorizontally { it }
                }
            }
        ) {
            HomeScreen(
                viewModel = homeVm,
                themeColor = themeColor,
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        // History: calendar is conceptually to the LEFT of home
        composable(
            route = "history",
            enterTransition = { slideInHorizontally { -it } },  // slides in from left
            popExitTransition = { slideOutHorizontally { -it } } // exits back to left
        ) {
            HistoryScreen(
                viewModel = historyVm,
                appViewModel = appViewModel,
                themeColor = themeColor,
                onBack = { navController.popBackStack() }
            )
        }

        // Settings: is conceptually to the RIGHT of home
        composable(
            route = "settings",
            enterTransition = { slideInHorizontally { it } },   // slides in from right
            popExitTransition = { slideOutHorizontally { it } } // exits back to right
        ) {
            SettingsScreen(
                viewModel = settingsVm,
                appViewModel = appViewModel,
                themeColor = themeColor,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
