package com.nabiilawidya.tehteksi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nabiilawidya.tehteksi.data.Classification
import com.nabiilawidya.tehteksi.databinding.ActivityDetailMonitorBinding

class DetailMonitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMonitorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getParcelableExtra<Classification>("classification")
        if (data != null) {
            binding.tvLabel.text = data.label
            binding.tvUser.text = data.userName
            binding.tvConfidence.text = "${String.format("%.2f", data.confidence)}%"
            binding.tvLocation.text = data.location
            binding.tvTimestamp.text = data.timestamp

            Glide.with(this)
                .load(data.imageUrl)
                .into(binding.imgDetail)
        }
    }
}
