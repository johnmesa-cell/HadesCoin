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
import androidx.compose.ui.res.stringResource
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
import com.example.hadescoin.R
import com.example.hadescoin.presentation.utils.BiometricHelper
import com.example.hadescoin.ui.theme.*

/**
 * Diálogo de dos pasos para generar un código de retiro en cajero.
 *
 * Modo inteligente (biometriaActiva = true):
 *   - El campo PIN desaparece del formulario.
 *   - Solo se ingresa el monto y se toca el botón de huella para generar.
 *   - onGenerate se llama con pin="" y autenticadoConHuella=true.
 *
 * Modo PIN (biometriaActiva = false o dispositivo sin lector):
 *   - Flujo original: monto + PIN de 4 dígitos.
 *   - onGenerate se llama con el PIN ingresado y autenticadoConHuella=false.
 */
@Composable
fun WithdrawCodeDialog(
    cargando:        Boolean,
    codigoRetiro:    String?,
    biometriaActiva: Boolean = false,
    activity:        FragmentActivity? = null,
    onGenerate:      (amount: Double, pin: String, autenticadoConHuella: Boolean) -> Unit,
    onDismiss:       () -> Unit
) {
    var monto by remember { mutableStateOf("") }
    var pin   by remember { mutableStateOf("") }

    val montoValido      = monto.toDoubleOrNull()?.let { it > 0 } ?: false
    // Modo huella: solo necesita monto válido
    val huellaLista      = biometriaActiva && activity != null && montoValido
    // Modo PIN: necesita monto + PIN de 4 dígitos
    val formularioListo  = !biometriaActiva && montoValido && pin.length == 4

    // Guardar strings aquí en el contexto Composable
    val bioConfirmTitle = stringResource(R.string.withdraw_biometry_confirmation_title)
    val bioConfirmSubtitle = stringResource(R.string.withdraw_biometry_confirmation_subtitle)

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
                        biometriaActiva = biometriaActiva,
                        formularioListo = formularioListo,
                        huellaLista     = huellaLista,
                        onMontoChange   = { v -> if (v.all { c -> c.isDigit() || c == '.' } && v.length <= 12) monto = v },
                        onPinChange     = { v -> if (v.length <= 4 && v.all { c -> c.isDigit() }) pin = v },
                        onGenerate      = { onGenerate(monto.toDouble(), pin, false) },
                         onHuellaClick   = {
                             if (activity != null) {
                                 BiometricHelper.mostrar(
                                     activity  = activity,
                                     titulo    = bioConfirmTitle,
                                     subtitulo = bioConfirmSubtitle,
                                     onExito   = { onGenerate(monto.toDouble(), "", true) },
                                     onError   = { }
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
    biometriaActiva: Boolean,
    formularioListo: Boolean,
    huellaLista:     Boolean,
    onMontoChange:   (String) -> Unit,
    onPinChange:     (String) -> Unit,
    onGenerate:      () -> Unit,
    onHuellaClick:   () -> Unit,
    onDismiss:       () -> Unit
) {
    val montoValido = monto.toDoubleOrNull()?.let { it > 0 } ?: false

    Column(
        modifier            = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Encabezado ────────────────────────────────────────────────────
         Text(
            text      = stringResource(R.string.withdraw_title),
            fontSize  = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color     = HadesCyan,
            textAlign = TextAlign.Center
        )

        // Indicador de modo activo
        if (biometriaActiva) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(HadesCyan.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, tint = HadesCyan, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = stringResource(R.string.withdraw_biometry_mode), fontSize = 10.sp, color = HadesCyan, fontWeight = FontWeight.Medium)
            }
        }

        Text(
            text      = stringResource(R.string.withdraw_expiry_info),
            fontSize  = 11.sp,
            color     = HadesOnDark.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        // ── Campo monto — siempre visible ─────────────────────────────────
         HadesTextField(
            value         = monto,
            onValueChange = onMontoChange,
            label         = stringResource(R.string.withdraw_amount_label),
            keyboardType  = KeyboardType.Decimal
        )

        // ── Campo PIN — solo visible cuando NO hay huella ─────────────────
         AnimatedVisibility(
            visible = !biometriaActiva,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            HadesTextField(
                value         = pin,
                onValueChange = onPinChange,
                label         = stringResource(R.string.withdraw_pin_label),
                isPassword    = true,
                keyboardType  = KeyboardType.NumberPassword
            )
        }

        // ── Modo huella: botón grande como acción principal ───────────────
         AnimatedVisibility(
            visible = biometriaActiva,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick  = onHuellaClick,
                        enabled  = huellaLista && !cargando,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (huellaLista) HadesCyan.copy(alpha = 0.12f)
                                else HadesOnDark.copy(alpha = 0.04f)
                            )
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Fingerprint,
                            contentDescription = stringResource(R.string.cd_generate_fingerprint),
                            tint               = if (huellaLista) HadesCyan else HadesOnDark.copy(alpha = 0.2f),
                            modifier           = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text      = if (montoValido) stringResource(R.string.withdraw_fingerprint_action)
                                else stringResource(R.string.withdraw_amount_required),
                    fontSize  = 11.sp,
                    color     = if (montoValido) HadesOnDark.copy(alpha = 0.4f)
                                else HadesOrange.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }

        // ── Botones inferiores ────────────────────────────────────────────
         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick  = onDismiss,
                modifier = Modifier.weight(1f).height(48.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = HadesOnDark.copy(alpha = 0.08f),
                    contentColor   = HadesOnDark.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = stringResource(R.string.btn_cancel), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            // Botón GENERAR — solo en modo PIN (modo huella usa el botón de huella de arriba)
             AnimatedVisibility(
                visible = !biometriaActiva,
                enter   = fadeIn() + expandHorizontally(),
                exit    = fadeOut() + shrinkHorizontally()
            ) {
                HadesButton(
                    text         = stringResource(R.string.btn_generate),
                    textCargando = stringResource(R.string.btn_generate_loading),
                    onClick      = onGenerate,
                    enabled      = formularioListo,
                    cargando     = cargando,
                    modifier     = Modifier.weight(1f).height(48.dp)
                )
            }
        }
    }
}

/** Botón de huella reutilizable (usado en otros flows si se necesita). */
@Composable
fun HuellaAlternativaButton(
    habilitado:    Boolean,
    cargando:      Boolean,
    onHuellaClick: () -> Unit,
    subtexto:      String = ""
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val textoFinal = if (subtexto.isBlank()) stringResource(R.string.withdrawal_alternative_text) else subtexto
        Text(text = "— $textoFinal —", fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.35f), textAlign = TextAlign.Center)
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
                contentDescription = stringResource(R.string.cd_authenticate_fingerprint),
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

    Column(
        modifier            = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.withdraw_success_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = HadesCyan)
        Text(text = stringResource(R.string.withdraw_success_subtitle), fontSize = 11.sp, color = HadesOnDark.copy(alpha = 0.5f), textAlign = TextAlign.Center)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(HadesPurple.copy(alpha = 0.15f), HadesCyan.copy(alpha = 0.1f))),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(HadesPurple, HadesCyan)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text          = codigo.chunked(3).joinToString(" "),
                fontSize      = 36.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 6.sp,
                color         = HadesPurple
            )
        }

        if (monto.isNotBlank())
            Text(
                text       = stringResource(R.string.withdraw_authorized_amount, monto.toDoubleOrNull()?.let { "%,.0f".format(it) } ?: monto),
                fontSize   = 13.sp,
                color      = HadesOrange,
                fontWeight = FontWeight.Bold
            )
        Text(text = stringResource(R.string.withdraw_expiry_timer), fontSize = 11.sp, color = HadesOnDark.copy(alpha = 0.45f))

        Button(
            onClick  = {
                clipboardManager.setText(AnnotatedString(codigo))
                copiado = true
                scope.launch { delay(2000); copiado = false }
            },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (copiado) HadesCyan.copy(alpha = 0.15f) else HadesPurple.copy(alpha = 0.15f),
                contentColor   = if (copiado) HadesCyan else HadesPurple
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = if (copiado) stringResource(R.string.btn_code_copied) else stringResource(R.string.btn_copy_code), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        }

        HadesButton(text = stringResource(R.string.btn_understood), onClick = onCerrar, modifier = Modifier.fillMaxWidth())
    }
}
