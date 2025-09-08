package com.capstone.safehito.viewmodel

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.safehito.model.Notification
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewModel(private val uid: String) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> get() = _notifications

    private val database = FirebaseDatabase.getInstance()
    private val notifRef = database.getReference("notifications").child(uid)

    private var listenerAdded = false
    private var lastProcessedFishStatusKey: String? = null
    private var lastFishInfectionKey: String? = null

    private var notificationsListener: ValueEventListener? = null
    private var fishStatusListener: ValueEventListener? = null

    private var lastWaterQualityMessage: String? = null
    private var lastWaterQualityTime: Long = 0L
    
    // Add duplicate prevention for fish infection notifications
    private var lastFishInfectionMessage: String? = null
    private var lastFishInfectionTime: Long = 0L

    // In-app dedup and rate limit
    private val dedupWindowMs = 30 * 60 * 1000L // 30 minutes for identical important message
    private val globalCooldownMs = 60 * 1000L // 60 seconds between created notifications
    private var lastAnyCreateTime: Long = 0L

    val unreadCount = notifications
        .map { list -> list.count { !it.read } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Single instance flags
    companion object {
        private var isListeningFishStatus = false
        private var activeViewModelCount = 0
        
        fun getActiveViewModelCount(): Int = activeViewModelCount
    }

    init {
        activeViewModelCount++
        Log.d("NotificationViewModel", "ViewModel created for UID: $uid. Active count: $activeViewModelCount")
        cleanInvalidNotifications()
        listenToNotifications()
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

                            val rawMessage = child.child("message").getValue(String::class.java)?.trim()
                            if (rawMessage.isNullOrEmpty()) {
                                Log.w("NotificationDebug", "Skipping notification $id because it has no message.")
                                return@mapNotNull null
                            }

                            val time = when (val value = child.child("time").value) {
                                is Long -> if (value > 0) value else System.currentTimeMillis()
                                is String -> value.toLongOrNull()?.takeIf { it > 0 } ?: System.currentTimeMillis()
                                else -> System.currentTimeMillis()
                            }

                            val read = child.child("read").getValue(Boolean::class.java) ?: false
                            val important = child.child("important").getValue(Boolean::class.java) ?: false

                            Notification(id, rawMessage, time, read, important)
                        } catch (e: Exception) {
                            Log.e("NotificationDebug", "Error parsing notification: ${e.message}")
                            null
                        }
                    }



                    // Check for new important notifications and show push notifications
                    val previousNotifications = _notifications.value
                    val newImportantNotifications = notifList.filter { notification ->
                        notification.important && 
                        !previousNotifications.any { it.id == notification.id }
                    }
                    
                    newImportantNotifications.forEach { notification ->
                        Log.d("NotificationViewModel", "New important notification detected: ${notification.message}")
                        // Show push notification for new important notifications
                        showPushNotificationForImportantNotification(notification)
                    }

                    // Show latest per unique message to avoid spamming the list with repeats
                    _notifications.value = notifList
                        .filter { it.message.isNotBlank() }
                        .sortedByDescending { it.time }
                        .distinctBy { it.message.trim().lowercase() }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationDebug", "Failed to load notifications: ${error.message}")
            }
        }

        notifRef.addValueEventListener(notificationsListener as ValueEventListener)
    }
    
    private fun showPushNotificationForImportantNotification(notification: com.capstone.safehito.model.Notification) {
        try {
            // Use the notification service to show push notification
            // This will be handled by the notification service when notifications are created
            Log.d("NotificationViewModel", "Important notification should trigger push: ${notification.message}")
        } catch (e: Exception) {
            Log.e("NotificationViewModel", "Failed to show push notification: ${e.message}")
        }
    }

    // Automatically start watching fish status (only once)
    fun watchFishStatusAndNotify() {
        if (isListeningFishStatus) {
            Log.d("NotificationViewModel", "Fish status listener already active for UID: $uid")
            return
        }
        Log.d("NotificationViewModel", "Starting fish status monitoring for UID: $uid")
        isListeningFishStatus = true

        val fishRef = FirebaseDatabase.getInstance()
            .getReference("users/$uid/scans")

        var isFirstLoad = true

        fishStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("NotificationViewModel", "Fish status onDataChange triggered for UID: $uid")
                val records = snapshot.children
                    .mapNotNull { snap ->
                        val key = snap.key ?: return@mapNotNull null
                        val status = snap.child("status").getValue(String::class.java) ?: ""
                        val timestamp = snap.child("timestamp").getValue(Long::class.java) ?: 0L
                        RecordKeyStatus(key, status, timestamp)
                    }
                    .sortedByDescending { it.timestamp }

                val mostRecent = records.firstOrNull()
                Log.d("NotificationViewModel", "Most recent record: ${mostRecent?.key} with status: ${mostRecent?.status}")

                if (isFirstLoad) {
                    isFirstLoad = false
                    lastProcessedFishStatusKey = mostRecent?.key
                    Log.d("NotificationViewModel", "First load - setting lastProcessedFishStatusKey to: $lastProcessedFishStatusKey")
                    return
                }

                mostRecent?.let { record ->
                    Log.d("NotificationViewModel", "Checking record: ${record.key} vs lastProcessed: $lastProcessedFishStatusKey vs lastFishInfection: $lastFishInfectionKey")
                    if (
                        record.key != lastProcessedFishStatusKey &&
                        record.key != lastFishInfectionKey &&
                        record.status.trim().equals("infected", ignoreCase = true)
                    ) {
                        Log.d("NotificationViewModel", "Creating infection notification for record: ${record.key}")
                        createNotification(
                            "Fish infection detected in the latest record. Immediate action is recommended."
                        )
                        lastProcessedFishStatusKey = record.key
                        lastFishInfectionKey = record.key
                        Log.d("NotificationViewModel", "Updated keys - lastProcessed: $lastProcessedFishStatusKey, lastFishInfection: $lastFishInfectionKey")
                    } else {
                        Log.d("NotificationViewModel", "Skipping notification - conditions not met")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationViewModel", "Failed to watch fish status for UID $uid: ${error.message}")
            }
        }

        fishRef.addValueEventListener(fishStatusListener as ValueEventListener)
    }

    private fun createNotification(message: String) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isEmpty()) {
            Log.w("NotificationViewModel", "Skipping notification creation because message is empty.")
            return
        }

        val now = System.currentTimeMillis()
        val lowerMsg = trimmedMessage.lowercase()

        val importantKeywords = listOf("infected", "infection", "fish infection", "critical", "emergency", "warning")
        val isImportant = importantKeywords.any { it in lowerMsg }

        if (!isImportant) {
            Log.d("NotificationViewModel", "Skipping non-important notification: $trimmedMessage")
            return
        }

        // Global cooldown to avoid bursts
        if (now - lastAnyCreateTime < globalCooldownMs) {
            Log.d("NotificationViewModel", "Global cooldown active, skipping create: $trimmedMessage")
            return
        }

        // Check recent duplicates with same message in Firebase within window
        val windowStart = now - dedupWindowMs
        notifRef.get().addOnSuccessListener { snapshot ->
            val hasRecentDuplicate = snapshot.children.any { child ->
                val msg = child.child("message").getValue(String::class.java)?.trim()?.lowercase()
                val time = when (val value = child.child("time").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: 0L
                    else -> 0L
                }
                msg == lowerMsg && time >= windowStart
            }

            if (hasRecentDuplicate) {
                Log.d("NotificationViewModel", "Duplicate important message within window, skipping create: $trimmedMessage")
                return@addOnSuccessListener
            }

            val newRef = notifRef.push()
            val notif = mapOf(
                "id" to newRef.key,
                "message" to trimmedMessage,
                "time" to now,
                "read" to false,
                "important" to isImportant
            )
            newRef.setValue(notif)
                .addOnSuccessListener {
                    lastAnyCreateTime = now
                    Log.d("NotificationViewModel", "Important notification created successfully.")
                }
                .addOnFailureListener {
                    Log.e("NotificationViewModel", "Failed to create notification: ${it.message}")
                }
        }.addOnFailureListener {
            Log.e("NotificationViewModel", "Failed to check duplicates: ${it.message}")
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
                Log.d("NotificationDebug", "Notification deleted successfully.")
            }
            .addOnFailureListener {
                Log.e("NotificationDebug", "Failed to delete notification: ${it.message}")
            }
    }

    fun deleteAllNotifications() {
        notifRef.removeValue()
            .addOnSuccessListener {
                Log.d("NotificationDebug", "All notifications deleted successfully.")
            }
            .addOnFailureListener {
                Log.e("NotificationDebug", "Failed to delete all notifications: ${it.message}")
            }
    }

    private val _hasSeen = MutableStateFlow(false)
    val hasSeen: StateFlow<Boolean> get() = _hasSeen

    fun markAsSeen() {
        _hasSeen.value = true
    }


    fun markNotificationAsRead(notificationId: String) {
        // Optimistic update
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(read = true) else it
        }

        notifRef.child(notificationId).child("read").setValue(true)
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification marked as read.")
            }
            .addOnFailureListener { error ->
                Log.e("NotificationViewModel", "Failed to mark as read: ${error.message}")
            }
    }


    fun loadNotifications() {
        // Notifications are automatically loaded via the listener
    }

    private fun cleanInvalidNotifications() {
        notifRef.get().addOnSuccessListener { snapshot ->
            val invalidNotifications = mutableListOf<String>()
            for (child in snapshot.children) {
                val id = child.child("id").getValue(String::class.java)
                val message = child.child("message").getValue(String::class.java)
                val time = child.child("time").getValue(Long::class.java)
                
                if (id.isNullOrEmpty() || message.isNullOrEmpty() || time == null) {
                    invalidNotifications.add(child.key ?: "")
                }
            }
            
            invalidNotifications.forEach { key ->
                notifRef.child(key).removeValue()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeViewModelCount--
        Log.d("NotificationViewModel", "ViewModel cleared for UID: $uid. Active count: $activeViewModelCount")
        
        // Clean up listeners when ViewModel is cleared
        notificationsListener?.let { listener ->
            notifRef.removeEventListener(listener)
        }
        fishStatusListener?.let { listener ->
            FirebaseDatabase.getInstance()
                .getReference("users/$uid/scans")
                .removeEventListener(listener)
        }
        
        // Reset static flags only if no more active ViewModels
        if (activeViewModelCount <= 0) {
            isListeningFishStatus = false
            lastFishInfectionKey = null
            Log.d("NotificationViewModel", "All ViewModels cleared, resetting static flags")
        }
    }
}

data class RecordKeyStatus(
    val key: String,
    val status: String,
    val timestamp: Long
)
