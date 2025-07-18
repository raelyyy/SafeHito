package com.capstone.safehito.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WaterDataViewModel : ViewModel() {

    private val dbRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("waterData")

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


    init {
        startListening()
    }

    private fun startListening() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val phVal = snapshot.child("ph").getValue(Double::class.java) ?: 0.0
                val tempVal = snapshot.child("temperature").getValue(Double::class.java) ?: 0.0
                val turbidityVal = snapshot.child("turbidity").getValue(Double::class.java) ?: 0.0
                val oxygenVal = snapshot.child("oxygen").getValue(Double::class.java) ?: 0.0
                val levelVal = snapshot.child("waterLevel").getValue(Double::class.java) ?: 0.0

                viewModelScope.launch {
                    _ph.emit(phVal.toString())
                    _temperature.emit(tempVal.toString())
                    _turbidity.emit(turbidityVal.toString())
                    _dissolvedOxygen.emit(oxygenVal.toString())
                    _waterLevel.emit(levelVal.toString())

                    _phHistory.emit((_phHistory.value + phVal.toFloat()).takeLast(20))
                    _temperatureHistory.emit((_temperatureHistory.value + tempVal.toFloat()).takeLast(20))
                    _turbidityHistory.emit((_turbidityHistory.value + turbidityVal.toFloat()).takeLast(20))
                    _oxygenHistory.emit((_oxygenHistory.value + oxygenVal.toFloat()).takeLast(20))

                    evaluateWaterStatus(phVal, tempVal, turbidityVal, oxygenVal, levelVal)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WaterViewModel", "Firebase error: ${error.message}")
            }
        })
    }

    private fun evaluateWaterStatus(
        ph: Double,
        temp: Double,
        turbidity: Double,
        oxygen: Double,
        waterLevel: Double
    ) {
        val issues = mutableListOf<String>()

        if (ph !in 6.5..8.5) issues.add("pH")
        if (temp !in 24.0..30.0) issues.add("Temperature")
        if (turbidity > 5.0) issues.add("Turbidity")
        if (oxygen < 4.0) issues.add("Oxygen")
        if (waterLevel < 18.0) issues.add("Water Level")

        val newStatus = when {
            issues.isEmpty() -> "Normal"
            issues.size <= 2 -> "Caution"
            else -> "Warning"
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
