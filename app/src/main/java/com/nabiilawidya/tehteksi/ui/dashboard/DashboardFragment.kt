package com.nabiilawidya.tehteksi.ui.dashboard

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.data.Disease
import com.nabiilawidya.tehteksi.databinding.FragmentDashboardBinding
import com.nabiilawidya.tehteksi.helper.TFLiteModelHelper
import com.nabiilawidya.tehteksi.ui.DiseaseActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var lastBitmap: Bitmap? = null
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        TFLiteModelHelper.init(requireContext())

        setupPermissions()
        setupLaunchers()
        observeViewModel()
        setupListeners()
    }

    private fun setupPermissions() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) openCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLaunchers() {
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                bitmap?.let { processImage(it) }
            } else {
                Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val imageStream = requireContext().contentResolver.openInputStream(uri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                processImage(selectedImage)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.classificationResult.observe(viewLifecycleOwner) { (label, confidence) ->
            binding.labelTextView.visibility = View.VISIBLE
            binding.confidenceTextView.visibility = View.VISIBLE
            binding.confidenceTextView.text = "%.2f%%".format(confidence)

            if (confidence < 70f) {
                binding.labelTextView.text = "Daun tidak teridentifikasi"
                binding.btnInfo.visibility = View.GONE
            } else {
                binding.labelTextView.text = label
                binding.saveButton.isEnabled = true

                val db = FirebaseFirestore.getInstance()
                db.collection("disease")
                    .whereEqualTo("nama", label)
                    .get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            binding.btnInfo.visibility = View.VISIBLE
                        } else {
                            binding.btnInfo.visibility = View.GONE
                        }
                    }
                    .addOnFailureListener {
                        binding.btnInfo.visibility = View.GONE
                    }
            }

        }

        viewModel.uploadState.observe(viewLifecycleOwner) {
            when (it) {
                is DashboardViewModel.UploadState.Loading -> {
                    binding.saveButton.isEnabled = false
                    binding.saveButton.text = "Uploading..."
                }
                is DashboardViewModel.UploadState.Success -> {
                    Toast.makeText(requireContext(), it.msg, Toast.LENGTH_SHORT).show()
                    binding.saveButton.isEnabled = true
                    binding.saveButton.text = "Simpan"
                }
                is DashboardViewModel.UploadState.Error -> {
                    Toast.makeText(requireContext(), "Upload gagal: ${it.error}", Toast.LENGTH_SHORT).show()
                    binding.saveButton.isEnabled = true
                    binding.saveButton.text = "Simpan"
                }
                else -> {
                    Log.w("UploadState", "Unhandled state: $it")
                }
            }
        }

    }

    private fun setupListeners() {
        binding.cameraButton.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.intentButton.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.saveButton.setOnClickListener {
            val location = binding.editTextLocation.text.toString().trim()

            if (location.isEmpty()) {
                binding.editTextLocation.error = "Lokasi wajib diisi"
                binding.editTextLocation.requestFocus()
                return@setOnClickListener
            }

            val confidence = viewModel.classificationResult.value?.second ?: 0f
            if (confidence < 70f) {
                Toast.makeText(requireContext(), "Gambar tidak bisa disimpan karena tidak teridentifikasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lastBitmap?.let { bitmap ->
                viewModel.uploadImage(bitmap, location, requireContext())
            } ?: Toast.makeText(requireContext(), "No image to upload!", Toast.LENGTH_SHORT).show()
        }
        binding.btnInfo.setOnClickListener {
            val label = viewModel.classificationResult.value?.first
            if (label.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Belum ada hasil klasifikasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("disease")
                .whereEqualTo("nama", label)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val document = result.documents[0]
                        val disease = document.toObject(Disease::class.java)
                        if (disease != null) {
                            val intent = Intent(requireContext(), DiseaseActivity::class.java)
                            intent.putExtra("disease", disease)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Data tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED -> openCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is needed to take pictures", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun processImage(bitmap: Bitmap) {
        binding.previewImageView.setImageBitmap(bitmap)
        lastBitmap = bitmap
        viewModel.classifyImage(bitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        TFLiteModelHelper.close()
    }
}
