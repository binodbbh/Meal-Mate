package com.binod.mealmatefeb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binod.mealmatefeb.data.User
import com.binod.mealmatefeb.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        // Check for existing logged in user when ViewModel is created
        viewModelScope.launch {
            _currentUser.value = repository.getCurrentUser()
            if (_currentUser.value != null) {
                _authState.value = AuthState.Success
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = repository.loginUser(email, password)
            if (user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Invalid credentials")
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            val user = User(email, password, name)
            val success = repository.registerUser(user)
            if (success) {
                _authState.value = AuthState.RegisterSuccess
            } else {
                _authState.value = AuthState.Error("Email already exists")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            _authState.value = AuthState.Initial
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }

    sealed class AuthState {
        object Initial : AuthState()
        object Success : AuthState()
        object RegisterSuccess : AuthState()
        data class Error(val message: String) : AuthState()
    }
} 