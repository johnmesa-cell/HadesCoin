package com.example.hadescoin.presentation.auth.register

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.hadescoin.ui.theme.*
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraCaptureView(
    onDocumentCaptured: () -> Unit,
    onBack: () -> Unit
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture  by remember { mutableStateOf<ImageCapture?>(null) }
    var capturado     by remember { mutableStateOf(false) }
    var errorMsg      by remember { mutableStateOf<String?>(null) }

    HadesBackground {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Encabezado ---
            Text(
                text          = "VERIFICACIÓN",
                fontSize      = 22.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 4.sp,
                color         = HadesPurple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = "Encuadra tu cédula en el recuadro",
                fontSize      = 12.sp,
                color         = HadesCyan.copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Preview de la cámara o confirmación ---
            if (!capturado) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, HadesCyan, RoundedCornerShape(12.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val capture = ImageCapture.Builder().build()
                                imageCapture = capture
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        capture
                                    )
                                } catch (e: Exception) {
                                    Log.e("CameraCapture", "Error al iniciar cámara", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Marco guía para la cédula
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(240.dp)
                            .height(150.dp)
                            .border(2.dp, HadesOrange.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Botón capturar ---
                Button(
                    onClick = {
                        capturarFoto(
                            imageCapture  = imageCapture,
                            context       = context,
                            executor      = ContextCompat.getMainExecutor(context),
                            onSuccess     = { capturado = true },
                            onError       = { errorMsg = it }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HadesCyan,
                        contentColor   = HadesDark
                    ),
                    shape  = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "CAPTURAR DOCUMENTO",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        letterSpacing = 1.sp
                    )
                }

            } else {
                // --- Estado: capturado ---
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HadesDark)
                        .border(2.dp, HadesCyan, RoundedCornerShape(12.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint    = HadesCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text       = "Documento capturado ✅",
                        color      = HadesCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick  = onDocumentCaptured,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = HadesCyan,
                        contentColor   = HadesDark
                    ),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text          = "CONTINUAR REGISTRO",
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // --- Error de cámara ---
            errorMsg?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text(
                    text  = "Cancelar",
                    color = HadesOrange,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun capturarFoto(
    imageCapture: ImageCapture?,
    context: Context,
    executor: Executor,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val photoFile = File(context.cacheDir, "cedula_captura.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture?.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSuccess()
            }
            override fun onError(exception: ImageCaptureException) {
                onError("Error al capturar: ${exception.message}")
            }
        }
    ) ?: onError("Cámara no disponible")
}
