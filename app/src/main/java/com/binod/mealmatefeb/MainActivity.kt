package com.binod.mealmatefeb

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.binod.mealmatefeb.data.UserRepository
import com.binod.mealmatefeb.screens.*
import com.binod.mealmatefeb.ui.theme.MealmatefebTheme
import com.binod.mealmatefeb.viewmodel.AuthViewModel
import androidx.compose.ui.platform.LocalContext
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import com.binod.mealmatefeb.viewmodel.ViewModelFactory

@Composable
fun MainContent(
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    viewModel: AuthViewModel
) {
    when (currentScreen) {
        "splash" -> SplashScreen(
            onSplashComplete = { onScreenChange("welcome") }
        )
        "welcome" -> WelcomeScreen(
            onSignInClick = { onScreenChange("login") },
            onSignUpClick = { onScreenChange("register") }
        )
        "login" -> LoginScreen(
            onLoginClick = { email, password -> 
                viewModel.login(email, password)
            },
            onRegisterClick = { onScreenChange("register") },
            viewModel = viewModel
        )
        "register" -> RegisterScreen(
            onRegisterClick = { email, password, name ->
                viewModel.register(email, password, name)
            },
            onBackToLoginClick = { onScreenChange("login") },
            viewModel = viewModel
        )
        "home" -> HomeScreen(
            onScreenChange = onScreenChange,
            authViewModel = viewModel
        )
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AuthViewModel

    private val permissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(
                this,
                "Both SMS and Contacts permissions are required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun checkAndRequestPermissions() {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (!allGranted) {
            requestPermissionLauncher.launch(permissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProvider(this, ViewModelFactory(applicationContext))[AuthViewModel::class.java]

        // Clear any persisted login state when app starts
        viewModel.logout()

        setContent {
            MealmatefebTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    val currentUser by viewModel.currentUser.collectAsState()
                    
                    LaunchedEffect(viewModel.authState.collectAsState().value) {
                        when (val state = viewModel.authState.value) {
                            is AuthViewModel.AuthState.Success -> currentScreen = "home"
                            is AuthViewModel.AuthState.RegisterSuccess -> {
                                currentScreen = "login"
                                viewModel.resetAuthState()
                            }
                            is AuthViewModel.AuthState.Error -> {
                                // Handle error state if needed
                            }
                            else -> {}
                        }
                    }

                    MainContent(
                        currentScreen = currentScreen,
                        onScreenChange = { newScreen -> 
                            if (newScreen == "login" || newScreen == "welcome") {
                                viewModel.logout()
                            }
                            currentScreen = newScreen 
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}