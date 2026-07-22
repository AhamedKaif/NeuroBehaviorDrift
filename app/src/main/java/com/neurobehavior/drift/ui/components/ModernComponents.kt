package com.neurobehavior.drift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.neurobehavior.drift.ui.navigation.NavRoutes
import com.neurobehavior.drift.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    gradient: Brush = CardGradient,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.background(gradient)) {
            Column(
                modifier = Modifier.padding(20.dp),
                content = content
            )
        }
    }
}

@Composable
fun PremiumFloatingBottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(36.dp),
        color = DarkSurface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, GlassBorder),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Dashboard,
                isSelected = currentRoute == NavRoutes.Dashboard.route,
                contentDescription = "Bottom Nav Home",
                onClick = { navController.navigate(NavRoutes.Dashboard.route) }
            )
            BottomNavItem(
                icon = Icons.Default.BarChart,
                isSelected = currentRoute == NavRoutes.Analytics.route,
                contentDescription = "Bottom Nav Analytics",
                onClick = { navController.navigate(NavRoutes.Analytics.route) }
            )
            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.ShowChart,
                isSelected = currentRoute == NavRoutes.DriftAnalysis.route,
                contentDescription = "Bottom Nav Drift",
                onClick = { navController.navigate(NavRoutes.DriftAnalysis.route) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                isSelected = currentRoute == NavRoutes.Settings.route,
                contentDescription = "Bottom Nav Settings",
                onClick = { navController.navigate(NavRoutes.Settings.route) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    val background = if (isSelected) PremiumGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    val contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun PremiumTopBar(
    title: String,
    subtitle: String,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .semantics { this.contentDescription = "Open Navigation Drawer" }
                    .clip(CircleShape)
                    .background(GlassColor)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer", tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.semantics { this.contentDescription = "Dashboard Header" }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        Row {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassColor)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassColor)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
    }
}
