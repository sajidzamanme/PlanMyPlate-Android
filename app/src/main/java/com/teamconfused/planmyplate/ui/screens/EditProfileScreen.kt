package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.ui.components.InputLabel
import com.teamconfused.planmyplate.ui.viewmodels.EditProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Personal Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = remember(uiState.dateOfBirth) {
                    runCatching {
                        java.time.LocalDate.parse(uiState.dateOfBirth)
                            .atStartOfDay(java.time.ZoneId.of("UTC"))
                            .toInstant()
                            .toEpochMilli()
                    }.getOrNull()
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onDateOfBirthChange(localDate.toString())
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.firstName.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // First Name
                    Column {
                        InputLabel(text = "First Name")
                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = viewModel::onFirstNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            isError = uiState.firstNameError != null,
                            supportingText = {
                                uiState.firstNameError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    // Last Name
                    Column {
                        InputLabel(text = "Last Name")
                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = viewModel::onLastNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            isError = uiState.lastNameError != null,
                            supportingText = {
                                uiState.lastNameError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    // Email (Read-only)
                    Column {
                        InputLabel(text = "Email Address")
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Phone Number
                    Column {
                        InputLabel(text = "Phone Number")
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChange,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            isError = uiState.phoneError != null,
                            supportingText = {
                                uiState.phoneError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    // Date of Birth
                    Column {
                        InputLabel(text = "Date of Birth (YYYY-MM-DD)")
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.dateOfBirth,
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                singleLine = true,
                                readOnly = true,
                                isError = uiState.dateOfBirthError != null,
                                supportingText = {
                                    uiState.dateOfBirthError?.let {
                                        Text(text = it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Select Date"
                                    )
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showDatePicker = true }
                            )
                        }
                    // Date of Birth column ends here
                    }

                    Button(
                        onClick = {
                            viewModel.updateProfile(onSuccess = onNavigateBack)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Failed to Update Profile") },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("OK")
                }
            }
        )
    }
}
