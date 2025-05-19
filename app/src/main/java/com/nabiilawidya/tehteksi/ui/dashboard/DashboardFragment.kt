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
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var model: Interpreter
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = Interpreter(loadModelFile())

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                binding.labelTextView.text = "Camera permission denied"
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            bitmap?.let {
                processImage(it)
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

        binding.cameraButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        binding.intentButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                binding.labelTextView.text = "Camera permission is needed to take pictures"
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun processImage(bitmap: Bitmap) {
        binding.previewImageView.setImageBitmap(bitmap)

        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(7) }

        model.run(input, output)

        for (i in output[0].indices) {
            println("Output[$i] = ${output[0][i]}")
        }

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val labels = arrayOf(
            "Brown Blight",
            "Gray Blight",
            "Green Mirid Bug",
            "Helopeltis",
            "Red spider",
            "Tea Algal Leaf Spot",
            "Healthy Leaf"
        )
        val confidenceValue = output[0][maxIdx] * 100
        println("Max index: $maxIdx, Confidence: $confidenceValue")

        binding.labelTextView.visibility = View.VISIBLE
        binding.confidenceTextView.visibility = View.VISIBLE
        binding.labelTextView.text = labels[maxIdx]
        binding.confidenceTextView.text = String.format("%.2f%%", confidenceValue)
    }


    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224
        val inputImage = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputImage.order(ByteOrder.nativeOrder())

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = scaledBitmap.getPixel(x, y)

                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                inputImage.putFloat(r)
                inputImage.putFloat(g)
                inputImage.putFloat(b)
            }
        }

        inputImage.rewind()
        return inputImage
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = requireContext().assets.openFd("MobileNetV2.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        model.close()
    }
}
