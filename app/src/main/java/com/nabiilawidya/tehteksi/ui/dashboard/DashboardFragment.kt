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
import com.nabiilawidya.tehteksi.databinding.FragmentDashboardBinding
import com.nabiilawidya.tehteksi.helper.TFLiteModelHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

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

        binding.cameraButton.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.intentButton.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.saveButton.setOnClickListener {
            lastBitmap?.let { bitmap ->
                uploadImageToCloudinary(bitmap)
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

        val (predictedLabel, confidence) = TFLiteModelHelper.classify(bitmap)

        binding.labelTextView.visibility = View.VISIBLE
        binding.confidenceTextView.visibility = View.VISIBLE
        binding.labelTextView.text = predictedLabel
        binding.confidenceTextView.text = String.format("%.2f%%", confidence)

        lastBitmap = bitmap
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap) {
        binding.saveButton.isEnabled = false
        binding.saveButton.text = "Uploading..."

        val file = File(requireContext().cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", "tehteksi")
            .addFormDataPart("folder", "tehteksi_classification")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/ds9wifmn8/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
                    binding.saveButton.isEnabled = true
                    binding.saveButton.text = "Simpan"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val imageUrl = JSONObject(body).getString("secure_url")
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Upload Success", Toast.LENGTH_SHORT).show()
                        binding.saveButton.isEnabled = true
                        Toast.makeText(requireContext(), "Uploaded to Cloudinary", Toast.LENGTH_SHORT).show()
                        binding.saveButton.text = "Simpan"

                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
                        binding.saveButton.isEnabled = true
                        binding.saveButton.text = "Simpan"
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        TFLiteModelHelper.close()
    }
}
