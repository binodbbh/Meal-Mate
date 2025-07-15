package com.binod.mealmatefeb.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.R
import com.binod.mealmatefeb.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val authState by viewModel.authState.collectAsState()
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState as AuthViewModel.AuthState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.meal_logo),
            contentDescription = "MealMate Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Welcome To MealMate",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            singleLine = true
        )

        Button(
            onClick = { 
                when {
                    email.isBlank() -> {
                        Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_LONG).show()
                    }
                    !email.contains("@") || !email.contains(".") -> {
                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
                    }
                    password.isBlank() -> {
                        Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        onLoginClick(email, password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login")
        }

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Don't have an account? Register")
        }
    }
} 