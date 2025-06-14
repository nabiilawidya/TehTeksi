package com.nabiilawidya.tehteksi.ui.history

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nabiilawidya.tehteksi.adapter.HistoryAdapter
import com.nabiilawidya.tehteksi.data.History
import com.nabiilawidya.tehteksi.databinding.ActivityHistoryBinding
import com.nabiilawidya.tehteksi.ui.detail.DetailActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val classificationList = mutableListOf<History>()
    private lateinit var adapter: HistoryAdapter

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadData() // refresh data setelah delete
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter(classificationList) { history ->
            openDetail(history)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadData()
    }

    private fun openDetail(history: History) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("history_data", history)
        detailLauncher.launch(intent)
    }

    private fun loadData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("classifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                classificationList.clear()
                for (doc in result) {
                    val item = doc.toObject(History::class.java)
                    classificationList.add(item)
                }
                adapter.updateData(classificationList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal ambil data", Toast.LENGTH_SHORT).show()
            }
    }
}
