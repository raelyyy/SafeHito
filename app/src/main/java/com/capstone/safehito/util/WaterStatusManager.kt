package com.capstone.safehito.util

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.capstone.safehito.R
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WaterStatusManager(private val context: Context, private val userId: String) {

    private val database = FirebaseDatabase.getInstance()
    private val waterDataRef = database.getReference("waterData")
    private val waterStatusRef = database.getReference("waterStatus")
    private val notificationsRef = database.getReference("notifications").child(userId)
    private val _fishStatus = MutableStateFlow("Loading...")
    val fishStatus: StateFlow<String> = _fishStatus

    private var lastStatus: String? = null
    private var lastTriggeredParams: Set<String> = emptySet()

    fun startMonitoring() {
        waterDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ph = snapshot.child("ph").getValue(Double::class.java) ?: return
                val temperature = snapshot.child("temperature").getValue(Double::class.java) ?: return
                val oxygen = snapshot.child("oxygen").getValue(Double::class.java) ?: return
                val turbidity = snapshot.child("turbidity").getValue(Double::class.java) ?: return
                val waterLevel = snapshot.child("waterLevel").getValue(Double::class.java) ?: return

                val (newStatus, triggeredParams, paramStates) = evaluateWaterStatusWithDetails(
                    ph, temperature, oxygen, turbidity, waterLevel
                )

                val newTriggeredSet = triggeredParams.toSet()
                val newlyTriggered = newTriggeredSet - lastTriggeredParams
                val resolvedParams = lastTriggeredParams - newTriggeredSet

                if (newStatus != lastStatus) {
                    waterStatusRef.setValue(newStatus)
                }

                // 🔔 PUSH NOTIFICATION LOGIC
                if ((newStatus != lastStatus && newStatus != "Normal") || newlyTriggered.isNotEmpty()) {
                    val msg = if (newlyTriggered.isNotEmpty()) {
                        "Water status changed to $newStatus due to: ${newlyTriggered.joinToString(", ")}."
                    } else {
                        "Water status changed to $newStatus."
                    }
                    saveNotification(msg)
                }

                // ✔ Resolved Parameters
                val confirmedResolved = resolvedParams.mapNotNull { param ->
                    val name = param.substringBefore(" (")
                    if (paramStates[name] == true) {
                        when (name) {
                            "pH" -> "pH ($ph)"
                            "Temperature" -> "Temperature (${temperature}°C)"
                            "Oxygen" -> "Oxygen (${oxygen} mg/L)"
                            "Turbidity" -> "Turbidity (${turbidity} NTU)"
                            "Water Level" -> "Water Level (${waterLevel} cm)"
                            else -> null
                        }
                    } else null
                }

                if (confirmedResolved.isNotEmpty() && newStatus == "Normal") {
                    val msg = "Water parameters back to normal: ${confirmedResolved.joinToString(", ")}."
                    saveNotification(msg)
                }

                lastStatus = newStatus
                lastTriggeredParams = newTriggeredSet
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Returns:
     *  - status: Normal / Caution / Warning
     *  - triggeredParams: List of parameter names with values
     *  - paramStates: Map<String, Boolean> where true = normal, false = out of range
     */
    private fun evaluateWaterStatusWithDetails(
        ph: Double,
        temperature: Double,
        oxygen: Double,
        turbidity: Double,
        waterLevel: Double
    ): Triple<String, List<String>, Map<String, Boolean>> {
        val triggeredParams = mutableListOf<String>()
        val paramStates = mutableMapOf<String, Boolean>()

        // ✅ Individual checks
        val phNormal = ph in 6.5..8.5
        paramStates["pH"] = phNormal
        if (!phNormal) triggeredParams.add("pH ($ph)")

        val tempNormal = temperature in 24.0..30.0
        paramStates["Temperature"] = tempNormal
        if (!tempNormal) triggeredParams.add("Temperature ($temperature°C)")

        val oxygenNormal = oxygen >= 4.0
        paramStates["Oxygen"] = oxygenNormal
        if (!oxygenNormal) triggeredParams.add("Oxygen ($oxygen mg/L)")

        val turbidityNormal = turbidity <= 50.0
        paramStates["Turbidity"] = turbidityNormal
        if (!turbidityNormal) triggeredParams.add("Turbidity ($turbidity NTU)")

        val waterLevelNormal = waterLevel >= 20.0
        paramStates["Water Level"] = waterLevelNormal
        if (!waterLevelNormal) {
            val levelStatus = when {
                waterLevel >= 40.0 -> "Excellent"
                waterLevel >= 30.0 -> "Good"
                waterLevel >= 20.0 -> "Sufficient"
                waterLevel >= 10.0 -> "Low"
                else -> "Critical"
            }
            triggeredParams.add("Water Level ($waterLevel cm - $levelStatus)")
        }

        val failedCount = triggeredParams.size

        // ✅ Status based on number of failed checks and critical conditions
        val status = when {
            failedCount == 0 -> "Normal"
            failedCount <= 2 -> "Caution"
            else -> "Warning"
        }

        return Triple(status, triggeredParams, paramStates)
    }




    private fun saveNotification(message: String) {
        val notificationId = notificationsRef.push().key ?: return
        val timestamp = System.currentTimeMillis()
        val notification = mapOf(
            "id" to notificationId,
            "message" to message,
            "time" to timestamp,
            "read" to false
        )
        notificationsRef.child(notificationId).setValue(notification)
        showLocalPushNotification(message)
    }

    private fun showLocalPushNotification(message: String) {
        val builder = NotificationCompat.Builder(context, "my_channel")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your real icon
            .setContentTitle("SafeHito Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

}
