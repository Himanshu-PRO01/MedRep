package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val themePref by viewModel.themePreference.collectAsState()
    val isLowPowerMode by viewModel.isLowPowerMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Power & Performance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Low Power Mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Reduces background processing, chart updates, and animations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isLowPowerMode,
                        onCheckedChange = { viewModel.setLowPowerMode(it) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.selectableGroup()) {
                    ThemeOptionRow(
                        text = "System Default",
                        selected = themePref == ThemePreference.SYSTEM,
                        onClick = { viewModel.setThemePreference(ThemePreference.SYSTEM) }
                    )
                    ThemeOptionRow(
                        text = "Light Mode",
                        selected = themePref == ThemePreference.LIGHT,
                        onClick = { viewModel.setThemePreference(ThemePreference.LIGHT) }
                    )
                    ThemeOptionRow(
                        text = "Dark Mode",
                        selected = themePref == ThemePreference.DARK,
                        onClick = { viewModel.setThemePreference(ThemePreference.DARK) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null recommended for accessibility with selectable
        )
        Spacer(Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
