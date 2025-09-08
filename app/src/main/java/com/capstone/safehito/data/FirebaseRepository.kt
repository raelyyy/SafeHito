package com.capstone.safehito.data

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.capstone.safehito.model.User
import com.capstone.safehito.model.AuditLog
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.FirebaseDatabase


class FirebaseRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("waterData")
    private val database = FirebaseDatabase.getInstance().reference


    // Helper function to safely read a Double value stored as String or Double in Firebase
    private fun getDoubleFromSnapshot(snapshot: DataSnapshot, key: String): String {
        val rawValue = snapshot.getValue()  // raw value can be Double, Long, String, or null

        Log.d("FirebaseRepository", "Raw value for $key: $rawValue")

        return when (rawValue) {
            is Double -> rawValue.toString()
            is Long -> rawValue.toDouble().toString() // Firebase might store numbers as Long
            is String -> rawValue.toDoubleOrNull()?.toString() ?: "--"
            else -> "--"
        }
    }

    fun getPh(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ph = getDoubleFromSnapshot(snapshot, "ph")
                trySend(ph)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get ph: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("ph").addValueEventListener(listener)
        awaitClose { dbRef.child("ph").removeEventListener(listener) }
    }

    fun getTemperature(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = getDoubleFromSnapshot(snapshot, "temperature")
                trySend(temp)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get temperature: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("temperature").addValueEventListener(listener)
        awaitClose { dbRef.child("temperature").removeEventListener(listener) }
    }

    fun getTurbidity(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val turbidity = getDoubleFromSnapshot(snapshot, "turbidity")
                trySend(turbidity)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get turbidity: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("turbidity").addValueEventListener(listener)
        awaitClose { dbRef.child("turbidity").removeEventListener(listener) }
    }

    fun getDissolvedOxygen(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val oxygen = getDoubleFromSnapshot(snapshot, "oxygen")
                trySend(oxygen)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get oxygen: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("oxygen").addValueEventListener(listener)
        awaitClose { dbRef.child("oxygen").removeEventListener(listener) }
    }

    fun getWaterLevel(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waterLevel = getDoubleFromSnapshot(snapshot, "waterLevel")
                trySend(waterLevel)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get waterLevel: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("waterLevel").addValueEventListener(listener)
        awaitClose { dbRef.child("waterLevel").removeEventListener(listener) }
    }

    fun getWaterStatus(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waterStatus = snapshot.getValue(String::class.java)
                Log.d("FirebaseRepository", "Water Status: $waterStatus")
                trySend(waterStatus ?: "--")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get waterStatus: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("waterStatus").addValueEventListener(listener)
        awaitClose { dbRef.child("waterStatus").removeEventListener(listener) }
    }

    fun getFishStatus(): Flow<String> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fishStatus = snapshot.getValue(String::class.java)
                Log.d("FirebaseRepository", "Fish Status: $fishStatus")
                trySend(fishStatus ?: "--")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get fishStatus: ${error.message}")
                trySend("--")
            }
        }
        dbRef.child("fishStatus").addValueEventListener(listener)
        awaitClose { dbRef.child("fishStatus").removeEventListener(listener) }
    }

    // ADMIN: Fetch all users
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (userSnap in snapshot.children) {
                    val user = userSnap.getValue(User::class.java)
                    user?.let {
                        users.add(it.copy(id = userSnap.key ?: ""))
                    }
                }
                trySend(users)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get users: ${error.message}")
                trySend(emptyList())
            }
        }
        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }

    // ADMIN: Add user
    fun addUser(fullName: String, email: String, contactNumber: String, role: String, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val newUserRef = usersRef.push()
        val user = mapOf(
            "id" to newUserRef.key,
            "fullName" to fullName,
            "email" to email,
            "role" to role,
            "contactNumber" to contactNumber,
            "profilePictureUrl" to "",
            "profileImageBase64" to ""
        )
        newUserRef.setValue(user)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // ADMIN: Edit user
    fun editUser(userId: String, updatedUser: User, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val updates = mapOf(
            "role" to updatedUser.role,
            "fullName" to updatedUser.fullName,
            "email" to updatedUser.email,
            "contactNumber" to updatedUser.contactNumber
        )
        usersRef.child(userId).updateChildren(updates)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // ADMIN: Remove user
    fun removeUser(userId: String, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.child(userId).removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // ADMIN: Log audit action (legacy method - use AuditLogService instead)
    fun logAuditAction(action: String, performedBy: String) {
        val auditRef = FirebaseDatabase.getInstance().getReference("audit_logs")
        val log = AuditLog(
            action = action, 
            performedBy = performedBy, 
            timestamp = System.currentTimeMillis(),
            details = "Legacy audit log entry",
            category = "LEGACY",
            severity = "INFO",
            userId = performedBy,
            sessionId = "LEGACY"
        )
        auditRef.push().setValue(log)
    }

    // ADMIN: Fetch audit logs
    fun getAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val auditRef = FirebaseDatabase.getInstance().getReference("audit_logs")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logs = mutableListOf<AuditLog>()
                for (logSnap in snapshot.children) {
                    val log = logSnap.getValue(AuditLog::class.java)
                    log?.let { logs.add(it) }
                }
                trySend(logs)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get audit logs: ${error.message}")
                trySend(emptyList())
            }
        }
        auditRef.addValueEventListener(listener)
        awaitClose { auditRef.removeEventListener(listener) }
    }

    // ADMIN: Fetch total scans across all users (one-time fetch)
    suspend fun fetchTotalScans(): Int {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        var total = 0
        val task = usersRef.get()
        val snapshot = task.await()
        for (userSnap in snapshot.children) {
            val scansSnap = userSnap.child("scans")
            total += scansSnap.childrenCount.toInt()
        }
        return total
    }

    // ADMIN: Fetch total infected scans across all users (one-time fetch)
    suspend fun fetchTotalInfectedScans(): Int {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        var total = 0
        val task = usersRef.get()
        val snapshot = task.await()
        for (userSnap in snapshot.children) {
            val scansSnap = userSnap.child("scans")
            for (scanSnap in scansSnap.children) {
                val status = scanSnap.child("status").getValue(String::class.java) ?: ""
                if (status.equals("Infected", ignoreCase = true)) {
                    total++
                }
            }
        }
        return total
    }

    // UTILITY: Set superadmin (use this function to promote a user to superadmin)
    fun setSuperadmin(userId: String, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.child(userId).child("role").setValue("superadmin")
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun clearAllAuditLogs(onComplete: (Boolean) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("audit_logs")
        ref.removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


}
