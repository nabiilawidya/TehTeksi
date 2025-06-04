package com.nabiilawidya.tehteksi.ui.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nabiilawidya.tehteksi.helper.TFLiteModelHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val _classificationResult = MutableLiveData<Pair<String, Float>>()
    val classificationResult: LiveData<Pair<String, Float>> get() = _classificationResult

    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> get() = _uploadState

    fun classifyImage(bitmap: Bitmap) {
        val (label, confidence) = TFLiteModelHelper.classify(bitmap)
        _classificationResult.postValue(label to confidence)
    }

    fun uploadImage(
        bitmap: Bitmap,
        location: String,
        context: android.content.Context
    ) {
        _uploadState.postValue(UploadState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = saveBitmapToCache(bitmap, context)
                val imageUrl = uploadToCloudinary(file)
                val user = FirebaseAuth.getInstance().currentUser
                val label = _classificationResult.value?.first ?: "Unknown"
                val confidence = _classificationResult.value?.second?.toDouble() ?: 0.0

                if (user != null) {
                    saveToFirestore(user.uid, label, confidence, imageUrl, location)
                }

                _uploadState.postValue(UploadState.Success("Data saved to Firestore"))
            } catch (e: Exception) {
                _uploadState.postValue(UploadState.Error(e.message ?: "Unknown Error"))
            }
        }
    }


    private fun saveBitmapToCache(bitmap: Bitmap, context: android.content.Context): File {
        val file = File(context.cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    private fun uploadToCloudinary(file: File): String {
        val client = OkHttpClient()

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

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body?.string() ?: throw IOException("Empty response body")
            return JSONObject(body).getString("secure_url")
        }
    }

    private fun saveToFirestore(
        userId: String,
        label: String,
        confidence: Double,
        imageUrl: String,
        location: String
    ) {
        val roundedConfidence = String.format(Locale.US, "%.2f", confidence).toDouble()

        val data = hashMapOf(
            "label" to label,
            "confidence" to roundedConfidence,
            "imageUrl" to imageUrl,
            "location" to location,
            "timestamp" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("classifications")
            .add(data)
    }
}

sealed class UploadState {
    object Loading : UploadState()
    data class Success(val msg: String) : UploadState()
    data class Error(val error: String) : UploadState()
}
