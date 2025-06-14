package com.nabiilawidya.tehteksi.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class History(
    val id: String = "",
    val imageUrl: String = "",
    val label: String = "",
    val confidence: Double = 0.0,
    val location: String = "",
    val timestamp: Timestamp? = null
) : Parcelable
