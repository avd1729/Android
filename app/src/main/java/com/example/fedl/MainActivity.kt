package com.example.fedl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Buffer
import okio.Okio
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var downloadModelButton: Button
    private lateinit var trainLocallyButton: Button
    private lateinit var uploadUpdateButton: Button

    private val TAG = "FederatedLearningDemo"
    private val SERVER_URL = "http://10.0.2.2:5000" // Local server URL (for emulator)
    private val CLIENT_ID = UUID.randomUUID().toString()

    private var modelVersion: String? = null
    private var currentRound: Int = 0
    private var modelWeights: ByteArray? = null
    private var isModelTrained = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        downloadModelButton = findViewById(R.id.downloadModelButton)
        trainLocallyButton = findViewById(R.id.trainLocallyButton)
        uploadUpdateButton = findViewById(R.id.uploadUpdateButton)

        // Request internet permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.INTERNET), 1)
        }

        setupButtons()
        updateStatusView("Ready to start federated learning process.")
    }

    private fun setupButtons() {
        downloadModelButton.setOnClickListener {
            lifecycleScope.launch {
                downloadGlobalModel()
            }
        }

        trainLocallyButton.setOnClickListener {
            lifecycleScope.launch {
                trainModelLocally()
            }
        }

        uploadUpdateButton.setOnClickListener {
            lifecycleScope.launch {
                uploadModelUpdate()
            }
        }
    }

    private suspend fun downloadGlobalModel() {
        updateStatusView("Downloading global model...")

        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("$SERVER_URL/get_model")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to download model: ${response.code}")
                    }

                    val modelFile = File(filesDir, "model.pkl")
                    val sink = modelFile.sink().buffer()
                    sink.writeAll(response.body!!.source())
                    sink.close()

                    // Here we would deserialize the model data
                    // For demo purposes, we'll just pretend we did
                    modelVersion = UUID.randomUUID().toString() // Simulated version
                    currentRound = 1 // Simulated round
                    modelWeights = ByteArray(100) // Simulated weights
                    isModelTrained = false
                }
            }

            updateStatusView("Global model downloaded successfully. Version: $modelVersion, Round: $currentRound")
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            updateStatusView("Error downloading model: ${e.localizedMessage}")
        }
    }

    private suspend fun trainModelLocally() {
        if (modelWeights == null) {
            updateStatusView("Please download the global model first")
            return
        }

        updateStatusView("Training model locally...")

        // Simulate local training with random data
        try {
            withContext(Dispatchers.Default) {
                // Simulate training delay
                Thread.sleep(2000)
                // In a real app, we would:
                // 1. Load the model into TFLite
                // 2. Use local data to train/update the model
                // 3. Extract updated weights

                // Simulate updated weights
                modelWeights = ByteArray(100).apply { Random().nextBytes(this) }
                isModelTrained = true
            }

            updateStatusView("Local training completed. Ready to upload update.")
        } catch (e: Exception) {
            Log.e(TAG, "Error training model locally", e)
            updateStatusView("Error training model: ${e.localizedMessage}")
        }
    }

    private suspend fun uploadModelUpdate() {
        if (!isModelTrained) {
            updateStatusView("Please train the model locally first")
            return
        }

        updateStatusView("Uploading model update...")

        try {
            withContext(Dispatchers.IO) {
                // Create the update package
                val updateData = mapOf(
                    "model_version" to modelVersion,
                    "client_id" to CLIENT_ID,
                    "weights" to modelWeights,
                    "sample_size" to 1000 // Simulated sample size
                )

                // Serialize it to a byte array (in a real app we'd use proper serialization)
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(updateData.toString()) }
                val serializedData = baos.toByteArray()

                // Create a multipart request with the file
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "update.pkl",
                        RequestBody.create("application/octet-stream".toMediaTypeOrNull(), serializedData)
                    )
                    .build()

                val request = Request.Builder()
                    .url("$SERVER_URL/submit_update")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to upload update: ${response.code}")
                    }

                    // Reset the trained flag
                    isModelTrained = false
                }
            }

            updateStatusView("Model update uploaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading model update", e)
            updateStatusView("Error uploading update: ${e.localizedMessage}")
        }
    }

    private fun updateStatusView(status: String) {
        runOnUiThread {
            statusTextView.text = status
            Log.d(TAG, status)
        }
    }
}