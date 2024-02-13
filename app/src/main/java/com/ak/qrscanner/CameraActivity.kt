package com.ak.qrscanner

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.ak.qrscanner.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(),ImageAnalysis.Analyzer {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: Camera
    private var flash = false

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            scanBarcode(imageUri = uri)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview: Preview = Preview.Builder()
                .build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor, this)

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)

        }, ContextCompat.getMainExecutor(this))

        binding.iconFlash.setOnClickListener {
            camera.cameraControl.enableTorch(!flash)
            flash = !flash
        }

        binding.iconGallery.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.icnCloseScanner.setOnClickListener {
            finish()
        }


    }

    override fun analyze(imageProxy: ImageProxy) {
        scanBarcode(imageProxy = imageProxy)
    }

    override fun onPause() {
        super.onPause()
        flash = false
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun scanBarcode(imageProxy: ImageProxy?=null, imageUri: Uri?=null) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()
        val scanner = BarcodeScanning.getClient(options)

        imageProxy?.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnCompleteListener {
                    imageProxy.close()
                    if (it.isSuccessful) {
                        readBarcodeData(it.result as List<Barcode>)
                    } else {
                        it.exception?.printStackTrace()
                    }
                }
        }
        imageUri?.let { uri->
            val inputImage = InputImage.fromFilePath(this,uri)
            scanner.process(inputImage)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        readBarcodeData(it.result as List<Barcode>)
                    } else {
                        it.exception?.printStackTrace()
                    }
                }
        }
    }

    private fun readBarcodeData(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            val barcode = barcodes[barcodes.lastIndex]
            when (barcode.valueType) {
                Barcode.TYPE_URL -> {
                    try {
                        binding.txtScanResult.text = barcode.url?.url.toString()
                    } catch (e: Exception) {
                    }
                }
                else -> {
                    try {
                        binding.txtScanResult.text = barcode.displayValue.toString()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}