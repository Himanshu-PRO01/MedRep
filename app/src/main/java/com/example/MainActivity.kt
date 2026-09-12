package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ReportScannerScreen
import com.example.ui.theme.AppTheme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Settings
import com.example.ui.ThemePreference
import com.example.ui.screens.SettingsScreen

val LocalLowPowerMode = compositionLocalOf { false }

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val themePref by viewModel.themePreference.collectAsState()
            val isLowPowerMode by viewModel.isLowPowerMode.collectAsState()
            
            val isDarkTheme = when (themePref) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            AppTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalLowPowerMode provides isLowPowerMode) {
                    var isAuthenticated by remember { mutableStateOf(false) }
                    
                    if (isAuthenticated) {
                        MedicalReportApp(viewModel)
                    } else {
                        BiometricLockScreen(
                            onAuthenticated = { isAuthenticated = true },
                            activity = this
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricLockScreen(onAuthenticated: () -> Unit, activity: FragmentActivity) {
    var errorText by remember { mutableStateOf<String?>(null) }
    
    val authenticate = {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    errorText = "Authentication error: \$errString"
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    errorText = "Authentication failed"
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Medical Data")
            .setSubtitle("Authenticate to access your sensitive medical reports")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        authenticate()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Secured Medical Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your lab reports and patient data are protected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = authenticate) {
                Text("Unlock")
            }
            if (errorText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MedicalReportApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute == "scanner" || currentRoute == "history" || currentRoute == "settings") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Scan") },
                        label = { Text("Scan") },
                        selected = currentRoute == "scanner",
                        onClick = {
                            navController.navigate("scanner") {
                                popUpTo("scanner") { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentRoute == "history",
                        onClick = {
                            navController.navigate("history") {
                                popUpTo("scanner")
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo("scanner")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "scanner",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { if (LocalLowPowerMode.current) EnterTransition.None else fadeIn() },
            exitTransition = { if (LocalLowPowerMode.current) ExitTransition.None else fadeOut() },
            popEnterTransition = { if (LocalLowPowerMode.current) EnterTransition.None else fadeIn() },
            popExitTransition = { if (LocalLowPowerMode.current) ExitTransition.None else fadeOut() }
        ) {
            composable("scanner") {
                ReportScannerScreen(viewModel) {
                    navController.navigate("analysis")
                }
            }
            composable("analysis") {
                AnalysisScreen(viewModel) {
                    viewModel.clearAnalysis()
                    navController.popBackStack()
                }
            }
            composable("history") {
                HistoryScreen(viewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel)
            }
        }
    }
}
