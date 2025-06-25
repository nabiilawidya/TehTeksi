package com.nabiilawidya.tehteksi.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Classification(
    val label: String = "",
    val imageUrl: String = "",
    val timestamp: String = "",
    val userName: String = "",
    val confidence: Double = 0.0,
    val location: String = ""
): Parcelable
