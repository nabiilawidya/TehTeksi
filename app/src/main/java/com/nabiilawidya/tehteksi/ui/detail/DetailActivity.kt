package com.nabiilawidya.tehteksi.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.data.History
import com.nabiilawidya.tehteksi.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var history: History

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        history = intent.getParcelableExtra("history_data")!!

        showDetail()

        binding.btnDelete.setOnClickListener {
            deleteHistory()
        }
    }

    private fun showDetail() {
        binding.tvLabel.text = history.label
        binding.tvConfidence.text = "%.2f%%".format(history.confidence)
        binding.tvLocation.text = history.location
        binding.tvTimestamp.text = history.timestamp?.toDate().toString()

        Glide.with(this)
            .load(history.imageUrl)
            .into(binding.imgDetail)
    }

    private fun deleteHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("classifications")
            .document(history.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal hapus: ${it.message}", Toast.LENGTH_LONG).show()
                it.printStackTrace()
            }
    }

}
