package com.neurobehavior.drift.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    // 1. Personal Information State
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Prefer not to say") }
    var occupation by remember { mutableStateOf("Student") }

    // 2. Academic / Work Information State
    var institution by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }

    // 3. Behavioral Baseline Information State
    var avgScreenTime by remember { mutableStateOf("") }
    var avgSleepHours by remember { mutableStateOf("") }
    var preferredWorkTime by remember { mutableStateOf("Morning") }
    var stressLevel by remember { mutableFloatStateOf(5f) }
    var privacyConsent by remember { mutableStateOf(true) }

    val state by viewModel.uiState.collectAsState()

    // Dropdown States
    var genderExpanded by remember { mutableStateOf(false) }
    var occupationExpanded by remember { mutableStateOf(false) }
    var preferredTimeExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Blur Glow Background
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-50).dp)
                .background(AIPrimary.copy(alpha = 0.1f), RoundedCornerShape(150.dp))
                .blur(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(20.dp))

            // Glass panel container matching website's max-w-4xl glass-panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC151C2C))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Branding Header
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
                        color = Color.White
                    )
                    Text(
                        text = "Cognitive Strain Detection Platform",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Error banner
                    if (state.registrationError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1AEF4444))
                                .border(1.dp, Color(0x33EF4444), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = state.registrationError ?: "",
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { this.contentDescription = "Registration Error" },
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // --- 1. PERSONAL INFORMATION SECTION ---
                    SectionHeader("Personal Information")

                    CustomInputField(value = fullName, onValueChange = { fullName = it }, label = "Full Name *", placeholder = "John Doe", description = "Full Name Input")
                    CustomInputField(value = username, onValueChange = { username = it }, label = "Username *", placeholder = "johndoe123", description = "Username Input")
                    CustomInputField(value = email, onValueChange = { email = it }, label = "Email Address *", placeholder = "john@example.com", keyboardType = KeyboardType.Email, description = "Email Input")
                    CustomInputField(value = age, onValueChange = { age = it }, label = "Age *", placeholder = "25", keyboardType = KeyboardType.Number, description = "Age Input")
                    CustomInputField(value = password, onValueChange = { password = it }, label = "Password *", placeholder = "Minimum 8 characters", isPassword = true, description = "Password Input")
                    CustomInputField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password *", placeholder = "Confirm password", isPassword = true)

                    // Gender Selector Dropdown
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text("Gender", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                        Box {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x800F172A))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(gender, color = Color.White, fontSize = 14.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.LightGray)
                                }
                            }
                            DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                                listOf("Male", "Female", "Non-binary", "Prefer not to say").forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = { gender = item; genderExpanded = false })
                                }
                            }
                        }
                    }

                    // Occupation Selector Dropdown
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text("Occupation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                        Box {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth().clickable { occupationExpanded = true }.semantics { this.contentDescription = "Occupation Input" },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x800F172A))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(occupation, color = Color.White, fontSize = 14.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.LightGray)
                                }
                            }
                            DropdownMenu(expanded = occupationExpanded, onDismissRequest = { occupationExpanded = false }) {
                                listOf("Student", "Professional", "Other").forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = { occupation = item; occupationExpanded = false })
                                }
                            }
                        }
                    }

                    // --- 2. ACADEMIC / WORK INFORMATION SECTION ---
                    SectionHeader("Academic / Work Information")

                    CustomInputField(value = institution, onValueChange = { institution = it }, label = "Institution / Company", placeholder = "University / Company Name")
                    CustomInputField(value = department, onValueChange = { department = it }, label = "Department / Field of Study", placeholder = "e.g. Computer Science")
                    CustomInputField(value = academicYear, onValueChange = { academicYear = it }, label = "Academic Year", placeholder = "e.g. Sophomore")
                    CustomInputField(value = workingHours, onValueChange = { workingHours = it }, label = "Working Hours Per Day", placeholder = "e.g. 8", keyboardType = KeyboardType.Number)

                    // --- 3. BEHAVIORAL BASELINE INFORMATION SECTION ---
                    SectionHeader("Behavioral Baseline Information")

                    CustomInputField(value = avgScreenTime, onValueChange = { avgScreenTime = it }, label = "Average Daily Screen Time (hours)", placeholder = "e.g. 6.5", keyboardType = KeyboardType.Number)
                    CustomInputField(value = avgSleepHours, onValueChange = { avgSleepHours = it }, label = "Average Sleep Hours", placeholder = "e.g. 7.5", keyboardType = KeyboardType.Number)

                    // Preferred Work/Study Time Dropdown
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text("Preferred Work/Study Time", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                        Box {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth().clickable { preferredTimeExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x800F172A))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preferredWorkTime, color = Color.White, fontSize = 14.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.LightGray)
                                }
                            }
                            DropdownMenu(expanded = preferredTimeExpanded, onDismissRequest = { preferredTimeExpanded = false }) {
                                listOf("Morning", "Afternoon", "Evening", "Night").forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = { preferredWorkTime = item; preferredTimeExpanded = false })
                                }
                            }
                        }
                    }

                    // Stress Level Slider (1-10)
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                        Text("Stress Level (1-10): ${stressLevel.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                        Slider(
                            value = stressLevel,
                            onValueChange = { stressLevel = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = AIPrimary,
                                activeTrackColor = AIPrimary,
                                inactiveTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    // Privacy Consent Checkbox
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1F0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = privacyConsent,
                                onCheckedChange = { privacyConsent = it ?: false },
                                colors = CheckboxDefaults.colors(checkedColor = AIPrimary),
                                modifier = Modifier.semantics { this.contentDescription = "Privacy Consent Checkbox" }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "I consent to the collection and processing of my behavioral data for the purpose of cognitive strain modeling. I understand that this data will be stored securely and will not be shared with third parties without explicit permission.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Submit Complete Registration Button
                    Button(
                        onClick = {
                            if (!privacyConsent) {
                                viewModel.register("", "", "", "", "", "") { } // Triggers missing consent VM error
                                return@Button
                            }
                            viewModel.register(
                                fullName = fullName,
                                email = email,
                                username = username,
                                password = password,
                                age = age,
                                occupation = occupation,
                                gender = gender,
                                institution = institution,
                                department = department,
                                academicYear = academicYear,
                                workingHours = workingHours,
                                avgScreenTime = avgScreenTime,
                                avgSleepHours = avgSleepHours,
                                preferredWorkTime = preferredWorkTime,
                                stressLevel = stressLevel.toInt()
                            ) {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .semantics { this.contentDescription = "Submit Register" },
                        colors = ButtonDefaults.buttonColors(containerColor = AIPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Complete Registration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Divider(color = Color(0xFF1E293B))
                    Spacer(Modifier.height(16.dp))

                    // Navigate back to Login
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?", fontSize = 13.sp, color = Color.Gray)
                        TextButton(
                            onClick = {
                                viewModel.clearErrors()
                                navController.popBackStack()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.semantics { this.contentDescription = "Navigate to Login" }
                        ) {
                            Text("Log in", color = AIPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Divider(color = Color(0xFF1E293B), thickness = 1.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    description: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .let { if (description != null) it.semantics { this.contentDescription = description } else it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0x800F172A),
                unfocusedContainerColor = Color(0x800F172A),
                focusedBorderColor = AIPrimary,
                unfocusedBorderColor = Color(0x33FFFFFF)
            )
        )
    }
}
