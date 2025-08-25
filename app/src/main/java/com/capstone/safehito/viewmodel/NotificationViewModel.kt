package com.capstone.safehito.viewmodel

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.safehito.model.Notification
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewModel(private val uid: String) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> get() = _notifications

    private val database = FirebaseDatabase.getInstance()
    private val notifRef = database.getReference("notifications").child(uid)

    private var listenerAdded = false
    private var fishStatusListenerAdded = false
    private var lastProcessedFishStatusKey: String? = null

    private var notificationsListener: ValueEventListener? = null

    private var lastWaterQualityMessage: String? = null
    private var lastWaterQualityTime: Long = 0L

    // Single instance flags
    companion object {
        private var isListeningFishStatus = false
    }

    init {
        cleanInvalidNotifications()
        listenToNotifications()
        //startWatchingFishStatus()
    }

    // Listen to notifications for displaying them
    private fun listenToNotifications() {
        if (listenerAdded) return
        listenerAdded = true

        notificationsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val notifList = snapshot.children.mapNotNull { child ->
                        try {
                            val id = child.child("id").getValue(String::class.java)
                                ?: child.key ?: return@mapNotNull null
                            val message = child.child("message").getValue(String::class.java) ?: "No message"
                            val time = when (val value = child.child("time").value) {
                                is Long -> value
                                is String -> value.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                            val read = child.child("read").getValue(Boolean::class.java) ?: false

                            Notification(id, message, time, read)
                        } catch (e: Exception) {
                            Log.e("NotificationDebug", "Error parsing notification: ${e.message}")
                            null
                        }
                    }
                    _notifications.value = notifList
                        .distinctBy { it.id }
                        .sortedByDescending { it.time }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationDebug", "Failed to load notifications: ${error.message}")
            }
        }

        notifRef.addValueEventListener(notificationsListener as ValueEventListener)
    }

    // Automatically start watching fish status (only once)
    fun watchFishStatusAndNotify() {
        if (isListeningFishStatus) {
            Log.d("NotificationViewModel", "Fish status listener already active.")
            return
        }
        isListeningFishStatus = true

        val fishRef = FirebaseDatabase.getInstance()
            .getReference("users/$uid/scans")


        var isFirstLoad = true

        fishRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("NotificationViewModel", "Fish status onDataChange triggered.")
                val records = snapshot.children
                    .mapNotNull { snap ->
                        val key = snap.key ?: return@mapNotNull null
                        val status = snap.child("status").getValue(String::class.java) ?: ""
                        val timestamp = snap.child("timestamp").getValue(Long::class.java) ?: 0L
                        RecordKeyStatus(key, status, timestamp)
                    }
                    .sortedByDescending { it.timestamp }

                val mostRecent = records.firstOrNull()

                if (isFirstLoad) {
                    isFirstLoad = false
                    lastProcessedFishStatusKey = mostRecent?.key
                    return
                }

                mostRecent?.let { record ->
                    if (
                        record.key != lastProcessedFishStatusKey &&
                        record.status.trim().equals("infected", ignoreCase = true)
                    ) {
                        createNotification(
                            "Fish infection detected in the latest record. Immediate action is recommended."
                        )
                        lastProcessedFishStatusKey = record.key
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationViewModel", "Failed to watch fish status: ${error.message}")
            }
        })
    }




    private fun createNotification(message: String) {
        val now = System.currentTimeMillis()
        val lowerMsg = message.lowercase()

        val isWaterQuality = listOf("ph", "oxygen", "dissolved", "temperature", "turbidity")
            .any { it in lowerMsg }

        if (isWaterQuality) {
            // ✅ prevent spam: skip if same water quality message appears within 2 minutes
            if (lastWaterQualityMessage == message && (now - lastWaterQualityTime) < 120_000) {
                Log.d("NotificationViewModel", "Skipping duplicate water quality notification.")
                return
            }
            lastWaterQualityMessage = message
            lastWaterQualityTime = now
        }

        val newRef = notifRef.push()
        val notif = mapOf(
            "id" to newRef.key,
            "message" to message,
            "time" to now,
            "read" to false
        )
        newRef.setValue(notif)
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification created successfully.")
            }
            .addOnFailureListener {
                Log.e("NotificationViewModel", "Failed to create notification: ${it.message}")
            }
    }


    fun markAllAsRead() {
        notifRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { child ->
                val isRead = child.child("read").getValue(Boolean::class.java) ?: true
                if (!isRead) {
                    child.ref.child("read").setValue(true)
                }
            }
        }.addOnFailureListener {
            Log.e("NotificationDebug", "Failed to mark notifications as read: ${it.message}")
        }
    }

    fun deleteNotification(notificationId: String) {
        notifRef.child(notificationId).removeValue()
            .addOnSuccessListener {
                Log.d("NotificationDebug", "Notification $notificationId deleted")
            }
            .addOnFailureListener {
                Log.e("NotificationDebug", "Failed to delete notification: ${it.message}")
            }
    }

    fun deleteAllNotifications() {
        notifRef.removeValue()
            .addOnSuccessListener {
                Log.d("NotificationDebug", "All notifications deleted")
            }
            .addOnFailureListener {
                Log.e("NotificationDebug", "Failed to delete all notifications: ${it.message}")
            }
    }

    fun cleanInvalidNotifications() {
        notifRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { child ->
                val time = when (val value = child.child("time").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: -1L
                    else -> -1L
                }

                if (time <= 0L) {
                    child.ref.removeValue()
                    Log.i("NotificationCleanup", "Deleted invalid notification: ${child.key}")
                }
            }
        }.addOnFailureListener {
            Log.e("NotificationCleanup", "Failed to clean notifications: ${it.message}")
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        notifRef.child(notificationId).child("read").setValue(true)
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification marked as read.")
            }
            .addOnFailureListener { error ->
                Log.e("NotificationViewModel", "Failed to mark as read: ${error.message}")
            }
    }

    fun loadNotifications() {
        if (listenerAdded && notificationsListener != null) {
            notifRef.removeEventListener(notificationsListener!!)
            listenerAdded = false
        }
        listenToNotifications()
    }

    // Time formatting helpers
    fun Long.toRelativeTime(): String {
        return DateUtils.getRelativeTimeSpanString(
            this,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    fun Long.toDateOnly(): String {
        val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return format.format(Date(this))
    }

    // Helper data class
    data class RecordKeyStatus(
        val key: String,
        val status: String,
        val timestamp: Long
    )
}
