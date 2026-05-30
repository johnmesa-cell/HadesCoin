package com.example.hadescoin.presentation.components

/**
 * PinRecoveryComponents.kt
 *
 * Componente reutilizable que encapsula el flujo completo "Olvidé mi PIN".
 * Puede usarse desde Login, Perfil, Retiro o cualquier pantalla futura.
 *
 * Uso:
 *   PinRecoveryFlow(
 *       onDismiss  = { ... },
 *       onRecover  = { phone, doc -> viewModel.recuperarPin(phone, doc) },
 *       onReset    = { nuevoPin -> viewModel.resetearPin(nuevoPin) },
 *       pinRecuperado = pinRecuperado  // String? desde ViewModel
 *   )
 *
 * Flujo interno (3 pasos en dialogs encadenados):
 *   [1] RecoverPinStepDialog  — ingresa telefono + documento
 *   [2] PinRevealDialog       — muestra el PIN encontrado con boton copiar
 *   [3] ResetPinStepDialog    — confirma nuevo PIN (dos campos)
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.ui.theme.*

// ─── Controlador del flujo completo ───────────────────────────────────────────
/**
 * Orquesta los 3 pasos del flujo. Coloca este composable en la pantalla host
 * y pasa los callbacks del ViewModel correspondiente.
 *
 * @param pinRecuperado  Estado LiveData/StateFlow observado en la pantalla host.
 *                       Cuando llega no-null, el flujo avanza al paso 2.
 * @param onDismiss      Se llama si el usuario cancela en cualquier paso.
 * @param onRecover      Paso 1: invoca el caso de uso con (telefono, documento).
 * @param onReset        Paso 3: invoca el caso de uso con el nuevo PIN elegido.
 * @param onClearState   Limpia el estado del ViewModel al cerrar o completar.
 */
@Composable
fun PinRecoveryFlow(
    pinRecuperado : String?,
    onDismiss     : () -> Unit,
    onRecover     : (phone: String, doc: String) -> Unit,
    onReset       : (nuevoPin: String) -> Unit,
    onClearState  : () -> Unit = {}
) {
    // Paso activo: 1 = verificar identidad, 2 = ver PIN, 3 = cambiar PIN
    var paso by remember { mutableIntStateOf(1) }

    // Cuando el ViewModel entrega el PIN, avanzamos al paso 2
    LaunchedEffect(pinRecuperado) {
        if (pinRecuperado != null) paso = 2
    }

    when (paso) {
        1 -> RecoverPinStepDialog(
            onDismiss = { onDismiss(); onClearState() },
            onRecover = onRecover
        )
        2 -> PinRevealDialog(
            pin       = pinRecuperado ?: "",
            onDismiss = { onDismiss(); onClearState() },
            onChangePinClick = { paso = 3 }
        )
        3 -> ResetPinStepDialog(
            onDismiss = { onDismiss(); onClearState() },
            onReset   = { nuevoPin ->
                onReset(nuevoPin)
                onClearState()
            }
        )
    }
}

// ─── Paso 1: verificar identidad ──────────────────────────────────────────────
@Composable
fun RecoverPinStepDialog(
    onDismiss : () -> Unit,
    onRecover : (phone: String, doc: String) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var doc   by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = HadesPurple, modifier = Modifier.size(20.dp))
                Text("Recuperar PIN", color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text      = "Verifica tu identidad para continuar",
                    color     = HadesOnDark.copy(alpha = 0.7f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                HadesTextField(
                    value         = phone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    label         = "Teléfono (10 dígitos)",
                    keyboardType  = KeyboardType.Number
                )
                HadesTextField(
                    value         = doc,
                    onValueChange = { if (it.all { c -> c.isDigit() }) doc = it },
                    label         = "Número de Documento",
                    keyboardType  = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = phone.length == 10 && doc.isNotBlank(),
                onClick = { onRecover(phone, doc) }
            ) {
                Text("VERIFICAR", color = HadesCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = HadesOnDark.copy(alpha = 0.5f)) }
        }
    )
}

// ─── Paso 2: mostrar PIN con botón copiar ─────────────────────────────────────
@Composable
fun PinRevealDialog(
    pin              : String,
    onDismiss        : () -> Unit,
    onChangePinClick : () -> Unit
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
                Icon(Icons.Filled.LockOpen, contentDescription = null, tint = HadesCyan, modifier = Modifier.size(20.dp))
                Text("PIN Encontrado", color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "Este es tu PIN actual:",
                    color    = HadesOnDark.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )

                // Caja visual del PIN
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(400)) + scaleIn(tween(400))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(HadesPurple.copy(alpha = 0.15f), HadesCyan.copy(alpha = 0.08f))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(HadesPurple.copy(alpha = 0.6f), HadesCyan.copy(alpha = 0.4f))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dígitos separados
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment      = Alignment.CenterVertically
                        ) {
                            pin.forEach { digit ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            color = HadesNavyDark,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = HadesPurple.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = digit.toString(),
                                        fontSize   = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color      = HadesOnDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Boton copiar
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(pin))
                        copiado = true
                    },
                    colors  = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (copiado) HadesCyan else HadesOnDark.copy(alpha = 0.7f)
                    ),
                    border  = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (copiado) HadesCyan.copy(alpha = 0.6f) else HadesOnDark.copy(alpha = 0.2f)
                    ),
                    shape   = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector        = Icons.Filled.ContentCopy,
                        contentDescription = "Copiar PIN",
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = if (copiado) "¡Copiado!" else "Copiar PIN",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text      = "Te recomendamos cambiarlo por uno nuevo.",
                    color     = HadesOnDark.copy(alpha = 0.45f),
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onChangePinClick) {
                Text("CAMBIAR PIN", color = HadesOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ENTENDIDO", color = HadesCyan) }
        }
    )
}

// ─── Paso 3: elegir nuevo PIN ─────────────────────────────────────────────────
@Composable
fun ResetPinStepDialog(
    onDismiss : () -> Unit,
    onReset   : (nuevoPin: String) -> Unit
) {
    var nuevoPin     by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    val coincide   = nuevoPin.length == 4 && nuevoPin == confirmacion
    val errorTexto = when {
        nuevoPin.length == 4 && confirmacion.length == 4 && nuevoPin != confirmacion -> "Los PINs no coinciden"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = HadesOrange, modifier = Modifier.size(20.dp))
                Text("Nuevo PIN", color = HadesPurple, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text     = "Elige un PIN seguro de 4 dígitos",
                    color    = HadesOnDark.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                HadesTextField(
                    value         = nuevoPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) nuevoPin = it },
                    label         = "Nuevo PIN",
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )
                HadesTextField(
                    value         = confirmacion,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmacion = it },
                    label         = "Confirmar PIN",
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword,
                    isError       = errorTexto != null
                )
                if (errorTexto != null) {
                    Text(
                        text      = errorTexto,
                        color     = MaterialTheme.colorScheme.error,
                        fontSize  = 11.sp,
                        textAlign = TextAlign.End,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = coincide,
                onClick = { onReset(nuevoPin) }
            ) {
                Text(
                    text       = "ACTUALIZAR",
                    color      = if (coincide) HadesCyan else HadesOnDark.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = HadesOnDark.copy(alpha = 0.5f)) }
        }
    )
}
