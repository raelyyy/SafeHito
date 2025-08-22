package com.capstone.safehito.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "user", // "user", "admin", or "superadmin"
    val contactNumber: String = "",
    val profilePictureUrl: String = "",
    val profileImageBase64: String = "",
    val lastActive: Long? = null
) 