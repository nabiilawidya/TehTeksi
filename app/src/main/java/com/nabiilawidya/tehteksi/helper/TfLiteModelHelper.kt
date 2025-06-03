package com.nabiilawidya.tehteksi.helper

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.nabiilawidya.tehteksi.helper.labels.teaLeafLabels

object TFLiteModelHelper {

    private lateinit var interpreter: Interpreter
    private val teaLabels: Array<String> = teaLeafLabels
    private const val MODEL_NAME = "MobileNetV2.tflite"
    private const val INPUT_SIZE = 224

    fun init(context: Context) {
        val model = loadModelFile(context, MODEL_NAME)
        interpreter = Interpreter(model)
    }

    fun classify(bitmap: Bitmap): Pair<String, Float> {
        val input = preprocess(bitmap, INPUT_SIZE)
        val output = Array(1) { FloatArray(teaLabels.size) }

        interpreter.run(input, output)

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = output[0][maxIdx] * 100

        return teaLabels[maxIdx] to confidence
    }

    fun close() {
        if (this::interpreter.isInitialized) interpreter.close()
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun preprocess(bitmap: Bitmap, inputSize: Int): ByteBuffer {
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
}
