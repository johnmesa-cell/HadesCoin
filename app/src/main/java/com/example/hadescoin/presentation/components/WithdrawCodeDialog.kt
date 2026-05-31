package com.example.hadescoin.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.example.hadescoin.presentation.utils.BiometricHelper
import com.example.hadescoin.ui.theme.*

/**
 * Diálogo de dos pasos para generar un código de retiro en cajero.
 *
 * Paso 1 — Formulario: monto + PIN (siempre disponible).
 *           Si biometriaActiva=true, se muestra además el botón de huella
 *           como atajo para omitir el PIN. En dispositivos sin huella o
 *           con biometría desactivada solo aparece el PIN normal.
 * Paso 2 — Código: muestra el código de 6 dígitos generado + tiempo de expiración.
 */
@Composable
fun WithdrawCodeDialog(
    cargando:        Boolean,
    codigoRetiro:    String?,
    biometriaActiva: Boolean = false,
    activity:        FragmentActivity? = null,
    onGenerate:      (amount: Double, pin: String) -> Unit,
    onDismiss:       () -> Unit
) {
    var monto by remember { mutableStateOf("") }
    var pin   by remember { mutableStateOf("") }
    val montoValido     = monto.toDoubleOrNull()?.let { it > 0 } ?: false
    val formularioListo = montoValido && pin.length == 4
    // Con huella: solo necesita el monto (el PIN se omite)
    val huellaLista     = biometriaActiva && activity != null && montoValido

    Dialog(onDismissRequest = { if (!cargando) onDismiss() }) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = HadesNavyDark,
            tonalElevation = 8.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState    = codigoRetiro != null,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 })
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "withdraw_code_transition"
            ) { codigoMostrado ->
                if (codigoMostrado) {
                    CodigoRetiroContent(codigo = codigoRetiro ?: "", monto = monto, onCerrar = onDismiss)
                } else {
                    FormularioRetiroContent(
                        monto           = monto,
                        pin             = pin,
                        cargando        = cargando,
                        formularioListo = formularioListo,
                        biometriaActiva = biometriaActiva,
                        huellaLista     = huellaLista,
                        onMontoChange   = { v -> if (v.all { c -> c.isDigit() || c == '.' } && v.length <= 12) monto = v },
                        onPinChange     = { v -> if (v.length <= 4 && v.all { c -> c.isDigit() }) pin = v },
                        onGenerate      = { onGenerate(monto.toDouble(), pin) },
                        onHuellaClick   = {
                            if (activity != null) {
                                BiometricHelper.mostrar(
                                    activity  = activity,
                                    titulo    = "Confirmar retiro",
                                    subtitulo = "Usa tu huella para generar el código de retiro",
                                    onExito   = { onGenerate(monto.toDouble(), "") },
                                    onError   = { /* el sistema ya muestra el feedback */ }
                                )
                            }
                        },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioRetiroContent(
    monto:           String,
    pin:             String,
    cargando:        Boolean,
    formularioListo: Boolean,
    biometriaActiva: Boolean,
    huellaLista:     Boolean,
    onMontoChange:   (String) -> Unit,
    onPinChange:     (String) -> Unit,
    onGenerate:      () -> Unit,
    onHuellaClick:   () -> Unit,
    onDismiss:       () -> Unit
) {
    Column(
        modifier            = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "GENERAR CÓDIGO DE RETIRO", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = HadesCyan, textAlign = TextAlign.Center)
        Text(text = "El código expira en 25 minutos.", fontSize = 11.sp, color = HadesOnDark.copy(alpha = 0.5f), textAlign = TextAlign.Center)

        HadesTextField(value = monto, onValueChange = onMontoChange, label = "Monto máximo a retirar", keyboardType = KeyboardType.Decimal)
        HadesTextField(value = pin,   onValueChange = onPinChange,   label = "Tu PIN", isPassword = true, keyboardType = KeyboardType.NumberPassword)

        // ── Botón de huella — solo visible si biometría activa ────────────
        if (biometriaActiva) {
            HuellaAlternativaButton(
                habilitado    = huellaLista,
                cargando      = cargando,
                onHuellaClick = onHuellaClick
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick  = onDismiss,
                modifier = Modifier.weight(1f).height(48.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = HadesOnDark.copy(alpha = 0.08f), contentColor = HadesOnDark.copy(alpha = 0.6f)),
                shape    = RoundedCornerShape(10.dp)
            ) { Text(text = "CANCELAR", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

            HadesButton(
                text         = "GENERAR",
                textCargando = "GENERANDO...",
                onClick      = onGenerate,
                enabled      = formularioListo,
                cargando     = cargando,
                modifier     = Modifier.weight(1f).height(48.dp)
            )
        }
    }
}

/** Sección reutilizable que muestra el separador "— o usa tu huella —" + botón de huella. */
@Composable
fun HuellaAlternativaButton(
    habilitado:    Boolean,
    cargando:      Boolean,
    onHuellaClick: () -> Unit,
    subtexto:      String = "o usa tu huella digital"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(text = "— $subtexto —", fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.35f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(
            onClick  = onHuellaClick,
            enabled  = habilitado && !cargando,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (habilitado) HadesCyan.copy(alpha = 0.10f)
                    else HadesOnDark.copy(alpha = 0.04f)
                )
        ) {
            Icon(
                imageVector        = Icons.Filled.Fingerprint,
                contentDescription = "Autenticar con huella",
                tint               = if (habilitado) HadesCyan else HadesOnDark.copy(alpha = 0.25f),
                modifier           = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun CodigoRetiroContent(codigo: String, monto: String, onCerrar: () -> Unit) {
    var copiado by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "¡CÓDIGO GENERADO!", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = HadesCyan)
        Text(text = "Preséntalo en el cajero HadesCoin.", fontSize = 11.sp, color = HadesOnDark.copy(alpha = 0.5f), textAlign = TextAlign.Center)

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = Brush.horizontalGradient(listOf(HadesPurple.copy(alpha = 0.15f), HadesCyan.copy(alpha = 0.1f))), shape = RoundedCornerShape(16.dp))
                .border(width = 1.5.dp, brush = Brush.horizontalGradient(listOf(HadesPurple, HadesCyan)), shape = RoundedCornerShape(16.dp))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = codigo.chunked(3).joinToString(" "), fontSize = 36.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp, color = HadesPurple)
        }

        if (monto.isNotBlank()) Text(text = "Monto autorizado: $${monto.toDoubleOrNull()?.let { "%,.0f".format(it) } ?: monto}", fontSize = 13.sp, color = HadesOrange, fontWeight = FontWeight.Bold)
        Text(text = "⏱ Expira en 25 minutos", fontSize = 11.sp, color = HadesOnDark.copy(alpha = 0.45f))

        Button(
            onClick  = { clipboardManager.setText(AnnotatedString(codigo)); copiado = true; scope.launch { delay(2000); copiado = false } },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(containerColor = if (copiado) HadesCyan.copy(alpha = 0.15f) else HadesPurple.copy(alpha = 0.15f), contentColor = if (copiado) HadesCyan else HadesPurple),
            shape    = RoundedCornerShape(10.dp)
        ) { Text(text = if (copiado) "¡COPIADO! ✓" else "COPIAR CÓDIGO", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }

        HadesButton(text = "ENTENDIDO", onClick = onCerrar, modifier = Modifier.fillMaxWidth())
    }
}
