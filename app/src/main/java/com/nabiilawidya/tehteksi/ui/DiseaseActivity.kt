package com.nabiilawidya.tehteksi.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.adapter.SolusiAdapter
import com.nabiilawidya.tehteksi.data.Disease
import com.nabiilawidya.tehteksi.data.SolusiItem
import com.nabiilawidya.tehteksi.databinding.ActivityDiseaseBinding

class DiseaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiseaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDiseaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val disease = intent.getParcelableExtra<Disease>("disease") ?: return

        binding.tvNama.text = disease.nama
        binding.tvDeskripsi.text = disease.deskripsi

        Glide.with(this)
            .load(disease.gambar_url)
            .into(binding.imgFull)

        val adapter = SolusiAdapter(disease.solusi)
        binding.recyclerViewSolusi.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSolusi.adapter = adapter
    }

}
