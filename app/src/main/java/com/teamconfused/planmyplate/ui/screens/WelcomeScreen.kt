package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.ui.theme.PlanMyPlateTheme

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // App Logo and Name
            Icon(
                painter = painterResource(id = R.drawable.demo_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PlanMyPlate",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "Get Started")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Already a member? Login")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    PlanMyPlateTheme {
        WelcomeScreen(
            onGetStartedClick = {},
            onLoginClick = {}
        )
    }
}