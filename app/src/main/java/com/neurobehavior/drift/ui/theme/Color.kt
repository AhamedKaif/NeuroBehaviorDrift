package com.neurobehavior.drift.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// AI Premium Theme Colors (website matches)
val AIPrimary = Color(0xFF3B82F6) // accentBlue
val AISecondary = Color(0xFF10B981) // strainLow / emerald
val AITertiary = Color(0xFFEF4444) // strainHigh / rose

val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF151C2C)
val DarkCard = Color(0xFF151C2C)

val GlassColor = Color(0x1AFFFFFF)
val GlassBorder = Color(0x0DFFFFFF)

// Strain Levels
val StrainLow = Color(0xFF10B981)
val StrainModerate = Color(0xFFF59E0B)
val StrainHigh = Color(0xFFEF4444)
val StrainCritical = Color(0xFFEF4444)
val DriftBlue = Color(0xFF3B82F6)

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
