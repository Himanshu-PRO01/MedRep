package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AnalysisState
import com.example.ui.MainViewModel
import com.example.ui.components.CameraView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScannerScreen(viewModel: MainViewModel, onAnalyzeSuccess: () -> Unit) {
    var textInput by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    val state by viewModel.analysisState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        }
    }

    LaunchedEffect(state) {
        if (state is AnalysisState.Success) {
            onAnalyzeSuccess()
        }
    }

    if (showCamera) {
        CameraView(
            onImageCaptured = { base64 ->
                showCamera = false
                viewModel.analyzeImage(base64, apiKey)
            },
            onClose = { showCamera = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Medical Report Reader") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan Report")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enter your Gemini API Key:")
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("AIzaSy...") }
                )

                Text("Paste your medical report text here:")
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("E.g., Glucose: 110 mg/dL...") }
                )

                if (state is AnalysisState.Error) {
                    Text(
                        text = (state as AnalysisState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { viewModel.analyzeText(textInput, apiKey) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is AnalysisState.Loading
                ) {
                    if (state is AnalysisState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Analyze Report")
                    }
                }
            }
        }
    }
}
