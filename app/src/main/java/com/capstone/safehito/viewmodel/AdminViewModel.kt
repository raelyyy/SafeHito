package com.capstone.safehito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.safehito.data.FirebaseRepository
import com.capstone.safehito.data.AuditLogService
import com.capstone.safehito.model.User
import com.capstone.safehito.model.AuditLog
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: FirebaseRepository = FirebaseRepository(),
    private val auditLogService: AuditLogService = AuditLogService()
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _totalScans = MutableStateFlow(0)
    val totalScans: StateFlow<Int> = _totalScans.asStateFlow()

    private val _totalInfected = MutableStateFlow(0)
    val totalInfected: StateFlow<Int> = _totalInfected.asStateFlow()

    init {
        refreshUsers()
        refreshAuditLogs()

        // Clean old logs periodically (every 24 hours)
        viewModelScope.launch {
            kotlinx.coroutines.delay(24 * 60 * 60 * 1000L) // 24 hours
            auditLogService.cleanOldLogs()
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collectLatest { userList ->
                _users.value = userList
            }
        }
    }

    fun refreshAuditLogs() {
        viewModelScope.launch {
            repository.getAuditLogs().collectLatest { logs ->
                _auditLogs.value = logs
            }
        }
    }

    fun clearAllAuditLogs(onComplete: (Boolean) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("audit_logs")
        ref.removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun addUser(
        fullName: String,
        email: String,
        contactNumber: String,
        role: String,
        performedBy: String = "Admin",
        onComplete: (Boolean) -> Unit
    ) {
        repository.addUser(fullName, email, contactNumber, role) { success ->
            if (success) {
                refreshUsers()
                auditLogService.logUserCreated(performedBy, email, fullName)
                refreshAuditLogs()
            }
            onComplete(success)
        }
    }

    fun editUser(
        user: User,
        performedBy: String = "Admin",
        onComplete: (Boolean) -> Unit
    ) {
        repository.editUser(user.id, user) { success ->
            if (success) {
                refreshUsers()
                auditLogService.logUserUpdated(performedBy, user.email, user.fullName)
                refreshAuditLogs()
            }
            onComplete(success)
        }
    }

    fun removeUser(
        user: User,
        performedBy: String,
        onComplete: (Boolean) -> Unit
    ) {
        repository.removeUser(user.id) { success ->
            if (success) {
                refreshUsers()
                auditLogService.logUserDeleted(performedBy, user.email, user.fullName)
                refreshAuditLogs()
            }
            onComplete(success)
        }
    }

    fun logAuditAction(action: String, performedBy: String) {
        auditLogService.logAdminAction(
            performedBy,
            action,
            "Admin action logged via legacy method"
        )
        refreshAuditLogs()
    }

    fun fetchTotalScans() {
        viewModelScope.launch {
            val total = repository.fetchTotalScans()
            _totalScans.value = total
        }
    }

    fun fetchTotalInfected() {
        viewModelScope.launch {
            val infected = repository.fetchTotalInfectedScans()
            _totalInfected.value = infected
        }
    }

    fun exportLogsToCSV(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val csvContent = buildString {
                    appendLine("Timestamp,Action,PerformedBy,Details,Category,Severity,UserId,SessionId")
                    auditLogs.value.forEach { log ->
                        appendLine(
                            "${log.timestamp},${log.action},${log.performedBy}," +
                                    "${log.details},${log.category},${log.severity}," +
                                    "${log.userId},${log.sessionId}"
                        )
                    }
                }
                onComplete(csvContent)
                auditLogService.logAdminAction(
                    "Admin",
                    "Logs Exported",
                    "Exported ${auditLogs.value.size} audit logs to CSV"
                )
                refreshAuditLogs()
            } catch (e: Exception) {
                onComplete("Error exporting logs: ${e.message}")
            }
        }
    }
}
