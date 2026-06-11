package com.neurobehavior.drift.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neurobehavior.drift.ui.screens.analytics.AnalyticsScreen
import com.neurobehavior.drift.ui.screens.dashboard.DashboardScreen
import com.neurobehavior.drift.ui.screens.drift.DriftAnalysisScreen
import com.neurobehavior.drift.ui.screens.recommendations.RecommendationsScreen
import com.neurobehavior.drift.ui.screens.settings.SettingsScreen
import com.neurobehavior.drift.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.Splash.route) {
        composable(NavRoutes.Splash.route) {
            SplashScreen {
                navController.navigate(NavRoutes.Dashboard.route) {
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
            }
        }
        composable(NavRoutes.Dashboard.route)       { DashboardScreen(navController) }
        composable(NavRoutes.Analytics.route)       { AnalyticsScreen(navController) }
        composable(NavRoutes.DriftAnalysis.route)   { DriftAnalysisScreen(navController) }
        composable(NavRoutes.Recommendations.route) { RecommendationsScreen(navController) }
        composable(NavRoutes.Settings.route)        { SettingsScreen(navController) }
    }
}
