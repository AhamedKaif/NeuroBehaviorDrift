package com.neurobehavior.drift.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.navigation.NavRoutes
import com.neurobehavior.drift.ui.components.PremiumFloatingBottomBar
import com.neurobehavior.drift.ui.theme.DarkBackground
import com.neurobehavior.drift.ui.theme.AIPrimary
import com.neurobehavior.drift.ui.theme.StrainLow
import com.neurobehavior.drift.ui.theme.StrainHigh
import com.neurobehavior.drift.ui.theme.StrainModerate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    // Editing local state variables
    var fullNameEdit by remember { mutableStateOf("") }
    var ageEdit by remember { mutableStateOf("") }
    var occupationEdit by remember { mutableStateOf("") }
    var genderEdit by remember { mutableStateOf("Prefer not to say") }
    var institutionEdit by remember { mutableStateOf("") }
    var departmentEdit by remember { mutableStateOf("") }
    var academicYearEdit by remember { mutableStateOf("") }
    var workingHoursEdit by remember { mutableStateOf("") }
    var avgScreenTimeEdit by remember { mutableStateOf("") }
    var avgSleepHoursEdit by remember { mutableStateOf("") }
    var preferredWorkTimeEdit by remember { mutableStateOf("Morning") }
    var stressLevelEdit by remember { mutableFloatStateOf(5f) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.isEditing) {
        if (state.isEditing) {
            fullNameEdit = state.fullName
            ageEdit = state.age
            occupationEdit = state.occupation
            genderEdit = state.gender
            institutionEdit = state.institution
            departmentEdit = state.department
            academicYearEdit = state.academicYear
            workingHoursEdit = state.workingHours
            avgScreenTimeEdit = state.avgScreenTime
            avgSleepHoursEdit = state.avgSleepHours
            preferredWorkTimeEdit = state.preferredWorkTime
            stressLevelEdit = state.stressLevel.toFloat()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(AIPrimary.copy(alpha = 0.08f), RoundedCornerShape(175.dp))
                .blur(90.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Profile Management",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.semantics { this.contentDescription = "Profile Header" }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (state.isEditing) {
                            IconButton(
                                onClick = {
                                    viewModel.updateAllFields(
                                        fullName = fullNameEdit,
                                        age = ageEdit,
                                        occupation = occupationEdit,
                                        gender = genderEdit,
                                        institution = institutionEdit,
                                        department = departmentEdit,
                                        academicYear = academicYearEdit,
                                        workingHours = workingHoursEdit,
                                        avgScreenTime = avgScreenTimeEdit,
                                        avgSleepHours = avgSleepHoursEdit,
                                        preferredWorkTime = preferredWorkTimeEdit,
                                        stressLevel = stressLevelEdit.toInt()
                                    )
                                    viewModel.saveProfile()
                                },
                                modifier = Modifier.semantics { this.contentDescription = "Save Profile Button" }
                            ) {
                                Icon(Icons.Default.Save, "Save", tint = Color.White)
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.setEditing(true) },
                                modifier = Modifier.semantics { this.contentDescription = "Edit Profile Button" }
                            ) {
                                Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                            }
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success / Error Messages
                if (state.successMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StrainLow.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = state.successMessage ?: "",
                            color = StrainLow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .semantics { this.contentDescription = "Profile Save Success Message" }
                        )
                    }
                }

                if (state.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StrainHigh.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = StrainHigh,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }

                // 1. Account Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222F47), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2C))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .border(2.dp, AIPrimary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.fullName.isNotBlank()) state.fullName.take(1).uppercase() else "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        if (state.isEditing) {
                            ProfileEditField(value = fullNameEdit, onValueChange = { fullNameEdit = it }, label = "Full Name")
                        } else {
                            Text(
                                text = state.fullName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.semantics { this.contentDescription = "Profile Full Name" }
                            )
                        }

                        Text(
                            text = "@${state.username}",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(Modifier.height(12.dp))
                        Divider(color = Color(0xFF1E293B))
                        Spacer(Modifier.height(12.dp))

                        ProfileDetailRow("Email", state.email)
                        ProfileDetailRow("Member Since", if (state.createdAt.isNotBlank()) state.createdAt.take(10) else "Recent")
                    }
                }

                // 2. Profile Details & Baselines
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
                        Text("Profile Specifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        if (state.isEditing) {
                            ProfileEditField(value = ageEdit, onValueChange = { ageEdit = it }, label = "Age", keyboardType = KeyboardType.Number)
                            ProfileEditField(value = occupationEdit, onValueChange = { occupationEdit = it }, label = "Occupation")
                            ProfileEditField(value = genderEdit, onValueChange = { genderEdit = it }, label = "Gender")
                        } else {
                            Text(
                                text = "Age: ${state.age}",
                                fontSize = 14.sp,
                                color = Color.LightGray,
                                modifier = Modifier.semantics { this.contentDescription = "Profile Age" }
                            )
                            Text(
                                text = "Occupation: ${state.occupation}",
                                fontSize = 14.sp,
                                color = Color.LightGray,
                                modifier = Modifier.semantics { this.contentDescription = "Profile Occupation" }
                            )
                            ProfileDetailRow("Gender", state.gender)
                        }
                    }
                }

                // 3. Academic / Work Info Card
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
                        Text("Academic & Work Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        if (state.isEditing) {
                            ProfileEditField(value = institutionEdit, onValueChange = { institutionEdit = it }, label = "Institution / Company")
                            ProfileEditField(value = departmentEdit, onValueChange = { departmentEdit = it }, label = "Department / Field")
                            ProfileEditField(value = academicYearEdit, onValueChange = { academicYearEdit = it }, label = "Academic Year")
                            ProfileEditField(value = workingHoursEdit, onValueChange = { workingHoursEdit = it }, label = "Working Hours/Day", keyboardType = KeyboardType.Number)
                        } else {
                            ProfileDetailRow("Institution / Company", state.institution.ifBlank { "Not Specified" })
                            ProfileDetailRow("Department / Field", state.department.ifBlank { "Not Specified" })
                            ProfileDetailRow("Academic Year", state.academicYear.ifBlank { "Not Specified" })
                            ProfileDetailRow("Working Hours / Day", if (state.workingHours.isNotBlank()) "${state.workingHours} hrs" else "Not Specified")
                        }
                    }
                }

                // 4. Baseline Settings
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
                        Text("Behavioral Baselines", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        if (state.isEditing) {
                            ProfileEditField(value = avgScreenTimeEdit, onValueChange = { avgScreenTimeEdit = it }, label = "Avg Daily Screen Time (hours)", keyboardType = KeyboardType.Number)
                            ProfileEditField(value = avgSleepHoursEdit, onValueChange = { avgSleepHoursEdit = it }, label = "Avg Sleep Hours", keyboardType = KeyboardType.Number)
                            ProfileEditField(value = preferredWorkTimeEdit, onValueChange = { preferredWorkTimeEdit = it }, label = "Preferred Study Time")

                            Column {
                                Text("Stress Level (1-10): ${stressLevelEdit.toInt()}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = stressLevelEdit,
                                    onValueChange = { stressLevelEdit = it },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(thumbColor = AIPrimary, activeTrackColor = AIPrimary)
                                )
                            }
                        } else {
                            ProfileDetailRow("Avg Screen Time / Day", if (state.avgScreenTime.isNotBlank()) "${state.avgScreenTime} hrs" else "Not Specified")
                            ProfileDetailRow("Avg Sleep / Night", if (state.avgSleepHours.isNotBlank()) "${state.avgSleepHours} hrs" else "Not Specified")
                            ProfileDetailRow("Preferred Study Time", state.preferredWorkTime)

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Stress Calibration", color = Color.Gray, fontSize = 12.sp)
                                    Text("${state.stressLevel} / 10", color = AIPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                val progressColor = if (state.stressLevel < 4) StrainLow else if (state.stressLevel < 7) StrainModerate else StrainHigh
                                LinearProgressIndicator(
                                    progress = (state.stressLevel / 10f).coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = progressColor,
                                    trackColor = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }

                // 5. Danger Zone - Account Deletion
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StrainHigh.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StrainHigh.copy(alpha = 0.03f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Danger Zone", color = StrainHigh, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Permanently delete your account and all associated cognitive logs. This action is irreversible.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StrainHigh),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        if (showDeleteConfirm) AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?", color = Color.White) },
            text = { Text("Are you absolutely sure you want to delete your account? This will permanently wipe your profile and statistics.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount {
                            navController.navigate(NavRoutes.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StrainHigh)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = Color.LightGray) }
            },
            containerColor = Color(0xFF151C2C)
        )
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.LightGray, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AIPrimary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0B0F19),
                unfocusedContainerColor = Color(0xFF0B0F19),
                focusedBorderColor = AIPrimary,
                unfocusedBorderColor = Color(0xFF222F47)
            )
        )
    }
}
