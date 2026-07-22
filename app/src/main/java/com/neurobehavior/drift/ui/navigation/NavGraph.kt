package com.neurobehavior.drift.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neurobehavior.drift.data.preferences.UserPreferencesRepository
import com.neurobehavior.drift.ui.screens.analytics.AnalyticsScreen
import com.neurobehavior.drift.ui.screens.dashboard.DashboardScreen
import com.neurobehavior.drift.ui.screens.drift.DriftAnalysisScreen
import com.neurobehavior.drift.ui.screens.recommendations.RecommendationsScreen
import com.neurobehavior.drift.ui.screens.settings.SettingsScreen
import com.neurobehavior.drift.ui.screens.splash.SplashScreen
import com.neurobehavior.drift.ui.screens.auth.LoginScreen
import com.neurobehavior.drift.ui.screens.auth.RegisterScreen
import com.neurobehavior.drift.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(navController: NavHostController, prefs: UserPreferencesRepository) {
    val userPrefs by prefs.userPreferencesFlow.collectAsState(initial = null)

    NavHost(navController = navController, startDestination = NavRoutes.Splash.route) {
        composable(NavRoutes.Splash.route) {
            SplashScreen {
                val token = userPrefs?.jwtToken ?: ""
                val target = if (token.isNotBlank()) NavRoutes.Dashboard.route else NavRoutes.Login.route
                navController.navigate(target) {
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
            }
        }
        composable(NavRoutes.Login.route)           { LoginScreen(navController) }
        composable(NavRoutes.Register.route)        { RegisterScreen(navController) }
        composable(NavRoutes.Dashboard.route)       { DashboardScreen(navController) }
        composable(NavRoutes.Analytics.route)       { AnalyticsScreen(navController) }
        composable(NavRoutes.DriftAnalysis.route)   { DriftAnalysisScreen(navController) }
        composable(NavRoutes.Recommendations.route) { RecommendationsScreen(navController) }
        composable(NavRoutes.Settings.route)        { SettingsScreen(navController) }
        composable(NavRoutes.Profile.route)         { ProfileScreen(navController) }
    }
}
