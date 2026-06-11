package com.neurobehavior.drift.ui.screens.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.components.GlassCard
import com.neurobehavior.drift.ui.components.PremiumFloatingBottomBar
import com.neurobehavior.drift.ui.theme.*

data class RecommendationItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val category: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(navController: NavController) {
    val items = listOf(
        RecommendationItem(Icons.Default.SelfImprovement, "Mindful Usage Break",
            "Take a 20-minute break from all screens. Practice deep breathing or a short walk outside.",
            "Immediate Relief", AISecondary),
        RecommendationItem(Icons.Default.Timer, "Pomodoro Technique",
            "Work in 25-minute focused blocks, then take a 5-minute phone-free break.",
            "Productivity", AIPrimary),
        RecommendationItem(Icons.Default.NightsStay, "Digital Sunset",
            "Set a strict no-phone rule after 9 PM. Night screen usage disrupts melatonin production.",
            "Sleep Hygiene", NeuralPurple),
        RecommendationItem(Icons.Default.Notifications, "Batch Notifications",
            "Check notifications only 3 times per day — morning, noon, and evening.",
            "Attention", DriftBlue),
        RecommendationItem(Icons.Default.FitnessCenter, "Physical Movement",
            "20 minutes of physical exercise increases BDNF, improving cognitive resilience.",
            "Wellness", AITertiary),
        RecommendationItem(Icons.Default.Apps, "App Minimalism",
            "Identify your top 3 time-draining apps and set a 30-minute daily limit on each.",
            "Digital Wellness", AISecondary),
        RecommendationItem(Icons.Default.Psychology, "Cognitive Load Awareness",
            "Rate your mental clarity on a 1-10 scale every 2 hours.",
            "Self-Awareness", AIPrimary),
        RecommendationItem(Icons.Default.BedtimeOff, "Screen-Free Morning",
            "Avoid your phone for the first 30 minutes after waking.",
            "Morning Routine", NeuralPurple),
        RecommendationItem(Icons.Default.Visibility, "20-20-20 Eye Rule",
            "Every 20 minutes, look at something 20 feet away for 20 seconds.",
            "Eye Health", DriftBlue)
    )

    Box(Modifier.fillMaxSize().background(DarkBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Wellness Strategies", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = { PremiumFloatingBottomBar(navController) }
        ) { pad ->
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("AI-generated strategies based on your neural drift and cognitive load patterns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                items(items) { RecoItemCard(it) }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun RecoItemCard(item: RecommendationItem) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, Modifier.size(28.dp), item.color)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Surface(shape = CircleShape, color = item.color.copy(alpha = 0.15f)) {
                        Text(
                            item.category.uppercase(),
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = item.color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f), lineHeight = 18.sp)
            }
        }
    }
}
