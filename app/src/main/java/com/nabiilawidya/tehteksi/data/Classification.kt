package com.nabiilawidya.tehteksi.data

data class Classification(
    val label: String = "",
    val imageUrl: String = "",
    val timestamp: String = "",
    val userName: String = "",
    val confidence: Double = 0.0,
    val location: String = ""
)
