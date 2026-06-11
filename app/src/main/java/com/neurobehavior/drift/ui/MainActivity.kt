package com.neurobehavior.drift.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.neurobehavior.drift.ui.navigation.NavGraph
import com.neurobehavior.drift.ui.theme.NeuroBehaviorDriftTheme
import com.neurobehavior.drift.workers.BehaviorAnalysisWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BehaviorAnalysisWorker.schedule(this)
        setContent {
            NeuroBehaviorDriftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(navController = rememberNavController())
                }
            }
        }
    }
}
