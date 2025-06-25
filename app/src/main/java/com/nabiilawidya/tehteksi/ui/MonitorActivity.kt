package com.nabiilawidya.tehteksi.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.R
import com.nabiilawidya.tehteksi.adapter.MonitorAdapter
import com.nabiilawidya.tehteksi.data.Classification
import com.nabiilawidya.tehteksi.databinding.ActivityMonitorBinding

class MonitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitorBinding
    private lateinit var adapter: MonitorAdapter
    private val classificationList = mutableListOf<Classification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MonitorAdapter(classificationList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchAllClassifications()
    }

    private fun fetchAllClassifications() {
        val db = FirebaseFirestore.getInstance()

        db.collectionGroup("classifications").get()
            .addOnSuccessListener { snapshots ->
                classificationList.clear()

                val tasks = mutableListOf<Task<DocumentSnapshot>>()

                for (doc in snapshots) {
                    val data = doc.data
                    val label = data["label"] as? String ?: "-"
                    val imageUrl = data["imageUrl"] as? String ?: ""
                    val timestamp = data["timestamp"]?.toString() ?: "-"
                    val confidence = (data["confidence"] as? Number)?.toDouble() ?: 0.0
                    val location = data["location"] as? String ?: "-"
                    val path = doc.reference.path
                    val uid = path.split("/")[1]

                    val task = db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "Unknown"

                            val classification = Classification(
                                label = label,
                                imageUrl = imageUrl,
                                timestamp = timestamp,
                                confidence = confidence,
                                location = location,
                                userName = name
                            )

                            classificationList.add(classification)
                            adapter.notifyDataSetChanged()
                        }

                    tasks.add(task)
                }

                Tasks.whenAllComplete(tasks)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal ambil data: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
