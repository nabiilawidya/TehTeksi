package com.nabiilawidya.tehteksi.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.data.Disease

class HomeViewModel : ViewModel() {
    private val _penyakitList = MutableLiveData<List<Disease>>()
    val penyakitList: LiveData<List<Disease>> get() = _penyakitList

    init {
        loadPenyakit()
    }

    private fun loadPenyakit() {
        FirebaseFirestore.getInstance().collection("disease")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { it.toObject(Disease::class.java) }
                _penyakitList.value = list
            }
            .addOnFailureListener {
                Log.e("ViewModel", "Gagal ambil data: ${it.message}")
            }
    }
}