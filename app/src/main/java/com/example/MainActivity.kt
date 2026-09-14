package com.example

import android.os.Bundle
import android.util.Log
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
import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener { auth ->
                            currentUser = auth.currentUser
                        }
                        Firebase.auth.addAuthStateListener(listener)
                        onDispose {
                            Firebase.auth.removeAuthStateListener(listener)
                        }
                    }

                    if (currentUser != null) {
                        var isBiometricAuthenticated by remember { mutableStateOf(false) }
                        if (isBiometricAuthenticated) {
                            MedicalReportApp(viewModel)
                        } else {
                            BiometricLockScreen(
                                onAuthenticated = { isBiometricAuthenticated = true },
                                activity = this@MainActivity
                            )
                        }
                    } else {
                        AuthScreen(
                            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
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
        val isLowPower = LocalLowPowerMode.current
        NavHost(
            navController = navController,
            startDestination = "scanner",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { if (isLowPower) EnterTransition.None else fadeIn() },
            exitTransition = { if (isLowPower) ExitTransition.None else fadeOut() },
            popEnterTransition = { if (isLowPower) EnterTransition.None else fadeIn() },
            popExitTransition = { if (isLowPower) ExitTransition.None else fadeOut() }
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

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(context, credentialManager, onAuthSuccess, {}, coroutineScope)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Sign In Required",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sign in to Sync Profiles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Access your medical reports securely across all your devices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                onGoogleSignInClicked(context, credentialManager, onAuthSuccess, { errorText = it }, coroutineScope)
            }) {
                Text("Sign in with Google")
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

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val clientId = if (clientIdResId != 0) {
        try { context.getString(clientIdResId) } catch (e: Exception) { null }
    } else null
    if (clientId.isNullOrEmpty()) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope
) {
    val clientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val clientId = if (clientIdResId != 0) {
        try { context.getString(clientIdResId) } catch (e: Exception) { null }
    } else null
    if (clientId.isNullOrEmpty()) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found in google-services.json")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: Exception) {
            if (e !is GetCredentialCancellationException) {
                Log.e("Auth", "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }
}
