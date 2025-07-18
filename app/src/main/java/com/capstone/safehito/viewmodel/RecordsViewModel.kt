package com.capstone.safehito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.safehito.model.Record
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class RecordsUiState(
    val selectedTab: String = "Hourly",
    val records: List<Record> = emptyList()
)

class RecordsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    init {
        loadRecords()
    }

    fun loadRecords() {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference("scan_history/$uid")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = snapshot.children.mapNotNull { it.getValue(Record::class.java) }
                    .sortedByDescending { it.timestamp }
                _uiState.update { current ->
                    current.copy(records = records)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: Log error if needed
            }
        })
    }

    fun selectTab(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
