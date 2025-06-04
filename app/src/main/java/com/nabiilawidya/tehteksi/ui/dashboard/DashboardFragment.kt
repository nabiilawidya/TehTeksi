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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        TFLiteModelHelper.init(requireContext())

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) openCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            bitmap?.let { processImage(it) }
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

        viewModel.classificationResult.observe(viewLifecycleOwner) { (label, confidence) ->
            binding.labelTextView.visibility = View.VISIBLE
            binding.confidenceTextView.visibility = View.VISIBLE
            binding.labelTextView.text = label
            binding.confidenceTextView.text = "%.2f%%".format(confidence)
            lastBitmap?.let {
                // Kalau perlu, update UI lain kalau ada
            }
        }

        // Observe upload state
        viewModel.uploadState.observe(viewLifecycleOwner) {
            when (it) {
                is UploadState.Loading -> {
                    binding.saveButton.isEnabled = false
                    binding.saveButton.text = "Uploading..."
                }
                is UploadState.Success -> {
                    Toast.makeText(requireContext(), it.msg, Toast.LENGTH_SHORT).show()
                    binding.saveButton.isEnabled = true
                    binding.saveButton.text = "Simpan"
                }
                is UploadState.Error -> {
                    Toast.makeText(requireContext(), "Upload gagal: ${it.error}", Toast.LENGTH_SHORT).show()
                    binding.saveButton.isEnabled = true
                    binding.saveButton.text = "Simpan"
                }
            }
        }

        binding.cameraButton.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.intentButton.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.saveButton.setOnClickListener {
            lastBitmap?.let { bitmap ->
                val location = binding.editTextLocation.text.toString().trim()
                viewModel.uploadImage(bitmap, location, requireContext())
            } ?: run {
                Toast.makeText(requireContext(), "No image to upload!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> openCamera()

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
