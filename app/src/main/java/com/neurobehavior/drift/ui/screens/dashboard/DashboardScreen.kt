package com.neurobehavior.drift.ui.screens.dashboard

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.navigation.NavRoutes
import com.neurobehavior.drift.ui.theme.*
import com.neurobehavior.drift.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Gradient Glow behind content
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .background(AIPrimary.copy(alpha = 0.12f), RoundedCornerShape(200.dp))
                .blur(100.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PremiumTopBar(
                    title = "NEURO-DRIFT",
                    subtitle = "Cognitive Strain Monitor",
                    onRefresh = { viewModel.refresh() },
                    onSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onOpenDrawer = { navController.navigate(NavRoutes.Profile.route) }
                )
            },
            bottomBar = { PremiumFloatingBottomBar(navController) }
        ) { pad ->
            Box(Modifier.padding(pad)) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AIPrimary)
                    }
                    !state.permissionGranted -> PermissionView {
                        ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                    else -> DashboardContent(state = state, navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRetraining by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Dashboard Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Operational Live State",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AIPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cognitive Diagnostics Workspace",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Subject ID: ${state.username} | Monitoring cognitive loads.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    isRetraining = true
                    viewModel.retrainModel(
                        onSuccess = {
                            isRetraining = false
                            Toast.makeText(ctx, "Model successfully retrained with user baseline!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { err ->
                            isRetraining = false
                            Toast.makeText(ctx, "Retraining failed: $err", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = !isRetraining,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF222F47)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp).let { if (isRetraining) it else it })
                Spacer(Modifier.width(8.dp))
                Text(if (isRetraining) "Retraining..." else "Retrain Model", color = Color.LightGray, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    viewModel.logout {
                        navController.navigate(NavRoutes.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0x66EF4444)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                Spacer(Modifier.width(8.dp))
                Text("Disconnect", color = Color(0xFFEF4444), fontSize = 13.sp)
            }
        }

        // 2. Metrics Widgets Grid (4 Items)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Widget 1: Cognitive Strain Badge
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Cognitive Strain",
                    valueElement = {
                        val strainColor = when (state.strainLabel) {
                            "Low" -> StrainLow
                            "Medium", "Moderate" -> StrainModerate
                            else -> StrainHigh
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(strainColor.copy(alpha = 0.1f))
                                .border(1.dp, strainColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(6.dp).background(strainColor, CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(state.strainLabel, color = strainColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    },
                    subtitle = "Confidence: ${Math.round(state.strainProbability * 100)}%"
                )

                // Widget 2: Behavioral Drift Score
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Behavioral Drift",
                    valueElement = {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${Math.round(state.driftScore * 100)}%", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(6.dp))
                            val driftLabel = if (state.driftScore < 0.3f) "Normal" else if (state.driftScore < 0.65f) "Warning" else "Critical"
                            val driftColor = if (state.driftScore < 0.3f) StrainLow else if (state.driftScore < 0.65f) StrainModerate else StrainHigh
                            Text(driftLabel, color = driftColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    progressVal = state.driftScore
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Widget 3: Daily Screen Time
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Daily Screen Time",
                    valueElement = {
                        Row(verticalAlignment = Alignment.Bottom) {
                            val totalMins = state.screenTimeHours * 60 + state.screenTimeMinutes
                            Text("$totalMins", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("min", color = Color.Gray, fontSize = 13.sp)
                        }
                    },
                    subtitle = "Target: < 480 min/day"
                )

                // Widget 4: Active Session
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Session",
                    valueElement = {
                        Row(verticalAlignment = Alignment.Bottom) {
                            val activeMins = if (state.currentMetrics.containsKey("session_duration")) state.currentMetrics["session_duration"]?.toInt() ?: 15 else 15
                            Text("$activeMins", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("min", color = Color.Gray, fontSize = 13.sp)
                        }
                    },
                    subtitle = "Break suggested in 75 min"
                )
            }
        }

        // 3. Interactive Behavioral Sandbox Panel
        InteractiveSandboxPanel(state = state, viewModel = viewModel)

        // 4. System Alerts & Recommendations
        RecommendationsWidget(alerts = state.alerts, recommendations = state.recommendations, onClear = { viewModel.clearAlerts { } })

        // Spacer at the bottom to allow scrolling over floating bar
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun DashboardMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    valueElement: @Composable () -> Unit,
    subtitle: String? = null,
    progressVal: Float? = null
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            valueElement()
            if (progressVal != null) {
                val progressColor = if (progressVal < 0.3f) StrainLow else if (progressVal < 0.65f) StrainModerate else StrainHigh
                LinearProgressIndicator(
                    progress = progressVal.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = progressColor,
                    trackColor = Color(0xFF1E293B)
                )
            } else if (subtitle != null) {
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun InteractiveSandboxPanel(state: DashboardUiState, viewModel: DashboardViewModel) {
    var isTracking by rememberSaveable { mutableStateOf(true) }
    var sandboxText by rememberSaveable { mutableStateOf("") }
    var typedChars by rememberSaveable { mutableStateOf(0) }
    var backspaceCount by rememberSaveable { mutableStateOf(0) }
    var clickCount by rememberSaveable { mutableStateOf(0) }
    var elapsedSeconds by rememberSaveable { mutableStateOf(0) }
    var isFatigueSimulated by rememberSaveable { mutableStateOf(false) }
    var transmitSuccess by rememberSaveable { mutableStateOf(false) }
    var isIngesting by rememberSaveable { mutableStateOf(false) }

    // Live Metrics Computed
    val liveCpm = if (elapsedSeconds > 2) Math.min(450, Math.max(50, Math.round((typedChars / (elapsedSeconds / 60.0))))) else 250
    val liveErrorRate = if (typedChars + backspaceCount > 0) backspaceCount.toFloat() / (typedChars + backspaceCount) else 0.02f
    val liveMouseSpeed = if (isFatigueSimulated) 85 else 400
    val liveClicks = if (elapsedSeconds > 2) Math.min(120, Math.round((clickCount / (elapsedSeconds / 60.0)))) else 20

    // Timing effect
    LaunchedEffect(isTracking) {
        while (isTracking) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AIPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, null, tint = AIPrimary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Behavioral Sandbox", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { isTracking = !isTracking },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracking) AIPrimary.copy(alpha = 0.15f) else Color(0xFF0B0F19)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isTracking) AIPrimary.copy(alpha = 0.3f) else Color(0xFF222F47)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (isTracking) "Tracking Live" else "Paused", color = if (isTracking) AIPrimary else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            typedChars = 0
                            backspaceCount = 0
                            clickCount = 0
                            elapsedSeconds = 0
                            sandboxText = ""
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .border(1.dp, Color(0xFF222F47), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Text(
                "Type inside this sandbox, click, and trigger inputs to calculate keystroke speeds (CPM) and delete rates in real-time.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Textarea input
            OutlinedTextField(
                value = sandboxText,
                onValueChange = { newVal ->
                    if (isTracking) {
                        if (newVal.length < sandboxText.length) {
                            backspaceCount += (sandboxText.length - newVal.length)
                        } else {
                            typedChars += (newVal.length - sandboxText.length)
                        }
                        sandboxText = newVal
                    }
                },
                placeholder = { Text("Start typing text here... The system will record inputs dynamically.", color = Color.DarkGray, fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clickable { if (isTracking) clickCount += 1 },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0B0F19),
                    unfocusedContainerColor = Color(0xFF0B0F19),
                    focusedBorderColor = AIPrimary,
                    unfocusedBorderColor = Color(0xFF222F47)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Statistics counters HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chars: $typedChars", color = Color.LightGray, fontSize = 10.sp)
                Text("Del: $backspaceCount", color = Color.LightGray, fontSize = 10.sp)
                Text("Clicks: $clickCount", color = Color.LightGray, fontSize = 10.sp)
                Text("Sec: ${elapsedSeconds}s", color = Color.LightGray, fontSize = 10.sp)
            }

            Divider(color = Color(0xFF1E293B))

            // Simulated HUD Gauges grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0F19), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Typing CPM", color = Color.Gray, fontSize = 9.sp)
                    Text("$liveCpm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error Rate", color = Color.Gray, fontSize = 9.sp)
                    Text("${String.format("%.1f", liveErrorRate * 100)}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mouse Vel", color = Color.Gray, fontSize = 9.sp)
                    Text("$liveMouseSpeed px/s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Clicks Rate", color = Color.Gray, fontSize = 9.sp)
                    Text("$liveClicks /m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Fatigue Simulation Switch Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF222F47), RoundedCornerShape(10.dp))
                    .clickable { isFatigueSimulated = !isFatigueSimulated }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = if (isFatigueSimulated) StrainHigh else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Force Cognitive Fatigue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Text("Simulates degraded behavioral baseline outputs.", color = Color.Gray, fontSize = 9.sp)
                }

                Switch(
                    checked = isFatigueSimulated,
                    onCheckedChange = { isFatigueSimulated = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = AIPrimary)
                )
            }

            // Transmit button
            Button(
                onClick = {
                    isIngesting = true
                    val payload = if (isFatigueSimulated) {
                        JSONObject().apply {
                            put("screen_time", 480.0)
                            put("typing_speed", 90.0)
                            put("typing_error_rate", 0.22)
                            put("session_duration", 150.0)
                            put("click_frequency", 7.0)
                            put("break_frequency", 0.2)
                            put("mouse_speed", 85.0)
                        }
                    } else {
                        JSONObject().apply {
                            put("screen_time", (state.screenTimeHours * 60 + state.screenTimeMinutes).toDouble())
                            put("typing_speed", liveCpm.toDouble())
                            put("typing_error_rate", liveErrorRate.toDouble())
                            put("session_duration", 15.0)
                            put("click_frequency", liveClicks.toDouble())
                            put("break_frequency", 2.0)
                            put("mouse_speed", liveMouseSpeed.toDouble())
                        }
                    }
                    viewModel.transmitMetrics(payload) {
                        isIngesting = false
                        transmitSuccess = true
                        typedChars = 0
                        backspaceCount = 0
                        clickCount = 0
                        elapsedSeconds = 0
                        sandboxText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (transmitSuccess) StrainLow else AIPrimary
                )
            ) {
                if (isIngesting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else if (transmitSuccess) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ingested Successfully!", fontWeight = FontWeight.Bold)
                    }
                    LaunchedEffect(Unit) {
                        delay(2000)
                        transmitSuccess = false
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Transmit Metrics to Model", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationsWidget(alerts: List<AlertItem>, recommendations: List<String>, onClear: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = StrainHigh, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recommendations & Alerts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (alerts.isNotEmpty()) {
                    Text(
                        "Clear All",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onClear() }
                    )
                }
            }

            if (alerts.isEmpty() && recommendations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = StrainLow.copy(alpha = 0.3f), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Behavioral Baseline Stable", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("No load deviations found.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    alerts.forEach { alert ->
                        val alertColor = when (alert.severity) {
                            "HIGH" -> StrainHigh
                            "MEDIUM" -> StrainModerate
                            else -> DriftBlue
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B0F19), RoundedCornerShape(10.dp))
                                .border(1.dp, alertColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    alert.title,
                                    color = alertColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text(alert.createdAt.takeLast(8), color = Color.Gray, fontSize = 8.sp)
                            }
                            Text(alert.message, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    recommendations.forEach { reco ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1A3B82F6), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = AIPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(reco, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionView(onGrant: () -> Unit) {
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
