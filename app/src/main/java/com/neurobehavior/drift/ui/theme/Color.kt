package com.neurobehavior.drift.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// AI Premium Theme Colors
val AIPrimary = Color(0xFF815BFF)
val AISecondary = Color(0xFF00D1FF)
val AITertiary = Color(0xFFFF2D55)

val DarkBackground = Color(0xFF0A0A0E)
val DarkSurface = Color(0xFF161622)
val DarkCard = Color(0xFF1D1D2B)

val GlassColor = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)

// Strain Levels
val StrainLow = Color(0xFF00E676)
val StrainModerate = Color(0xFFFFD600)
val StrainHigh = Color(0xFFFF5252)
val StrainCritical = Color(0xFFFF1744)
val DriftBlue = Color(0xFF448AFF)

val NeuralPurple = AIPrimary
val NeuralTeal = AISecondary

// Gradients
val PremiumGradient = Brush.linearGradient(
    colors = listOf(AIPrimary, AISecondary)
)

val CardGradient = Brush.linearGradient(
    colors = listOf(DarkCard, Color(0xFF252538))
)

val AnomalyGradient = Brush.linearGradient(
    colors = listOf(AITertiary, Color(0xFFFF5E7D))
)
