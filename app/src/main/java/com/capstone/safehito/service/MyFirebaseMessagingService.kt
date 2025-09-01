package com.capstone.safehito.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.capstone.safehito.MainActivity
import com.capstone.safehito.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val CHANNEL_ID = "safehito_channel"
        private const val CHANNEL_NAME = "SafeHito Alerts"
        private const val CHANNEL_DESCRIPTION = "Important alerts and notifications from SafeHito"
        
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                }
                
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
        
        fun showLocalNotification(context: Context, title: String, message: String, isImportant: Boolean = true) {
            try {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(if (isImportant) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibration pattern for important notifications
                
                if (isImportant) {
                    builder.setDefaults(NotificationCompat.DEFAULT_ALL)
                }
                
                val notificationId = System.currentTimeMillis().toInt()
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
                
                Log.d("NotificationService", "Local notification sent: $title - $message")
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to show local notification: ${e.message}")
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if user has enabled push notifications
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val pushEnabled = prefs.getBoolean("push_enabled", true)

        if (!pushEnabled) {
            Log.d("NotificationService", "Push notifications disabled by user")
            return
        }

        val title = remoteMessage.data["title"] ?: "SafeHito Alert"
        val body = remoteMessage.data["body"] ?: "Something happened."
        val isImportant = remoteMessage.data["important"]?.toBoolean() ?: true

        // Only show important notifications
        if (isImportant) {
            showLocalNotification(this, title, body, isImportant)
        } else {
            Log.d("NotificationService", "Skipping non-important push notification: $body")
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NotificationService", "New FCM token: $token")
        // You can send this token to your server here
    }
}
