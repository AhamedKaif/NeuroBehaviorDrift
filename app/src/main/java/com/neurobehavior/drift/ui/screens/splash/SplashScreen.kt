package com.neurobehavior.drift.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        alpha.animateTo(1f, tween(600))
        delay(2200)
        onNavigateToDashboard()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0F0F1A), Color(0xFF1A1A3E)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale.value)) {
            Icon(Icons.Default.Psychology, null, Modifier.size(96.dp).scale(pulseScale), Color(0xFF7C4DFF))
            Spacer(Modifier.height(24.dp))
            Text("NeuroBehavior", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha.value))
            Text("Drift Monitor", color = Color(0xFF9E9EE8), fontSize = 16.sp,
                modifier = Modifier.alpha(alpha.value))
            Spacer(Modifier.height(8.dp))
            Text("Privacy-First · On-Device AI", color = Color(0xFF666699), fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.alpha(alpha.value))
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(Modifier.size(28.dp).alpha(alpha.value), Color(0xFF7C4DFF), 2.dp)
        }
    }
}
