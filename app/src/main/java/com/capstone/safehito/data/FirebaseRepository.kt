package com.capstone.safehito.data

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("waterData")

    // Helper function to safely read a Double value stored as String or Double in Firebase
    private fun getDoubleFromSnapshot(snapshot: DataSnapshot, key: String): String {
        val rawValue = snapshot.getValue()  // raw value can be Double, Long, String, or null

        Log.d("FirebaseRepository", "Raw value for $key: $rawValue")

        return when (rawValue) {
            is Double -> rawValue.toString()
            is Long -> rawValue.toDouble().toString() // Firebase might store numbers as Long
            is String -> rawValue.toDoubleOrNull()?.toString() ?: "N/A"
            else -> "N/A"
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
                trySend("N/A")
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
                trySend("N/A")
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
                trySend("N/A")
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
                trySend("N/A")
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
                trySend("N/A")
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
                trySend(waterStatus ?: "N/A")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get waterStatus: ${error.message}")
                trySend("N/A")
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
                trySend(fishStatus ?: "N/A")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Failed to get fishStatus: ${error.message}")
                trySend("N/A")
            }
        }
        dbRef.child("fishStatus").addValueEventListener(listener)
        awaitClose { dbRef.child("fishStatus").removeEventListener(listener) }
    }

}
