package com.binod.mealmatefeb.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.binod.mealmatefeb.data.UserRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(UserRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 