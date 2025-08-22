package com.capstone.safehito.model

data class AuditLog(
    val action: String = "",
    val performedBy: String = "",
    val timestamp: Long = 0L,
    val details: String = "",
    val category: String = "",
    val severity: String = "INFO",
    val userId: String = "",
    val ipAddress: String = "",
    val sessionId: String = ""
) 