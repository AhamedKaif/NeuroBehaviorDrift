package com.neurobehavior.drift.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash          : NavRoutes("splash")
    object Dashboard       : NavRoutes("dashboard")
    object Analytics       : NavRoutes("analytics")
    object DriftAnalysis   : NavRoutes("drift_analysis")
    object Recommendations : NavRoutes("recommendations")
    object Settings        : NavRoutes("settings")
}
