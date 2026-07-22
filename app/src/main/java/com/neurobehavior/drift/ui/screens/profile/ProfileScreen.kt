package com.neurobehavior.drift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.neurobehavior.drift.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    var fullNameEdit by remember { mutableStateOf("") }
    var ageEdit by remember { mutableStateOf("") }
    var occupationEdit by remember { mutableStateOf("") }

    LaunchedEffect(state.isEditing) {
        if (state.isEditing) {
            fullNameEdit = state.fullName
            ageEdit = state.age
            occupationEdit = state.occupation
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { this.contentDescription = "Profile Header" }) },
                navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    if (state.isEditing) {
                        IconButton(
                            onClick = {
                                viewModel.updateField(fullNameEdit, ageEdit, occupationEdit)
                                viewModel.saveProfile()
                            },
                            modifier = Modifier.semantics { this.contentDescription = "Save Profile Button" }
                        ) {
                            Icon(Icons.Default.Save, "Save")
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.setEditing(true) },
                            modifier = Modifier.semantics { this.contentDescription = "Edit Profile Button" }
                        ) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (state.isEditing) {
                OutlinedTextField(
                    value = fullNameEdit,
                    onValueChange = { fullNameEdit = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = ageEdit,
                    onValueChange = { ageEdit = it },
                    label = { Text("Age") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = occupationEdit,
                    onValueChange = { occupationEdit = it },
                    label = { Text("Occupation") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            } else {
                Text(
                    text = state.fullName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .semantics { this.contentDescription = "Profile Full Name" }
                )

                Text(
                    text = "Age: ${state.age}",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .semantics { this.contentDescription = "Profile Age" }
                )

                Text(
                    text = "Occupation: ${state.occupation}",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .semantics { this.contentDescription = "Profile Occupation" }
                )
            }

            if (state.successMessage != null) {
                Text(
                    text = state.successMessage ?: "",
                    color = Color.Green,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .semantics { this.contentDescription = "Profile Save Success Message" }
                )
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
