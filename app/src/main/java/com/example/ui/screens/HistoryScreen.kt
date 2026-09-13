package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.ui.ThemePreference
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val reports by viewModel.savedReports.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Blood Test", "Imaging", "Prescription", "Other")

    val filteredReports = if (selectedCategory == null) reports else reports.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            val themePref by viewModel.themePreference.collectAsState()
            TopAppBar(
                title = { Text("Saved Reports") },
                actions = {
                    IconButton(onClick = { 
                        val nextTheme = if (themePref == ThemePreference.DARK) ThemePreference.LIGHT else ThemePreference.DARK
                        viewModel.setThemePreference(nextTheme)
                    }) {
                        Icon(
                            imageVector = if (themePref == ThemePreference.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (reports.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") }
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (reports.isEmpty()) "No saved reports yet." else "No reports in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                if (selectedCategory == "Blood Test") {
                    BloodTestTrendsSection(reports = reports)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReports) { report ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = report.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${report.category} • ${report.date} • ${report.urgency}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteReport(report.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Report", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
