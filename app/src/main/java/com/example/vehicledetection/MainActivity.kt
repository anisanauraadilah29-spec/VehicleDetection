package com.example.vehicledetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView

    private lateinit var detector: Detector

    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val CAMERA_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        val statusDot = findViewById<ImageView>(R.id.ivStatusDot)
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.anim_pulse)
        statusDot.startAnimation(pulseAnim)

        detector = Detector(this)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Langsung cek izin kamera saat aplikasi dibuka untuk deteksi realtime
        checkCameraPermission()

    }

    private fun checkCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == CAMERA_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            Toast.makeText(
                this,
                "Izin kamera ditolak",
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            val preview =
                Preview.Builder()
                    .build()

            preview.setSurfaceProvider(
                previewView.surfaceProvider
            )

            val imageAnalysis =
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor
            ) { imageProxy ->

                analyzeImage(imageProxy)

            }

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Gagal membuka kamera",
                    Toast.LENGTH_SHORT
                ).show()

                e.printStackTrace()

            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun analyzeImage(
        imageProxy: ImageProxy
    ) {

        val bitmap =
            ImageUtils.imageProxyToBitmap(imageProxy)

        val results =
            detector.detect(bitmap)

        runOnUiThread {

            overlayView.setResults(results, bitmap.width, bitmap.height)

        }

        imageProxy.close()

    }
    override fun onDestroy() {

        super.onDestroy()

        cameraExecutor.shutdown()

    }

}

