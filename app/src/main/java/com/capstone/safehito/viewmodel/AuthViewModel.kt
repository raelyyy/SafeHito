package com.capstone.safehito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.safehito.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _authResult = MutableStateFlow("")
    val authResult: StateFlow<String> = _authResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _authResult.value = "Login successful"
                } else {
                    _authResult.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        contactNumber: String
    ) {
        viewModelScope.launch {
            val result = repo.signUp(email, password, fullName, contactNumber)
            _authResult.value = result.getOrElse { it.message ?: "Signup failed" }
        }
    }


    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = repo.resetPassword(email)
            _authResult.value = result.getOrElse { it.message ?: "Unknown error" }
        }
    }

    fun setError(message: String) {
        _authResult.value = message
    }
}
