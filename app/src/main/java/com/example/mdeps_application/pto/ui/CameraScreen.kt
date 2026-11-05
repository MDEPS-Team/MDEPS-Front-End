@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mdeps_application.R
import java.io.File
import java.util.UUID
import java.util.concurrent.Executor

private const val CAMERA_LOG_TAG = "PtoCameraScreen"

@Composable
fun CameraScreen(
    onPhotoCaptured: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val captureDescription = stringResource(R.string.pto_a11y_capture_evidence_photo)
    val closeDescription = stringResource(R.string.pto_a11y_close_camera)

    DisposableEffect(lifecycleOwner) {
        onDispose {
            runCatching {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }.onFailure { error ->
                Log.w(CAMERA_LOG_TAG, "Camera unbind failed on dispose", error)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.pto_camera_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.semantics { contentDescription = closeDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = closeDescription,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener(
                        {
                            runCatching {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder()
                                    .build()
                                    .also { it.surfaceProvider = previewView.surfaceProvider }

                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()
                                imageCapture = capture

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture,
                                )
                            }.onFailure { error ->
                                Log.e(CAMERA_LOG_TAG, "Failed to bind camera use cases", error)
                            }
                        },
                        mainExecutor,
                    )
                },
            )

            FloatingActionButton(
                onClick = {
                    capturePhoto(
                        imageCapture = imageCapture,
                        cacheDir = context.cacheDir,
                        executor = mainExecutor,
                        onPhotoCaptured = onPhotoCaptured,
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .semantics { contentDescription = captureDescription },
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = captureDescription,
                )
            }
        }
    }
}

private fun capturePhoto(
    imageCapture: ImageCapture?,
    cacheDir: File,
    executor: Executor,
    onPhotoCaptured: (String) -> Unit,
) {
    val capture = imageCapture
    if (capture == null) {
        Log.w(CAMERA_LOG_TAG, "ImageCapture is not ready yet")
        return
    }

    val photoFile = File(cacheDir, "pto_evidence_${UUID.randomUUID()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    capture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uriString = Uri.fromFile(photoFile).toString()
                onPhotoCaptured(uriString)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(CAMERA_LOG_TAG, "Photo capture failed", exception)
            }
        },
    )
}
