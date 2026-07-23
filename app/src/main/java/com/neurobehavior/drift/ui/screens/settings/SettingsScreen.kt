package com.neurobehavior.drift.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.navigation.NavRoutes
import com.neurobehavior.drift.ui.components.PremiumFloatingBottomBar
import com.neurobehavior.drift.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var showClear by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Subtle blue glow in the background
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .background(AIPrimary.copy(alpha = 0.08f), RoundedCornerShape(175.dp))
                .blur(90.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Settings",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.semantics { this.contentDescription = "Settings Header" }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { PremiumFloatingBottomBar(navController) }
        ) { pad ->
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- MODEL TELEMETRY SECTIONS (WEBSITE MATCH) ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Model Telemetry & Calibration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Deep telemetry regarding the underlying Random Forest Classifier.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Accuracy card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("OVERALL ACCURACY", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("94.2%", color = AIPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("RF Classifier", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Confusion Matrix card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CONFUSION MATRIX", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))

                            // Grid layout for confusion matrix
                            ConfusionMatrixGrid()
                        }
                    }
                }

                // Feature Importances progress bars matching website settings
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
                        Text("DECISION TREE IMPORTANCES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        FeatureImportanceRow("Typing Error Rate (Backspace %)", 0.285f, StrainHigh)
                        FeatureImportanceRow("Typing Speed (CPM)", 0.224f, AIPrimary)
                        FeatureImportanceRow("Active Session Duration (min)", 0.176f, AIPrimary)
                        FeatureImportanceRow("Break Frequency (breaks/hr)", 0.141f, StrainLow)
                        FeatureImportanceRow("Mouse Velocity (px/sec)", 0.088f, AIPrimary)
                        FeatureImportanceRow("Daily Screen Time (min)", 0.062f, Color.Gray)
                        FeatureImportanceRow("Click Frequency (clicks/min)", 0.024f, Color.Gray)
                    }
                }

                // --- DEVICE SETTINGS SECTIONS ---
                SectionLabel("Theme Settings")
                ToggleRow(
                    Icons.Default.DarkMode, "Dark Mode",
                    "Enable dark theme colors", isDarkMode, "Dark Mode Toggle Switch"
                ) { isDarkMode = it }

                SectionLabel("Notifications")
                ToggleRow(
                    Icons.Default.Notifications, "High Strain Alerts",
                    "Notify when cognitive strain exceeds threshold", s.strainAlertsEnabled, "Notification Toggle Switch"
                ) { vm.setStrainAlerts(it) }

                if (s.strainAlertsEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Alert threshold: ${(s.strainThreshold * 100).toInt()}%", color = Color.LightGray, fontSize = 12.sp)
                            Slider(
                                value = s.strainThreshold,
                                onValueChange = { vm.setThreshold(it) },
                                valueRange = 0.3f..0.9f,
                                steps = 5,
                                colors = SliderDefaults.colors(thumbColor = AIPrimary, activeTrackColor = AIPrimary)
                            )
                        }
                    }
                }

                ToggleRow(
                    Icons.Default.NightsStay, "Night Usage Alerts",
                    "Alert when using phone after 10 PM", s.nightAlertsEnabled
                ) { vm.setNightAlerts(it) }

                SectionLabel("Tracking")
                ToggleRow(
                    Icons.Default.Timeline, "Continuous Tracking",
                    "Collect behavioral signals throughout the day", s.trackingEnabled
                ) { vm.setTracking(it) }

                SectionLabel("Privacy")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StrainLow.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StrainLow.copy(alpha = 0.05f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, null, Modifier.size(18.dp), StrainLow)
                            Spacer(Modifier.width(8.dp))
                            Text("Privacy Guarantee", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "• No personal content collected\n• All AI runs on-device\n• No data sent to servers\n• Only anonymous usage patterns analyzed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                    }
                }

                ActionRow(
                    Icons.Default.Security, "Usage Access Permission", "Required for behavior tracking", "Manage",
                    contentDescription = "Request Permissions Button"
                ) {
                    ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }

                SectionLabel("Data Management")
                ActionRow(
                    Icons.Default.DeleteForever, "Clear All Data",
                    "Remove all history and reset baseline", "Clear", Color(0xFFEF4444),
                    contentDescription = "Clear Cache Button"
                ) { showClear = true }

                SectionLabel("Account Settings")
                ActionRow(
                    Icons.Default.Logout, "Logout",
                    "Sign out of your session", "Logout", Color(0xFFEF4444),
                    contentDescription = "Logout Button"
                ) {
                    vm.logout {
                        navController.navigate(NavRoutes.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        if (showClear) AlertDialog(
            onDismissRequest = { showClear = false },
            title = { Text("Clear All Data?", color = Color.White) },
            text = { Text("This permanently deletes all behavioral history and resets your baseline. Cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearAllData()
                        showClear = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClear = false }) { Text("Cancel", color = Color.LightGray) }
            },
            containerColor = Color(0xFF151C2C)
        )
    }
}

@Composable
fun ConfusionMatrixGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("", modifier = Modifier.width(20.dp), fontSize = 9.sp)
            Text("L", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.width(25.dp))
            Text("M", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.width(25.dp))
            Text("H", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.width(25.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("L", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
            MatrixValueCell("78", StrainLow, modifier = Modifier.width(25.dp))
            MatrixValueCell("2", Color.DarkGray, modifier = Modifier.width(25.dp))
            MatrixValueCell("0", Color.DarkGray, modifier = Modifier.width(25.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("M", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
            MatrixValueCell("1", Color.DarkGray, modifier = Modifier.width(25.dp))
            MatrixValueCell("75", StrainModerate, modifier = Modifier.width(25.dp))
            MatrixValueCell("4", Color.DarkGray, modifier = Modifier.width(25.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("H", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
            MatrixValueCell("0", Color.DarkGray, modifier = Modifier.width(25.dp))
            MatrixValueCell("3", Color.DarkGray, modifier = Modifier.width(25.dp))
            MatrixValueCell("77", StrainHigh, modifier = Modifier.width(25.dp))
        }
    }
}

@Composable
fun MatrixValueCell(value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(value, color = if (color == Color.DarkGray) Color.Gray else color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FeatureImportanceRow(label: String, valPercent: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray, fontSize = 10.sp)
            Text("${String.format("%.1f", valPercent * 100)}%", color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = valPercent,
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = AIPrimary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ToggleRow(icon: ImageVector, title: String, sub: String, checked: Boolean, contentDescription: String? = null, onChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp), AIPrimary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(sub, color = Color.Gray, fontSize = 11.sp)
            }
            val modifier = if (contentDescription != null) {
                Modifier.semantics { this.contentDescription = contentDescription }
            } else {
                Modifier
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                modifier = modifier,
                colors = SwitchDefaults.colors(checkedThumbColor = AIPrimary)
            )
        }
    }
}

@Composable
fun ActionRow(
    icon: ImageVector, title: String, sub: String, label: String,
    tintColor: Color = AIPrimary, contentDescription: String? = null, onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222F47), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp), tintColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(sub, color = Color.Gray, fontSize = 11.sp)
            }
            val modifier = if (contentDescription != null) {
                Modifier.semantics { this.contentDescription = contentDescription }
            } else {
                Modifier
            }
            TextButton(
                onClick = onAction,
                modifier = modifier,
                colors = ButtonDefaults.textButtonColors(contentColor = tintColor)
            ) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
