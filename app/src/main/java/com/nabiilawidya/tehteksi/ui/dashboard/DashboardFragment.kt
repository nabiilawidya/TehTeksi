package com.nabiilawidya.tehteksi.ui.dashboard

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nabiilawidya.tehteksi.databinding.FragmentDashboardBinding
import com.nabiilawidya.tehteksi.helper.TFLiteModelHelper


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var modelHelper: TFLiteModelHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TFLiteModelHelper.init(requireContext())

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) openCamera()
            else binding.labelTextView.text = "Camera permission denied"
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

        binding.cameraButton.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.intentButton.setOnClickListener { galleryLauncher.launch("image/*") }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> openCamera()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                binding.labelTextView.text = "Camera permission is needed to take pictures"
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

        val (predictedLabel, confidence) = TFLiteModelHelper.classify(bitmap)

        binding.labelTextView.visibility = View.VISIBLE
        binding.confidenceTextView.visibility = View.VISIBLE
        binding.labelTextView.text = predictedLabel
        binding.confidenceTextView.text = String.format("%.2f%%", confidence)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        TFLiteModelHelper.close() // Jangan lupa tutup interpreter
    }
}
