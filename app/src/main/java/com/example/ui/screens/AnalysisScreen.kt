package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SavedReport
import com.example.ui.AnalysisState
import com.example.ui.MainViewModel

import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val state by viewModel.analysisState.collectAsState()
    val view = LocalView.current
    
    when (val s = state) {
        is AnalysisState.Success -> {
            val report = s.report
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Analysis") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.saveReport(report) 
                            }) {
                                Icon(Icons.Default.Bookmark, contentDescription = "Save Report")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = report.urgencyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = report.patientSummary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    Text("Test Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    report.testItems.forEach { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.testName, fontWeight = FontWeight.Bold)
                                Text("Result: ${item.resultValue} (${item.status})")
                                Text("Normal Range: ${item.normalRange}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.explanation, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
        else -> {
            // Handled in parent
        }
    }
}
