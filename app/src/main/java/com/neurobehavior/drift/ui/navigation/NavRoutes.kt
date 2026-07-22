package com.neurobehavior.drift.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash          : NavRoutes("splash")
    object Login           : NavRoutes("login")
    object Register        : NavRoutes("register")
    object Dashboard       : NavRoutes("dashboard")
    object Analytics       : NavRoutes("analytics")
    object DriftAnalysis   : NavRoutes("drift_analysis")
    object Recommendations : NavRoutes("recommendations")
    object Settings        : NavRoutes("settings")
    object Profile         : NavRoutes("profile")
}
