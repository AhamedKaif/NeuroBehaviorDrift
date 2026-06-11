package com.neurobehavior.drift.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.theme.NeuralPurple
import com.neurobehavior.drift.ui.theme.StrainLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var showClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.Default.ArrowBack,"Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            SectionLabel("Notifications")
            ToggleRow(Icons.Default.Notifications, "High Strain Alerts",
                "Notify when cognitive strain exceeds threshold", s.strainAlertsEnabled) { vm.setStrainAlerts(it) }
            if (s.strainAlertsEnabled) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Alert threshold: ${(s.strainThreshold*100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall)
                        Slider(s.strainThreshold, { vm.setThreshold(it) }, valueRange = 0.3f..0.9f, steps = 5)
                    }
                }
            }
            ToggleRow(Icons.Default.NightsStay, "Night Usage Alerts",
                "Alert when using phone after 10 PM", s.nightAlertsEnabled) { vm.setNightAlerts(it) }

            SectionLabel("Tracking")
            ToggleRow(Icons.Default.Timeline, "Continuous Tracking",
                "Collect behavioral signals throughout the day", s.trackingEnabled) { vm.setTracking(it) }

            SectionLabel("Privacy")
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(StrainLow.copy(.08f))) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, Modifier.size(20.dp), StrainLow)
                        Spacer(Modifier.width(8.dp))
                        Text("Privacy Guarantee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("• No personal content collected\n• All AI runs on-device\n• No data sent to servers\n• Only anonymous usage patterns analyzed",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(.7f))
                }
            }
            ActionRow(Icons.Default.Security, "Usage Access Permission", "Required for behavior tracking", "Manage") {
                ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }

            SectionLabel("Data Management")
            ActionRow(Icons.Default.DeleteForever, "Clear All Data",
                "Remove all history and reset baseline", "Clear", Color(0xFFF44336)) { showClear = true }

            SectionLabel("About")
            InfoRow(Icons.Default.Info, "Version", "1.0.0")
            InfoRow(Icons.Default.School, "Project", "Final Year IT Project")
            InfoRow(Icons.Default.Psychology, "AI Models", "Random Forest + Isolation Forest → TFLite")
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showClear) AlertDialog(
        onDismissRequest = { showClear = false },
        title = { Text("Clear All Data?") },
        text  = { Text("This permanently deletes all behavioral history and resets your baseline. Cannot be undone.") },
        confirmButton = { TextButton({ vm.clearAllData(); showClear = false },
            colors = ButtonDefaults.textButtonColors(Color(0xFFF44336))) { Text("Clear") } },
        dismissButton = { TextButton({ showClear = false }) { Text("Cancel") } }
    )
}

@Composable fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = NeuralPurple,
        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
}

@Composable fun ToggleRow(icon: ImageVector, title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(22.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(sub, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.6f))
            }
            Switch(checked, onChange)
        }
    }
}

@Composable fun InfoRow(icon: ImageVector, title: String, value: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(22.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(.6f))
            }
        }
    }
}

@Composable fun ActionRow(icon: ImageVector, title: String, sub: String, label: String,
                          tintColor: Color = MaterialTheme.colorScheme.primary, onAction: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(22.dp), tintColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(.6f))
            }
            TextButton(onAction, colors = ButtonDefaults.textButtonColors(tintColor)) { Text(label) }
        }
    }
}
