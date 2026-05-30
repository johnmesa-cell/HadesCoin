package com.example.hadescoin.presentation.auth.register

import android.content.Context
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.hadescoin.presentation.components.HadesBackground
import com.example.hadescoin.ui.theme.*
import java.io.File
import java.util.concurrent.Executor

enum class CedulaSide {
    FRONTAL, TRASERA
}

@Composable
fun CameraCaptureView(
    side: CedulaSide,
    onCaptured: () -> Unit,
    onBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturado    by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    val stepNumber = if (side == CedulaSide.FRONTAL) 2 else 3
    val sideLabel  = if (side == CedulaSide.FRONTAL) "PARTE FRONTAL" else "PARTE TRASERA"
    val sideHint   = if (side == CedulaSide.FRONTAL)
        "Encuadra el frente de tu cédula (foto y nombre)"
    else
        "Encuadra la parte trasera de tu cédula (código de barras)"
    val fileName   = if (side == CedulaSide.FRONTAL) "cedula_frontal.jpg" else "cedula_trasera.jpg"

    HadesBackground {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(currentStep = stepNumber, totalSteps = 3)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text          = sideLabel,
                fontSize      = 20.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 4.sp,
                color         = HadesPurple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = sideHint,
                fontSize      = 12.sp,
                color         = HadesCyan.copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!capturado) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, HadesCyan, RoundedCornerShape(12.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build()
                                preview.surfaceProvider = previewView.surfaceProvider
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
                                    errorMsg = "Error al iniciar cámara: ${e.message}"
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(220.dp)
                            .height(140.dp)
                            .border(2.dp, HadesOrange.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick  = {
                        capturarFoto(
                            fileName     = fileName,
                            imageCapture = imageCapture,
                            context      = context,
                            executor     = ContextCompat.getMainExecutor(context),
                            onSuccess    = { capturado = true; errorMsg = null },
                            onError      = { errorMsg = it }
                        )
                    },
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = HadesCyan,
                        contentColor   = HadesBlack
                    ),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text          = "CAPTURAR",
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 14.sp,
                        letterSpacing = 1.sp
                    )
                }

            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HadesBlack)
                        .border(2.dp, HadesCyan, RoundedCornerShape(12.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector        = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint               = HadesCyan,
                        modifier           = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text       = "$sideLabel capturada ✅",
                        color      = HadesCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        textAlign  = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick  = onCaptured,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = HadesCyan,
                        contentColor   = HadesBlack
                    ),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text          = if (side == CedulaSide.FRONTAL) "CONTINUAR AL PASO 3" else "FINALIZAR VERIFICACIÓN",
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 14.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick  = { capturado = false; errorMsg = null },
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = HadesOrange),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, HadesOrange),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text       = "Retomar foto",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp
                    )
                }
            }

            errorMsg?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text      = it,
                    color     = MaterialTheme.colorScheme.error,
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBack) {
                Text(text = "Cancelar", color = HadesOrange, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step     = index + 1
            val isActive = step == currentStep
            val isDone   = step < currentStep
            val color    = when {
                isDone   -> HadesCyan
                isActive -> HadesOrange
                else     -> HadesOnDark.copy(alpha = 0.3f)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(if (isActive) 36.dp else 28.dp)
                    .background(color.copy(alpha = if (isActive) 0.15f else 0.05f), RoundedCornerShape(50))
                    .border(1.5.dp, color, RoundedCornerShape(50))
            ) {
                Text(
                    text       = if (isDone) "✓" else "$step",
                    color      = color,
                    fontWeight = FontWeight.Bold,
                    fontSize   = if (isActive) 14.sp else 11.sp
                )
            }
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(1.5.dp)
                        .background(if (isDone) HadesCyan else HadesOnDark.copy(alpha = 0.2f))
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text          = "Paso $currentStep de $totalSteps",
        fontSize      = 10.sp,
        color         = HadesOnDark.copy(alpha = 0.5f),
        letterSpacing = 1.sp
    )
}

private fun capturarFoto(
    fileName: String,
    imageCapture: ImageCapture?,
    context: Context,
    executor: Executor,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val photoFile     = File(context.cacheDir, fileName)
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
