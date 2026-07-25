package com.rodrigonovoa.readlog.ui.addbook

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
internal fun BarcodeScanner(
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scanner = remember { BarcodeScanning.getClient() }
    val previewView = remember { PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    } }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )

    androidx.compose.runtime.DisposableEffect(lifecycleOwner, previewView) {
        var cameraProvider: ProcessCameraProvider? = null

        val listener = Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindBarcodeScanner(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                cameraProvider = cameraProvider!!,
                scanner = scanner,
                onBarcodeDetected = onBarcodeDetected,
            )
        }

        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProviderFuture.cancel(true)
            cameraProvider?.unbindAll()
        }
    }
}

private fun bindBarcodeScanner(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    scanner: BarcodeScanner,
    onBarcodeDetected: (String) -> Unit,
) {
    val preview = Preview.Builder()
        .build()
        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                BarcodeAnalyzer(scanner, onBarcodeDetected),
            )
        }

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis,
        )
    } catch (_: Exception) {
        // Camera binding failed (e.g. no back camera). Ignore silently.
    }
}

private class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onBarcodeDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private var isPaused = false

    override fun analyze(imageProxy: ImageProxy) {
        if (isPaused) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val isbn = barcodes
                    .firstOrNull { isValidIsbn(it) }
                    ?.displayValue

                if (!isbn.isNullOrBlank()) {
                    isPaused = true
                    onBarcodeDetected(isbn)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

private fun isValidIsbn(barcode: Barcode): Boolean {
    return barcode.valueType == Barcode.TYPE_ISBN && barcode.displayValue?.let { isValidIsbn(it) } == true
}

private fun isValidIsbn(value: String): Boolean {
    val cleaned = value.replace(Regex("[^0-9X]", RegexOption.IGNORE_CASE), "")
    return when (cleaned.length) {
        13 -> cleaned.matches(Regex("""(978|979)\d{10}"""))
        10 -> cleaned.matches(Regex("""\d{9}[0-9X]""", RegexOption.IGNORE_CASE))
        else -> false
    }
}
