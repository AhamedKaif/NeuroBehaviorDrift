package com.neurobehavior.drift.ui.screens.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.navigation.NavRoutes
import com.neurobehavior.drift.ui.theme.*
import com.neurobehavior.drift.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Background Gradient Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AIPrimary.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width
                ),
                radius = size.width,
                center = Offset(size.width, 0f)
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PremiumTopBar(
                    title = "Neuro",
                    subtitle = "Cognitive Wellness AI",
                    onRefresh = { viewModel.refresh() },
                    onSettings = { navController.navigate(NavRoutes.Settings.route) }
                )
            },
            bottomBar = { PremiumFloatingBottomBar(navController) }
        ) { pad ->
            Box(Modifier.padding(pad)) {
                when {
                    state.isLoading -> LoadingView()
                    !state.permissionGranted -> PermissionView {
                        ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                    else -> DashboardContent(state = state)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(state: DashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        MainScoreCard(state.cognitiveStrainScore, state.strainLevel, state.driftScore)
        
        Text(
            "Behavioral Signals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                Modifier.weight(1f), Icons.Default.PhoneAndroid, "Screen",
                "${state.screenTimeHours}h ${state.screenTimeMinutes}m", AISecondary
            )
            MetricCard(
                Modifier.weight(1f), Icons.Default.SwapHoriz, "Switches",
                "${state.appSwitches}", AIPrimary
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                Modifier.weight(1f), Icons.Default.LockOpen, "Unlocks",
                "${state.unlockCount}", NeuralPurple
            )
            MetricCard(
                Modifier.weight(1f), Icons.Default.NightsStay, "Night Use",
                "${state.nightUsageMinutes}m", DriftBlue
            )
        }

        BaselineCard(state.hasBaseline, state.daysOfData)
        
        if (state.topApps.isNotEmpty()) TopAppsCard(state.topApps)
        if (state.recommendations.isNotEmpty()) RecoCard(state.recommendations)
        
        Spacer(Modifier.height(100.dp)) // Padding for floating nav
    }
}

@Composable
fun MainScoreCard(strain: Float, level: StrainLevel, drift: Float) {
    val animStrain by animateFloatAsState(
        targetValue = strain,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    GlassCard(gradient = PremiumGradient) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Cognitive Strain",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(24.dp))
            
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                // Glow effect behind the ring
                Canvas(Modifier.size(220.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        radius = size.width / 2
                    )
                }
                
                Canvas(Modifier.size(180.dp)) {
                    val sw = 16.dp.toPx()
                    drawArc(
                        color = Color.White.copy(alpha = 0.2f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(sw, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = 140f,
                        sweepAngle = 260f * animStrain,
                        useCenter = false,
                        style = Stroke(sw, cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(animStrain * 100).toInt()}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        level.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreMiniDetail("Behavioral Drift", "${(drift * 100).toInt()}%")
                VerticalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(30.dp))
                ScoreMiniDetail("Confidence", "High")
            }
        }
    }
}

@Composable
fun ScoreMiniDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun MetricCard(modifier: Modifier, icon: ImageVector, label: String, value: String, color: Color) {
    GlassCard(modifier = modifier) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(24.dp), color)
        }
        Spacer(Modifier.height(12.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun BaselineCard(has: Boolean, days: Int) {
    GlassCard(gradient = if (has) PremiumGradient else CardGradient) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (has) Color.White.copy(alpha = 0.2f) else AIPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (has) Icons.Default.Verified else Icons.Default.Analytics,
                    null,
                    tint = if (has) Color.White else AIPrimary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (has) "Baseline Calibrated" else "Learning Patterns",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    if (has) "AI is monitoring for drifts" else "$days/7 days analyzed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TopAppsCard(apps: List<com.neurobehavior.drift.data.model.AppUsageData>) {
    GlassCard {
        Text("Top Cognitive Drivers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        val total = apps.sumOf { it.usageMinutes }.coerceAtLeast(1)
        apps.take(4).forEach { app ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(app.appName, Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1)
                LinearProgressIndicator(
                    progress = { (app.usageMinutes.toFloat() / total).coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                    color = AISecondary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(Modifier.width(8.dp))
                Text("${app.usageMinutes}m", style = MaterialTheme.typography.labelSmall, color = AISecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecoCard(recs: List<String>) {
    GlassCard(gradient = AnomalyGradient) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("AI Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Spacer(Modifier.height(12.dp))
        recs.forEach { rec ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("•", color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.width(8.dp))
                Text(rec, style = MaterialTheme.typography.bodySmall, color = Color.White, lineHeight = 18.sp)
            }
        }
    }
}

@Composable fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AIPrimary, strokeWidth = 4.dp)
            Spacer(Modifier.height(20.dp))
            Text("Decoding Neural Patterns...", style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

@Composable fun PermissionView(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(100.dp).clip(CircleShape).background(AIPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, null, Modifier.size(48.dp), AIPrimary)
        }
        Spacer(Modifier.height(32.dp))
        Text("Usage Permission Required", style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Neuro needs access to usage statistics to build your cognitive baseline. Data is processed entirely on-device.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = AIPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Grant Access", fontWeight = FontWeight.Bold)
        }
    }
}
