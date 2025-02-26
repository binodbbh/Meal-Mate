package com.binod.mealmatefeb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.binod.mealmatefeb.data.UserRepository
import com.binod.mealmatefeb.screens.LoginScreen
import com.binod.mealmatefeb.screens.RegisterScreen
import com.binod.mealmatefeb.screens.HomeScreen
import com.binod.mealmatefeb.ui.theme.MealmatefebTheme
import com.binod.mealmatefeb.viewmodel.AuthViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainContent(
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    viewModel: AuthViewModel
) {
    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginClick = { email, password -> 
                viewModel.login(email, password)
            },
            onRegisterClick = { onScreenChange("register") }
        )
        "register" -> RegisterScreen(
            onRegisterClick = { email, password, name ->
                viewModel.register(email, password, name)
                onScreenChange("login")
            },
            onBackToLoginClick = { onScreenChange("login") }
        )
        "home" -> HomeScreen(
            onScreenChange = onScreenChange,
            authViewModel = viewModel
        )
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = UserRepository(applicationContext)
        viewModel = AuthViewModel(repository)

        setContent {
            MealmatefebTheme {
                var currentScreen by remember { mutableStateOf("login") }
                
                LaunchedEffect(viewModel.authState.collectAsState().value) {
                    when (val state = viewModel.authState.value) {
                        is AuthViewModel.AuthState.Success -> currentScreen = "home"
                        is AuthViewModel.AuthState.Error -> {
                            // Handle error state if needed
                        }
                        else -> {}
                    }
                }

                MainContent(
                    currentScreen = currentScreen,
                    onScreenChange = { newScreen -> currentScreen = newScreen },
                    viewModel = viewModel
                )
            }
        }
    }
}