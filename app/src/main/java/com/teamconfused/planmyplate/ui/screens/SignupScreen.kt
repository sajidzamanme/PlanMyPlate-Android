package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.ui.components.InputLabel
import com.teamconfused.planmyplate.ui.theme.PlanMyPlateTheme
import com.teamconfused.planmyplate.ui.viewmodels.SignupUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    uiState: SignupUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_icon),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Sign up",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    InputLabel(text = "First Name")
                    OutlinedTextField(
                        value = uiState.firstName,
                        onValueChange = onFirstNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        isError = uiState.firstNameError != null,
                        supportingText = { uiState.firstNameError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    InputLabel(text = "Last Name")
                    OutlinedTextField(
                        value = uiState.lastName,
                        onValueChange = onLastNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        isError = uiState.lastNameError != null,
                        supportingText = { uiState.lastNameError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                    )
                }
            }

            InputLabel(text = "Email Address")
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = { uiState.emailError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            InputLabel(text = "Phone Number")
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                isError = uiState.phoneError != null,
                supportingText = { uiState.phoneError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
            )

            InputLabel(text = "Date of Birth (YYYY-MM-DD)")
            OutlinedTextField(
                value = uiState.dateOfBirth,
                onValueChange = onDateOfBirthChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            InputLabel(text = "Password")
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = uiState.passwordError != null,
                supportingText = { uiState.passwordError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                singleLine = true
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = uiState.isTermsAccepted, onCheckedChange = onTermsAcceptedChange)
                Text(
                    text = "I agree to PlanMyPlate's Terms & Conditions",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onTermsAcceptedChange(!uiState.isTermsAccepted) }
                )
            }
            uiState.termsError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSignupClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.isTermsAccepted && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Create an Account", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already a member? ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { onLoginClick() },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    PlanMyPlateTheme {
        SignupScreen(
            uiState = SignupUiState(),
            onFirstNameChange = {},
            onLastNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onPhoneChange = {},
            onDateOfBirthChange = {},
            onTermsAcceptedChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onBackClick = {}
        )
    }
}
