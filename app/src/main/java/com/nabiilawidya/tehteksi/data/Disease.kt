package com.nabiilawidya.tehteksi.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SolusiItem(
    val judul: String = "",
    val detail: String = ""
) : Parcelable

@Parcelize
data class Disease(
    val nama: String = "",
    val deskripsi: String = "",
    val solusi: List<SolusiItem> = emptyList(),
    val gambar_url: String = ""
) : Parcelable
