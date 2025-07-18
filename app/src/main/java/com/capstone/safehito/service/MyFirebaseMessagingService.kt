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

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        /*val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val pushEnabled = prefs.getBoolean("push_enabled", true)

        if (!pushEnabled) {
            // User disabled notifications: do nothing
            return
        }

        val title = remoteMessage.data["title"] ?: "SafeHito Alert"
        val body = remoteMessage.data["body"] ?: "Something happened."

        showNotification(title, body)*/
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "my_channel"

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
