package com.nabiilawidya.tehteksi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import java.text.SimpleDateFormat
import java.util.Locale

class MonitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitorBinding
    private lateinit var adapter: MonitorAdapter
    private val classificationList = mutableListOf<Classification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MonitorAdapter(classificationList) { selectedItem ->
            val intent = Intent(this, DetailMonitorActivity::class.java)
            intent.putExtra("classification", selectedItem)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupSpinner()
        fetchAllClassifications()
    }

    private fun setupSpinner() {
        val options = arrayOf(
            "Timestamp (Terbaru)",
            "Timestamp (Terlama)",
            "Lokasi (A-Z)",
            "Lokasi (Z-A)"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = spinnerAdapter

        binding.spinnerSort.setSelection(0)

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortData(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun sortData(position: Int) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        when (position) {
            0 -> classificationList.sortByDescending { sdf.parse(it.timestamp) }
            1 -> classificationList.sortBy { sdf.parse(it.timestamp) }
            2 -> classificationList.sortBy { it.location.lowercase(Locale.getDefault()) }
            3 -> classificationList.sortByDescending { it.location.lowercase(Locale.getDefault()) }
        }

        adapter.notifyDataSetChanged()
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
                    val timestampObj = data["timestamp"]
                    val timestamp = if (timestampObj is com.google.firebase.Timestamp) {
                        val date = timestampObj.toDate()
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        sdf.format(date)
                    } else {
                        "-"
                    }
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
                            sortData(binding.spinnerSort.selectedItemPosition)
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
