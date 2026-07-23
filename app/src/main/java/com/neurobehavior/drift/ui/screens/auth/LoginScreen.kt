package com.neurobehavior.drift.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.navigation.NavRoutes
import com.neurobehavior.drift.ui.theme.DarkBackground
import com.neurobehavior.drift.ui.theme.AIPrimary
import com.neurobehavior.drift.ui.theme.GlassBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Background Gradient Glow matching website's absolute blur element
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .background(AIPrimary.copy(alpha = 0.15f), RoundedCornerShape(150.dp))
                .blur(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphism Card matching .glass-panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC151C2C)) // rgba(21, 28, 44, 0.8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Branding Header
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AIPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧠", fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Neuro-Behavioral Drift",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Cognitive Strain Detection Platform",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    // 2. Error Display Banner
                    if (state.errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1AEF4444)) // red-500/10
                                .border(1.dp, Color(0x33EF4444), RoundedCornerShape(12.dp)) // red-500/20
                                .padding(12.dp)
                        ) {
                            Text(
                                text = state.errorMessage ?: "",
                                color = Color(0xFFEF4444), // red-400
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { this.contentDescription = "Error Message" },
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 3. Form Input Username
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text(
                            text = "USERNAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = { Text("Enter username", color = Color.Gray, fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { this.contentDescription = "Username Input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x800F172A), // slate-900/50
                                unfocusedContainerColor = Color(0x800F172A),
                                focusedBorderColor = AIPrimary,
                                unfocusedBorderColor = Color(0x33FFFFFF) // slate-700/60 fallback
                            )
                        )
                    }

                    // 4. Form Input Password
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                        Text(
                            text = "PASSWORD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Enter password", color = Color.Gray, fontSize = 14.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { this.contentDescription = "Password Input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x800F172A), // slate-900/50
                                unfocusedContainerColor = Color(0x800F172A),
                                focusedBorderColor = AIPrimary,
                                unfocusedBorderColor = Color(0x33FFFFFF)
                            )
                        )
                    }

                    // 5. Submit Sign In Button
                    Button(
                        onClick = {
                            viewModel.login(username, password) {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .semantics { this.contentDescription = "Login Button" },
                        colors = ButtonDefaults.buttonColors(containerColor = AIPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // 6. Navigation Link Toggles
                    Spacer(Modifier.height(24.dp))
                    Divider(color = Color(0xFF1E293B)) // border-slate-800
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account?", fontSize = 13.sp, color = Color.Gray)
                        TextButton(
                            onClick = {
                                viewModel.clearErrors()
                                navController.navigate(NavRoutes.Register.route)
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.semantics { this.contentDescription = "Navigate to Register" }
                        ) {
                            Text("Register now", color = AIPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.clearErrors()
                        },
                        modifier = Modifier.semantics { this.contentDescription = "Forgot Password Link" }
                    ) {
                        Text("Forgot Password?", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
