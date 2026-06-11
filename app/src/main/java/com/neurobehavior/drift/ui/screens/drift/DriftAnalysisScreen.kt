package com.neurobehavior.drift.ui.screens.drift

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.neurobehavior.drift.ui.components.GlassCard
import com.neurobehavior.drift.ui.components.PremiumFloatingBottomBar
import com.neurobehavior.drift.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriftAnalysisScreen(navController: NavController, vm: DriftAnalysisViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                delay(1000)
                vm.startListening()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Drift Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        VoiceButton(
                            isListening = s.isListening,
                            onClick = {
                                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    if (s.isListening) vm.stopListening() else vm.startListening()
                                } else {
                                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = { PremiumFloatingBottomBar(navController) }
        ) { pad ->
            Column(Modifier.padding(pad).fillMaxSize()) {
                VoiceOverlay(s.voiceResult, s.voiceError)

                when {
                    s.isLoading -> LoadingView()
                    !s.hasData  -> NoDataView()
                    else -> Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DriftHeroCard(s.overallDrift, s.anomalyScore, s.trend)
                        
                        Text("Signal Deviations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        
                        s.featureDrifts.forEach { FeatureDriftRow(it) }
                        
                        AnomalyAlertCard(s.anomalyScore, s.isAnomaly)
                        
                        ComparisonTable(s.baselineValues, s.todayValues)
                        
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isListening) StrainHigh.copy(alpha = 0.2f) else GlassColor)
            .border(1.dp, if (isListening) StrainHigh else GlassBorder, CircleShape)
    ) {
        Icon(
            if (isListening) Icons.Default.MicNone else Icons.Default.Mic,
            null,
            tint = if (isListening) StrainHigh else Color.White,
            modifier = Modifier.size(24.dp).graphicsLayer(scaleX = scale, scaleY = scale)
        )
    }
}

@Composable
fun VoiceOverlay(result: String?, error: String?) {
    AnimatedVisibility(
        visible = result != null || error != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (error != null) StrainHigh.copy(alpha = 0.15f) else AIPrimary.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, if (error != null) StrainHigh.copy(alpha = 0.3f) else AIPrimary.copy(alpha = 0.3f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (error != null) Icons.Default.Warning else Icons.Default.AutoAwesome,
                    null, Modifier.size(20.dp),
                    if (error != null) StrainHigh else AIPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    error ?: "AI identified: \"$result\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DriftHeroCard(drift: Float, anomaly: Float, trend: String) {
    GlassCard(gradient = PremiumGradient) {
        Column {
            Text("Behavioral Drift Index", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("${(drift * 100).toInt()}%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = Color.White)
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (trend == "increasing") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null, Modifier.size(20.dp), Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(trend.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("Trend over 72h", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { drift.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun FeatureDriftRow(f: FeatureDrift) {
    val icon = when (f.iconName) {
        "screen" -> Icons.Default.PhoneAndroid
        "switch" -> Icons.Default.SwapHoriz
        "unlock" -> Icons.Default.LockOpen
        else     -> Icons.Default.NightsStay
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(AIPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(20.dp), AIPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(f.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${f.baselineValue} (Base) → ${f.todayValue} (Now)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }
            Text("+${f.driftPct}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (f.driftPct > 30) StrainHigh else AISecondary)
        }
    }
}

@Composable
fun AnomalyAlertCard(score: Float, isAnomaly: Boolean) {
    GlassCard(gradient = if (isAnomaly) AnomalyGradient else CardGradient) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isAnomaly) Icons.Default.Error else Icons.Default.CheckCircle,
                null, Modifier.size(40.dp), Color.White
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (isAnomaly) "Anomalous Pattern Detected" else "Behavior is Stable",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    if (isAnomaly) "Deviation score of ${(score*100).toInt()}% indicates significant change."
                    else "AI analysis confirms patterns match your baseline.",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ComparisonTable(baseline: List<String>, today: List<String>) {
    val labels = listOf("Screen Time", "App Switches", "Unlocks", "Night Usage")
    GlassCard {
        Text("Metric Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        labels.forEachIndexed { i, label ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                Row {
                    Text(baseline.getOrElse(i){"0"}, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), Color.White.copy(alpha = 0.3f))
                    Spacer(Modifier.width(12.dp))
                    Text(today.getOrElse(i){"0"}, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AISecondary)
                }
            }
            if (i < labels.size - 1) HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}

@Composable
fun NoDataView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CloudQueue, null, Modifier.size(80.dp), Color.White.copy(alpha = 0.2f))
            Spacer(Modifier.height(24.dp))
            Text("Insufficient Data", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Collect 24h more of behavior signals.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AIPrimary)
    }
}
