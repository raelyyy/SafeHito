package com.capstone.safehito.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.safehito.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _uploadResult = MutableStateFlow("")
    val uploadResult: StateFlow<String> = _uploadResult

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _profilePictureUrl = MutableStateFlow("")
    val profilePictureUrl: StateFlow<String> = _profilePictureUrl

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _isUploading.value = true
            val result = repo.uploadProfilePicture(uri)
            _isUploading.value = false
            result.onSuccess { url ->
                _profilePictureUrl.value = url
                _uploadResult.value = "Upload successful"
            }.onFailure {
                _uploadResult.value = it.message ?: "Upload failed"
            }
        }
    }

    fun updateProfile(fullName: String, phone: String) {
        viewModelScope.launch {
            val result = repo.updateProfile(fullName, phone)
            _uploadResult.value = result.getOrElse { it.message ?: "Update failed" }
        }
    }

    fun resetMessage() {
        _uploadResult.value = ""
    }
}
