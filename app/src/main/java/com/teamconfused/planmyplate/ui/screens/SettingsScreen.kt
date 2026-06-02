package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onLogoutClick: () -> Unit,
    onUpdatePreferencesClick: () -> Unit = {}
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Update Preferences Button
            OutlinedButton(
                onClick = onUpdatePreferencesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Update Preferences")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Logout")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
