package com.capstone.safehito.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.capstone.safehito.model.AuditLog
import java.util.*

class AuditLogService {
    companion object {
        private const val TAG = "AuditLogService"
        
        // Categories for different types of system events
        const val CATEGORY_AUTHENTICATION = "AUTHENTICATION"
        const val CATEGORY_USER_MANAGEMENT = "USER_MANAGEMENT"
        const val CATEGORY_SCANNING = "SCANNING"
        const val CATEGORY_SYSTEM = "SYSTEM"
        const val CATEGORY_WATER_MONITORING = "WATER_MONITORING"
        const val CATEGORY_ADMIN_ACTIONS = "ADMIN_ACTIONS"
        
        // Severity levels
        const val SEVERITY_INFO = "INFO"
        const val SEVERITY_WARNING = "WARNING"
        const val SEVERITY_ERROR = "ERROR"
        const val SEVERITY_CRITICAL = "CRITICAL"
    }
    
    private val database = FirebaseDatabase.getInstance().getReference("audit_logs")
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Log authentication events
     */
    fun logLogin(email: String, success: Boolean, details: String = "") {
        val log = AuditLog(
            action = if (success) "User Login" else "Login Failed",
            performedBy = email,
            timestamp = System.currentTimeMillis(),
            details = details.ifEmpty { if (success) "User successfully logged in" else "Login attempt failed" },
            category = CATEGORY_AUTHENTICATION,
            severity = if (success) SEVERITY_INFO else SEVERITY_WARNING,
            userId = email,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logLogout(email: String) {
        val log = AuditLog(
            action = "User Logout",
            performedBy = email,
            timestamp = System.currentTimeMillis(),
            details = "User logged out successfully",
            category = CATEGORY_AUTHENTICATION,
            severity = SEVERITY_INFO,
            userId = email,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logRegistration(email: String, fullName: String, success: Boolean, details: String = "") {
        val log = AuditLog(
            action = if (success) "User Registration" else "Registration Failed",
            performedBy = email,
            timestamp = System.currentTimeMillis(),
            details = details.ifEmpty { if (success) "New user registered: $fullName" else "User registration failed" },
            category = CATEGORY_AUTHENTICATION,
            severity = if (success) SEVERITY_INFO else SEVERITY_ERROR,
            userId = email,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logPasswordReset(email: String, success: Boolean, details: String = "") {
        val log = AuditLog(
            action = if (success) "Password Reset Requested" else "Password Reset Failed",
            performedBy = email,
            timestamp = System.currentTimeMillis(),
            details = details.ifEmpty { if (success) "Password reset email sent" else "Password reset failed" },
            category = CATEGORY_AUTHENTICATION,
            severity = if (success) SEVERITY_INFO else SEVERITY_WARNING,
            userId = email,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    /**
     * Log user management events
     */
    fun logUserCreated(adminEmail: String, newUserEmail: String, newUserName: String) {
        val log = AuditLog(
            action = "User Created",
            performedBy = adminEmail,
            timestamp = System.currentTimeMillis(),
            details = "Admin created new user: $newUserName ($newUserEmail)",
            category = CATEGORY_USER_MANAGEMENT,
            severity = SEVERITY_INFO, // ✅ creation is informational
            userId = adminEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }

    fun logUserUpdated(adminEmail: String, targetUserEmail: String, changes: String) {
        val log = AuditLog(
            action = "User Updated",
            performedBy = adminEmail,
            timestamp = System.currentTimeMillis(),
            details = "Admin updated user $targetUserEmail: $changes",
            category = CATEGORY_USER_MANAGEMENT,
            severity = SEVERITY_WARNING, // ✅ edits should stand out (orange)
            userId = adminEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }

    fun logUserDeleted(adminEmail: String, deletedUserEmail: String, deletedUserName: String) {
        val log = AuditLog(
            action = "User Deleted",
            performedBy = adminEmail,
            timestamp = System.currentTimeMillis(),
            details = "Admin deleted user: $deletedUserName ($deletedUserEmail)",
            category = CATEGORY_USER_MANAGEMENT,
            severity = SEVERITY_CRITICAL, // ✅ deletions are critical (red)
            userId = adminEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }


    /**
     * Log scanning events
     */
    fun logScanStarted(userEmail: String, scanType: String = "Fish Scan") {
        val log = AuditLog(
            action = "Scan Started",
            performedBy = userEmail,
            timestamp = System.currentTimeMillis(),
            details = "User initiated $scanType",
            category = CATEGORY_SCANNING,
            severity = SEVERITY_INFO,
            userId = userEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logScanCompleted(userEmail: String, result: String, confidence: Float, scanType: String = "Fish Scan") {
        val severity = when {
            result.contains("Healthy", ignoreCase = true) -> SEVERITY_INFO
            result.contains("No Fish", ignoreCase = true) -> SEVERITY_WARNING
            result.contains("Infected", ignoreCase = true) -> SEVERITY_WARNING
            else -> SEVERITY_INFO
        }
        
        val log = AuditLog(
            action = "Scan Completed",
            performedBy = userEmail,
            timestamp = System.currentTimeMillis(),
            details = "$scanType completed: $result (Confidence: ${(confidence * 100).toInt()}%)",
            category = CATEGORY_SCANNING,
            severity = severity,
            userId = userEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logScanFailed(userEmail: String, error: String, scanType: String = "Fish Scan") {
        val log = AuditLog(
            action = "Scan Failed",
            performedBy = userEmail,
            timestamp = System.currentTimeMillis(),
            details = "$scanType failed: $error",
            category = CATEGORY_SCANNING,
            severity = SEVERITY_ERROR,
            userId = userEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    /**
     * Log water monitoring events
     */
    fun logWaterParameterAlert(parameter: String, value: String, threshold: String, severity: String) {
        val log = AuditLog(
            action = "Water Parameter Alert",
            performedBy = "SYSTEM",
            timestamp = System.currentTimeMillis(),
            details = "$parameter alert: $value (Threshold: $threshold)",
            category = CATEGORY_WATER_MONITORING,
            severity = severity,
            userId = "SYSTEM",
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logWaterStatusChange(oldStatus: String, newStatus: String) {
        val severity = when {
            newStatus.contains("Warning", ignoreCase = true) -> SEVERITY_WARNING
            newStatus.contains("Critical", ignoreCase = true) -> SEVERITY_CRITICAL
            else -> SEVERITY_INFO
        }
        
        val log = AuditLog(
            action = "Water Status Changed",
            performedBy = "SYSTEM",
            timestamp = System.currentTimeMillis(),
            details = "Water status changed from '$oldStatus' to '$newStatus'",
            category = CATEGORY_WATER_MONITORING,
            severity = severity,
            userId = "SYSTEM",
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    /**
     * Log system events
     */
    fun logSystemEvent(event: String, details: String, severity: String = SEVERITY_INFO) {
        val log = AuditLog(
            action = event,
            performedBy = "SYSTEM",
            timestamp = System.currentTimeMillis(),
            details = details,
            category = CATEGORY_SYSTEM,
            severity = severity,
            userId = "SYSTEM",
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    fun logAdminAction(adminEmail: String, action: String, details: String, severity: String = SEVERITY_INFO) {
        val log = AuditLog(
            action = action,
            performedBy = adminEmail,
            timestamp = System.currentTimeMillis(),
            details = details,
            category = CATEGORY_ADMIN_ACTIONS,
            severity = severity,
            userId = adminEmail,
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    /**
     * Generic logging method
     */
    fun logCustomEvent(
        action: String,
        performedBy: String,
        details: String,
        category: String = CATEGORY_SYSTEM,
        severity: String = SEVERITY_INFO,
        userId: String = ""
    ) {
        val log = AuditLog(
            action = action,
            performedBy = performedBy,
            timestamp = System.currentTimeMillis(),
            details = details,
            category = category,
            severity = severity,
            userId = userId.ifEmpty { performedBy },
            sessionId = generateSessionId()
        )
        writeLog(log)
    }
    
    /**
     * Write log to Firebase
     */
    private fun writeLog(log: AuditLog) {
        try {
            val logRef = database.push()
            logRef.setValue(log)
                .addOnSuccessListener {
                    Log.d(TAG, "Audit log written successfully: ${log.action}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to write audit log: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing audit log: ${e.message}")
        }
    }
    
    /**
     * Generate a unique session ID
     */
    private fun generateSessionId(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }
    
    /**
     * Clean old logs (keep only last 1000 logs)
     */
    fun cleanOldLogs() {
        database.orderByChild("timestamp").limitToLast(1000).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.childrenCount > 1000) {
                    val logsToDelete = mutableListOf<String>()
                    snapshot.children.forEach { child ->
                        logsToDelete.add(child.key ?: "")
                    }
                    
                    // Delete oldest logs beyond the limit
                    val logsToRemove = logsToDelete.take(logsToDelete.size - 1000)
                    logsToRemove.forEach { key ->
                        database.child(key).removeValue()
                    }
                    
                    Log.d(TAG, "Cleaned ${logsToRemove.size} old audit logs")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to clean old logs: ${e.message}")
            }
    }
}
