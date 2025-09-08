package com.capstone.safehito.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.capstone.safehito.data.AuditLogService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WaterDataViewModel : ViewModel() {

    private val dbRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("waterData")
    private val auditLogService = AuditLogService()

    private val _ph = MutableStateFlow("0.0")
    val ph: StateFlow<String> = _ph

    private val _temperature = MutableStateFlow("0.0")
    val temperature: StateFlow<String> = _temperature

    private val _turbidity = MutableStateFlow("0.0")
    val turbidity: StateFlow<String> = _turbidity

    private val _dissolvedOxygen = MutableStateFlow("0.0")
    val dissolvedOxygen: StateFlow<String> = _dissolvedOxygen

    private val _waterLevel = MutableStateFlow("0.0")
    val waterLevel: StateFlow<String> = _waterLevel

    private val _waterStatus = MutableStateFlow("Normal")
    val waterStatus: StateFlow<String> = _waterStatus

    private val _fishStatus = MutableStateFlow("Loading...")
    val fishStatus: StateFlow<String> = _fishStatus

    private var lastProcessedFishStatusKey: String? = null

    // Add for each sensor type
    private val _phHistory = MutableStateFlow<List<Float>>(emptyList())
    val phHistory: StateFlow<List<Float>> = _phHistory

    private val _temperatureHistory = MutableStateFlow<List<Float>>(emptyList())
    val temperatureHistory: StateFlow<List<Float>> = _temperatureHistory

    private val _turbidityHistory = MutableStateFlow<List<Float>>(emptyList())
    val turbidityHistory: StateFlow<List<Float>> = _turbidityHistory

    private val _oxygenHistory = MutableStateFlow<List<Float>>(emptyList())
    val oxygenHistory: StateFlow<List<Float>> = _oxygenHistory

    private val _waterLevelHistory = MutableStateFlow<List<Float>>(emptyList())
    val waterLevelHistory: StateFlow<List<Float>> = _waterLevelHistory

    private var waterDataListener: ValueEventListener? = null

    init {
        startListening()
    }

    private fun startListening() {
        // Remove existing listener if any
        waterDataListener?.let { dbRef.removeEventListener(it) }
        
        waterDataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val phVal = snapshot.readDoubleFlexible("ph")
                val tempVal = snapshot.readDoubleFlexible("temperature")
                val turbidityVal = snapshot.readDoubleFlexible("turbidity")
                val oxygenVal = snapshot.readDoubleFlexible("oxygen")
                val levelVal = snapshot.readDoubleFlexible("waterLevel")

                viewModelScope.launch {
                    _ph.emit(formatForUi(phVal))
                    _temperature.emit(formatForUi(tempVal))
                    _turbidity.emit(formatForUi(turbidityVal))
                    _dissolvedOxygen.emit(formatForUi(oxygenVal))
                    _waterLevel.emit(formatForUi(levelVal))

                    if (phVal.isFiniteSafe()) {
                        _phHistory.emit((_phHistory.value + phVal.toFloat()).takeLast(20))
                    }
                    if (tempVal.isFiniteSafe()) {
                        _temperatureHistory.emit((_temperatureHistory.value + tempVal.toFloat()).takeLast(20))
                    }
                    if (turbidityVal.isFiniteSafe()) {
                        _turbidityHistory.emit((_turbidityHistory.value + turbidityVal.toFloat()).takeLast(20))
                    }
                    if (oxygenVal.isFiniteSafe()) {
                        _oxygenHistory.emit((_oxygenHistory.value + oxygenVal.toFloat()).takeLast(20))
                    }
                    if (levelVal.isFiniteSafe()) {
                        _waterLevelHistory.emit((_waterLevelHistory.value + levelVal.toFloat()).takeLast(20))
                    }

                    if (listOf(phVal, tempVal, turbidityVal, oxygenVal, levelVal).all { it.isFiniteSafe() }) {
                        evaluateWaterStatus(phVal, tempVal, turbidityVal, oxygenVal, levelVal)
                    } else {
                        Log.w("WaterViewModel", "Skipping status evaluation due to invalid value(s): ph=$phVal, temp=$tempVal, turbidity=$turbidityVal, oxygen=$oxygenVal, level=$levelVal")
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WaterViewModel", "Firebase error: ${error.message}")
            }
        }
        
        dbRef.addValueEventListener(waterDataListener!!)
    }

    fun refresh() {
        startListening()
    }

    private fun evaluateWaterStatus(
        ph: Double,
        temp: Double,
        turbidity: Double,
        oxygen: Double,
        waterLevel: Double
    ) {
        val issues = mutableListOf<String>()
        val oldStatus = _waterStatus.value

        // Check pH levels
        if (ph !in 6.5..8.5) {
            issues.add("pH")
            val severity = if (ph < 5.0 || ph > 10.0) AuditLogService.SEVERITY_CRITICAL else AuditLogService.SEVERITY_WARNING
            auditLogService.logWaterParameterAlert("pH", ph.toString(), "6.5-8.5", severity)
        }
        
        // Check temperature levels
        if (temp !in 24.0..30.0) {
            issues.add("Temperature")
            val severity = if (temp < 20.0 || temp > 35.0) AuditLogService.SEVERITY_CRITICAL else AuditLogService.SEVERITY_WARNING
            auditLogService.logWaterParameterAlert("Temperature", temp.toString(), "24.0-30.0°C", severity)
        }
        
        // Check turbidity levels
        if (turbidity > 125.0) {
            issues.add("Turbidity")
            val severity = if (turbidity > 100.0) AuditLogService.SEVERITY_WARNING else AuditLogService.SEVERITY_INFO
            auditLogService.logWaterParameterAlert("Turbidity", turbidity.toString(), "≤125.0 NTU", severity)
        }
        
        // Check oxygen levels
        if (oxygen < 3.5) {
            issues.add("Oxygen")
            val severity = if (oxygen < 2.5) AuditLogService.SEVERITY_CRITICAL else AuditLogService.SEVERITY_WARNING
            auditLogService.logWaterParameterAlert("Dissolved Oxygen", oxygen.toString(), "≥3.5 mg/L", severity)
        }
        
        // Check water level
        if (waterLevel < 20.0) {
            issues.add("Water Level")
            val severity = if (waterLevel < 10.0) AuditLogService.SEVERITY_CRITICAL else AuditLogService.SEVERITY_WARNING
            auditLogService.logWaterParameterAlert("Water Level", waterLevel.toString(), "≥20.0 cm", severity)
        }

        val newStatus = when {
            issues.isEmpty() -> "Normal"
            issues.size <= 2 -> "Caution"
            else -> "Warning"
        }

        // Log status change if it's different from previous status
        if (oldStatus != newStatus) {
            auditLogService.logWaterStatusChange(oldStatus, newStatus)
        }

        Log.d("WaterStatus", "New Status: $newStatus | Issues: $issues")

        viewModelScope.launch {
            _waterStatus.emit(newStatus)
        }
    }

    fun loadLatestFishStatus(uid: String) {
        FirebaseDatabase.getInstance()
            .getReference("waterData/fishStatus")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.e("FishStatus", "No data found.")
                        viewModelScope.launch {
                            _fishStatus.emit("No data")
                        }
                        return
                    }
                    for (child in snapshot.children) {
                        val status = child.child("status").getValue(String::class.java) ?: "Unknown"

                        Log.d("FishStatus", "Raw snapshot: ${child.value}")

                        // Emit status to UI
                        viewModelScope.launch {
                            _fishStatus.emit(status)
                        }

                        // No more notification logic here
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FishStatus", "Error: ${error.message}")
                }
            })
    }





}

// region Helpers
private fun DataSnapshot.readDoubleFlexible(key: String): Double {
    val node = this.child(key)
    val raw = node.getValue(Any::class.java)
    return when (raw) {
        is Number -> raw.toDouble()
        is String -> raw.toDoubleOrNull() ?: Double.NaN
        else -> Double.NaN
    }
}

private fun Double.isFiniteSafe(): Boolean {
    return !this.isNaN() && this != Double.POSITIVE_INFINITY && this != Double.NEGATIVE_INFINITY
}

private fun formatForUi(value: Double): String {
    return if (value.isFiniteSafe()) String.format("%.2f", value) else "--"
}
// endregion
