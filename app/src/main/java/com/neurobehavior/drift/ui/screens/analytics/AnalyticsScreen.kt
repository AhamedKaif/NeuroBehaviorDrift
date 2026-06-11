package com.neurobehavior.drift.ui.screens.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.components.GlassCard
import com.neurobehavior.drift.ui.components.PremiumFloatingBottomBar
import com.neurobehavior.drift.ui.theme.*
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavController, vm: AnalyticsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Performance Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Tab Selection
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("7D", "14D", "30D").forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = tab == index,
                            onClick = { 
                                tab = index
                                vm.load(when(index) { 0 -> 7; 1 -> 14; else -> 30 })
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = AIPrimary,
                                activeContentColor = Color.White,
                                inactiveContainerColor = DarkSurface,
                                inactiveContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(label)
                        }
                    }
                }

                if (s.isLoading) {
                    Box(Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AIPrimary)
                    }
                } else {
                    AnalyticsChartCard("Cognitive Strain Trend", s.strainData.map { it * 100f })
                    AnalyticsChartCard("Daily Screen Time (Hrs)", s.screenData, isColumn = true)
                    AnalyticsChartCard("Behavioral Drift", s.driftData.map { it * 100f })

                    SummarySection(s)
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AnalyticsChartCard(title: String, data: List<Float>, isColumn: Boolean = false) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            modelProducer.runTransaction {
                if (isColumn) columnSeries { series(data) }
                else lineSeries { series(data) }
            }
        }
    }

    GlassCard {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        
        CartesianChartHost(
            chart = rememberCartesianChart(
                layers = if (isColumn) {
                    arrayOf(rememberColumnCartesianLayer())
                } else {
                    arrayOf(rememberLineCartesianLayer())
                }
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
    }
}

@Composable
fun SummarySection(s: AnalyticsUiState) {
    GlassCard(gradient = PremiumGradient) {
        Text("Period Insight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryItem("Avg Strain", "${(s.avgStrain * 100).toInt()}%")
            SummaryItem("Avg Screen", "${s.avgScreenH}h")
            SummaryItem("Switches", "${s.avgSwitches}")
        }
        
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.2f))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Peak Load Day", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                Text(s.peakDay, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Optimal Day", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                Text(s.bestDay, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = AISecondary)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}
