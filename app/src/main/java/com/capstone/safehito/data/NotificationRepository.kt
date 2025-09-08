package com.capstone.safehito.data

import com.capstone.safehito.model.Notification
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class NotificationRepository {
    private val recordsRef = FirebaseDatabase.getInstance().getReference("records/Hourly")

    fun getImportantNotifications(): Flow<List<Notification>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = mutableListOf<Notification>()
                for (child in snapshot.children) {
                    val waterStatus = child.child("waterStatus").getValue(String::class.java)
                    val timeValue = child.child("time").value
                    val time = when (timeValue) {
                        is Long -> timeValue
                        is String -> timeValue.toLongOrNull() ?: 0L
                        else -> 0L
                    }

                    // Only create notifications for Warning status to reduce spam
                    if (waterStatus == "Warning") {
                        val id = child.key ?: time.toString()  // Fallback to time if no key
                        val message = "Water quality WARNING detected"
                        notifications.add(
                            Notification(
                                id = id,
                                message = message,
                                time = time,
                                read = false,
                                important = true
                            )
                        )
                    }
                }

                trySend(notifications.sortedByDescending { it.time })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        recordsRef.addValueEventListener(listener)
        awaitClose { recordsRef.removeEventListener(listener) }
    }
}
