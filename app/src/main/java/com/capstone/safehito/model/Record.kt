package com.capstone.safehito.model

data class Record(
    val image_url: String = "",
    val result: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = 0
)

