package com.nabiilawidya.tehteksi.ui.dashboard

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nabiilawidya.tehteksi.databinding.FragmentDashboardBinding
import com.nabiilawidya.tehteksi.helper.TFLiteModelHelper


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var lastBitmap: Bitmap? = null

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
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap -> bitmap?.let { processImage(it) } }

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

            if (confidence < 50f) {
                binding.labelTextView.text = "Daun tidak teridentifikasi"
            } else {
                binding.labelTextView.text = label
                binding.saveButton.isEnabled = true
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
            if (confidence < 50f) {
                Toast.makeText(requireContext(), "Gambar tidak bisa disimpan karena tidak teridentifikasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lastBitmap?.let { bitmap ->
                viewModel.uploadImage(bitmap, location, requireContext())
            } ?: Toast.makeText(requireContext(), "No image to upload!", Toast.LENGTH_SHORT).show()
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
        cameraLauncher.launch(null)
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
