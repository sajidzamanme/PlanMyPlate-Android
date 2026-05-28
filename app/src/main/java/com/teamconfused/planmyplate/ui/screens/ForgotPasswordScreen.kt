package com.teamconfused.planmyplate.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.ui.components.InputLabel
import com.teamconfused.planmyplate.ui.theme.PlanMyPlateTheme
import com.teamconfused.planmyplate.ui.viewmodels.ForgotPasswordStep
import com.teamconfused.planmyplate.ui.viewmodels.ForgotPasswordUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onVerifyCodeClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
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
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            when (uiState.step) {
                ForgotPasswordStep.EMAIL_INPUT -> {
                    EmailInputContent(
                        email = uiState.email,
                        error = uiState.error,
                        onEmailChange = onEmailChange,
                        onSendCodeClick = onSendCodeClick
                    )
                }
                ForgotPasswordStep.VERIFICATION_CODE -> {
                    VerificationCodeContent(
                        email = uiState.email,
                        code = uiState.code,
                        error = uiState.error,
                        onCodeChange = onCodeChange,
                        onVerifyCodeClick = onVerifyCodeClick,
                        onResendClick = { /* Resend logic */ }
                    )
                }
                ForgotPasswordStep.RESET_PASSWORD, ForgotPasswordStep.SUCCESS -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ResetPasswordContent(
                            newPassword = uiState.newPassword,
                            confirmPassword = uiState.confirmPassword,
                            error = uiState.error,
                            onNewPasswordChange = onNewPasswordChange,
                            onConfirmPasswordChange = onConfirmPasswordChange,
                            onResetPasswordClick = onResetPasswordClick,
                            modifier = if (uiState.step == ForgotPasswordStep.SUCCESS) Modifier.blur(4.dp) else Modifier
                        )
                        
                        if (uiState.step == ForgotPasswordStep.SUCCESS) {
                            SuccessDialog(onLoginClick = onLoginClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailInputContent(
    email: String,
    error: String?,
    onEmailChange: (String) -> Unit,
    onSendCodeClick: () -> Unit
) {
    Column {
        Text(
            text = "Forgot password",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your email for the verification process.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        InputLabel(text = "Email Address")
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            singleLine = true,
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSendCodeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Send Code", fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun VerificationCodeContent(
    email: String,
    code: String,
    error: String?,
    onCodeChange: (String) -> Unit,
    onVerifyCodeClick: () -> Unit,
    onResendClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        Text(
            text = "Enter 4 digit code",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter 4 digit code that your receive on your email ($email)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { focusRequester.requestFocus() } // Clicking anywhere opens keyboard
        ) {
            BasicTextField(
                value = code,
                onValueChange = {
                    // Accept only digits and max 4 chars
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        onCodeChange(it)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.focusRequester(focusRequester),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(4) { index ->
                            val isFilled = index < code.length
                            val char = if (isFilled) code[index].toString() else ""

                            // Box highlight animation
                            val borderColor by animateColorAsState(
                                if (index == code.length) Color.Green else Color.LightGray
                            )

                            OutlinedTextField(
                                value = char,
                                onValueChange = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .pointerInteropFilter { true },  // <<< BLOCK CLICKS WITHOUT HIDING KEYBOARD
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor,
                                    cursorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            )
        }
        
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Not received a code? ", color = Color.Gray)
            Text(
                "Resend",
                modifier = Modifier.clickable { onResendClick() },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyCodeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue", fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun ResetPasswordContent(
    newPassword: String,
    confirmPassword: String,
    error: String?,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Reset password",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Set the new password for your account.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        InputLabel(text = "Password")
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        InputLabel(text = "Re-enter Password")
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        )
        
        if (error != null) {
             Spacer(modifier = Modifier.height(8.dp))
             Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onResetPasswordClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Set a New Password", fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun SuccessDialog(onLoginClick: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon
                Icon(
                    painter = painterResource(R.drawable.check_circle_icon),
                    contentDescription = "Success",
                    tint = Color.Green,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Password Changed!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your can now use your new password to login.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    PlanMyPlateTheme {
        ForgotPasswordScreen(
            uiState = ForgotPasswordUiState(),
            onEmailChange = {},
            onCodeChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSendCodeClick = {},
            onVerifyCodeClick = {},
            onResetPasswordClick = {},
            onLoginClick = {},
            onBackClick = {}
        )
    }
}
