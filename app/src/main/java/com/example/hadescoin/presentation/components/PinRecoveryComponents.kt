package com.example.hadescoin.presentation.components

/**
 * PinRecoveryComponents.kt
 *
 * Flujo reutilizable "Olvide mi PIN" / verificacion de identidad.
 * Tambien sirve como base para confirmar Retiros u otras acciones sensibles.
 *
 * FLUJO (4 pasos encadenados):
 *   Paso 1 — VerifyIdentityDialog : ingresa telefono + cedula → genera codigo en Firebase
 *   Paso 2 — CodeRevealDialog    : muestra el codigo de 6 digitos + boton copiar
 *   Paso 3 — ConfirmCodeDialog   : el usuario ingresa el codigo que vio/copio
 *   Paso 4 — ResetPinStepDialog  : elige nuevo PIN (solo para flujo de PIN)
 *
 * Para flujo de Retiro: usa solo pasos 1-3 con showResetStep = false.
 *
 * USO minimo (flujo de PIN):
 *
 *   PinRecoveryFlow(
 *       codigoGenerado = codigoGenerado,
 *       codigoValidado = codigoValidado,
 *       onDismiss      = { showFlow = false },
 *       onGenerate     = { phone, doc -> viewModel.generarCodigoVerificacion(phone, doc) },
 *       onValidate     = { code -> viewModel.validarCodigo(code) },
 *       onReset        = { pin  -> viewModel.resetearPin(pin) },
 *       onClearState   = { viewModel.clearMessages() }
 *   )
 *
 * USO para Retiro (sin paso 4):
 *
 *   PinRecoveryFlow(
 *       codigoGenerado = codigoGenerado,
 *       codigoValidado = codigoValidado,
 *       showResetStep  = false,
 *       onVerified     = { viewModel.ejecutarRetiro() },
 *       onGenerate     = { phone, doc -> viewModel.generarCodigo(phone, doc) },
 *       onValidate     = { code -> viewModel.validarCodigo(code) },
 *       onClearState   = { viewModel.clearMessages() }
 *   )
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.R
import com.example.hadescoin.ui.theme.*

// ─── Orquestador del flujo ────────────────────────────────────────────────
@Composable
fun PinRecoveryFlow(
    codigoGenerado : String?,
    codigoValidado : Boolean,
    onDismiss      : () -> Unit,
    onGenerate     : (phone: String, document: String) -> Unit,
    onValidate     : (code: String) -> Unit,
    onReset        : (pin: String)  -> Unit = {},
    onVerified     : ()             -> Unit = {},
    showResetStep  : Boolean                = true,
    onClearState   : ()             -> Unit = {}
) {
    var paso by remember { mutableIntStateOf(1) }

    LaunchedEffect(codigoGenerado) { if (codigoGenerado != null) paso = 2 }

    LaunchedEffect(codigoValidado) {
        if (codigoValidado) {
            if (showResetStep) paso = 4
            else { onVerified(); onDismiss() }
        }
    }

    when (paso) {
        1 -> VerifyIdentityDialog(
            onDismiss  = { onDismiss(); onClearState() },
            onGenerate = onGenerate
        )
        2 -> CodeRevealDialog(
            codigo     = codigoGenerado ?: "",
            onDismiss  = { onDismiss(); onClearState() },
            onContinue = { paso = 3 }
        )
        3 -> ConfirmCodeDialog(
            onDismiss  = { onDismiss(); onClearState() },
            onValidate = onValidate
        )
        4 -> ResetPinStepDialog(
            onDismiss = { onDismiss(); onClearState() },
            onReset   = { pin -> onReset(pin); onClearState() }
        )
    }
}

// ─── Paso 1: ingresar telefono + cedula para verificar identidad ───────────────
@Composable
fun VerifyIdentityDialog(
    onDismiss  : () -> Unit,
    onGenerate : (phone: String, document: String) -> Unit
) {
    var phone    by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }

    val habilitado = phone.length == 10 && document.length >= 6

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = HadesPurple, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.recovery_title), color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text      = stringResource(R.string.recovery_identity_subtitle),
                    color     = HadesOnDark.copy(alpha = 0.7f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                HadesTextField(
                    value         = phone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    label         = stringResource(R.string.label_phone_recovery),
                    keyboardType  = KeyboardType.Number
                )
                HadesTextField(
                    value         = document,
                    onValueChange = { if (it.length <= 12 && it.all { c -> c.isDigit() }) document = it },
                    label         = stringResource(R.string.label_document_recovery),
                    keyboardType  = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = habilitado,
                onClick = { onGenerate(phone, document) }
            ) {
                Text(
                    stringResource(R.string.btn_verify_identity),
                    color      = if (habilitado) HadesCyan else HadesOnDark.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = HadesOnDark.copy(alpha = 0.6f)) }
        }
    )
}

// ─── Paso 2: mostrar el codigo generado con boton copiar ───────────────────────
@Composable
fun CodeRevealDialog(
    codigo     : String,
    onDismiss  : () -> Unit,
    onContinue : () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copiado   by remember { mutableStateOf(false) }
    var visible   by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Key, contentDescription = null, tint = HadesCyan, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.recovery_code_title), color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text      = stringResource(R.string.recovery_code_subtitle),
                    color     = HadesOnDark.copy(alpha = 0.7f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center
                )

                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(400)) + scaleIn(tween(400))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(HadesPurple.copy(alpha = 0.12f), HadesCyan.copy(alpha = 0.06f))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(HadesPurple.copy(alpha = 0.5f), HadesCyan.copy(alpha = 0.35f))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment      = Alignment.CenterVertically
                        ) {
                            codigo.forEach { digit ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(HadesNavyDark, RoundedCornerShape(8.dp))
                                        .border(1.dp, HadesPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = digit.toString(),
                                        fontSize   = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color      = HadesOnDark
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick  = { clipboard.setText(AnnotatedString(codigo)); copiado = true },
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (copiado) HadesCyan else HadesOnDark.copy(alpha = 0.7f)
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (copiado) HadesCyan.copy(alpha = 0.6f) else HadesOnDark.copy(alpha = 0.2f)
                    ),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.btn_copy), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (copiado) stringResource(R.string.btn_copied) else stringResource(R.string.btn_copy), fontSize = 13.sp)
                }

                Text(
                    text      = stringResource(R.string.recovery_code_reminder),
                    color     = HadesOnDark.copy(alpha = 0.4f),
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(stringResource(R.string.btn_continue), color = HadesOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = HadesOnDark.copy(alpha = 0.6f)) }
        }
    )
}

// ─── Paso 3: confirmar el codigo recibido ────────────────────────────────────
@Composable
fun ConfirmCodeDialog(
    onDismiss  : () -> Unit,
    onValidate : (code: String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.LockOpen, contentDescription = null, tint = HadesCyan, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.recovery_confirm_title), color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text      = stringResource(R.string.recovery_confirm_subtitle),
                    color     = HadesOnDark.copy(alpha = 0.7f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                HadesTextField(
                    value         = code,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                    label         = stringResource(R.string.label_verification_code),
                    keyboardType  = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = code.length == 6,
                onClick = { onValidate(code) }
            ) {
                Text(
                    text       = stringResource(R.string.btn_verify),
                    color      = if (code.length == 6) HadesCyan else HadesOnDark.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = HadesOnDark.copy(alpha = 0.6f)) }
        }
    )
}

// ─── Paso 4: elegir nuevo PIN (solo flujo de cambio de PIN) ─────────────────
@Composable
fun ResetPinStepDialog(
    onDismiss : () -> Unit,
    onReset   : (nuevoPin: String) -> Unit
) {
    var nuevoPin     by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    val coincide   = nuevoPin.length == 4 && nuevoPin == confirmacion
    val errorTexto = if (nuevoPin.length == 4 && confirmacion.length == 4 && !coincide)
        stringResource(R.string.error_pins_no_match) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = HadesOrange, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.recovery_reset_title), color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text     = stringResource(R.string.recovery_reset_subtitle),
                    color    = HadesOnDark.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                HadesTextField(
                    value         = nuevoPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) nuevoPin = it },
                    label         = stringResource(R.string.label_new_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )
                HadesTextField(
                    value         = confirmacion,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmacion = it },
                    label         = stringResource(R.string.label_confirm_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword,
                    isError       = errorTexto != null
                )
                if (errorTexto != null) {
                    Text(errorTexto, color = MaterialTheme.colorScheme.error, fontSize = 11.sp,
                        textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(enabled = coincide, onClick = { onReset(nuevoPin) }) {
                Text(stringResource(R.string.btn_update),
                    color      = if (coincide) HadesCyan else HadesOnDark.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = HadesOnDark.copy(alpha = 0.6f)) }
        }
    )
}
