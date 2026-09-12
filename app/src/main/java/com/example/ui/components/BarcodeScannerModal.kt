package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerModal(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            // User denied camera permission
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("barcode_scanner_dialog"),
            color = Color.Black
        ) {
            if (hasCameraPermission) {
                CameraScannerView(
                    onDismiss = onDismiss,
                    onBarcodeScanned = { rawCode ->
                        triggerScanFeedback(context)
                        onBarcodeScanned(rawCode)
                    }
                )
            } else {
                CameraPermissionDeniedView(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraScannerView(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX Preview View
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_ALL_FORMATS
                        )
                        .build()

                    val scanner = BarcodeScanning.getClient(options)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue ?: barcode.displayValue
                                        if (!rawValue.isNullOrBlank() && !hasScanned) {
                                            hasScanned = true
                                            // Extract cleaned card number or raw code
                                            val cleaned = extractRationCardNumber(rawValue)
                                            onBarcodeScanned(cleaned)
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("BarcodeScanner", "Barcode analysis error", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera
                    } catch (e: Exception) {
                        Log.e("BarcodeScanner", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder Reticle Overlay with Animated Laser Line
        ScannerOverlay()

        // Top Controls: Title & Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Scan Ration Card Barcode",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .testTag("close_scanner_button")
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close Scanner",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Controls: Torch Toggle & Instructions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Align Barcode / QR Code inside the square frame.\nIt scans automatically.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    lineHeight = 18.sp
                )
            }

            // Torch Button
            IconButton(
                onClick = {
                    isTorchOn = !isTorchOn
                    cameraControl?.cameraControl?.enableTorch(isTorchOn)
                },
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        if (isTorchOn) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.25f),
                        CircleShape
                    )
                    .testTag("toggle_torch_button")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isTorchOn) "Turn off flash" else "Turn on flash",
                    tint = if (isTorchOn) Color.Black else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val boxWidth = (canvasWidth * 0.76f).coerceAtMost(320.dp.toPx())
        val boxHeight = (boxWidth * 0.65f).coerceAtLeast(180.dp.toPx())

        val left = (canvasWidth - boxWidth) / 2f
        val top = (canvasHeight - boxHeight) / 2f - 40.dp.toPx()
        val right = left + boxWidth
        val bottom = top + boxHeight

        // Dark dim background around scanning box
        drawRect(
            color = Color.Black.copy(alpha = 0.55f),
            size = size
        )

        // Clear transparent cutout for viewfinder
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Glowing Blue/Green Border around viewfinder
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Corner Guides
        val cornerLen = 28.dp.toPx()
        val strokeW = 4.dp.toPx()
        val cornerColor = Color(0xFF00E676)

        // Top-Left Corner
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth = strokeW)

        // Top-Right Corner
        drawLine(cornerColor, Offset(right, top), Offset(right - cornerLen, top), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLen), strokeWidth = strokeW)

        // Bottom-Left Corner
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeWidth = strokeW)

        // Bottom-Right Corner
        drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth = strokeW)

        // Animated Red/Green Laser Line
        val currentLaserY = top + (boxHeight * laserPosition)
        drawLine(
            color = Color(0xFFFF5252),
            start = Offset(left + 8.dp.toPx(), currentLaserY),
            end = Offset(right - 8.dp.toPx(), currentLaserY),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}

@Composable
private fun CameraPermissionDeniedView(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "To scan ration card barcodes and QR codes directly with your camera, please grant camera permission.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel & Enter Manually")
                }
            }
        }
    }
}

/**
 * Triggers haptic vibration and subtle beep upon scanning a barcode.
 */
private fun triggerScanFeedback(context: Context) {
    try {
        // Haptic Vibration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }

        // Gentle Beep sound
        val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    } catch (e: Exception) {
        Log.e("BarcodeFeedback", "Error triggering feedback", e)
    }
}

/**
 * Parses raw barcode/QR code content to extract the cleanest Ration Card Number.
 */
fun extractRationCardNumber(raw: String): String {
    val trimmed = raw.trim()
    
    // If QR code contains XML or JSON (like Aadhaar/PDS QR code formats), extract card number pattern
    // e.g. "RC:12345678" or key-value format "cardNo=RC1234"
    if (trimmed.contains("cardNo=", ignoreCase = true)) {
        val regex = Regex("cardNo=([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE)
        val match = regex.find(trimmed)
        if (match != null) return match.groupValues[1].trim()
    }
    
    if (trimmed.contains("RC:", ignoreCase = true)) {
        val regex = Regex("RC:\\s*([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE)
        val match = regex.find(trimmed)
        if (match != null) return match.groupValues[1].trim()
    }

    // Otherwise return clean trimmed barcode string directly
    return trimmed
}
