package com.capstone.safehito.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")
    private val storage = FirebaseStorage.getInstance().getReference("profilePictures")

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        contactNumber: String
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("User ID is null"))

            val user = mapOf(
                "fullName" to fullName,
                "email" to email,
                "contactNumber" to contactNumber,
                "profilePictureUrl" to ""  // Placeholder
            )

            database.child(uid).setValue(user).await()

            Result.success("Sign up successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(uri: Uri): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val imageRef = storage.child("$uid.jpg")
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            database.child(uid).child("profilePictureUrl").setValue(downloadUrl).await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(fullName: String, phone: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val updates = mapOf(
                "fullName" to fullName,
                "contactNumber" to phone
            )
            database.child(uid).updateChildren(updates).await()
            Result.success("Profile updated")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun resetPassword(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success("Password reset email sent")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
