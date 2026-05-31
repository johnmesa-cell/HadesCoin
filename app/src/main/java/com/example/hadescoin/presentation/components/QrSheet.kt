package com.example.hadescoin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.R
import com.example.hadescoin.ui.theme.*

/**
 * Bottom sheet de selección QR.
 *
 * Muestra dos opciones decorativas/demostrativas (próximamente):
 *   - Escanear QR  — leer el código QR de otra persona para transferirle.
 *   - Generar QR   — mostrar el QR propio para recibir dinero.
 *
 * Ninguna de las dos ejecuta acción real aún; están deshabilitadas
 * visualmente con badge "Próximamente" hasta que se implementen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Título ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(HadesPurple.copy(alpha = 0.10f))
                    .border(1.dp, HadesPurple.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.QrCode,
                    contentDescription = null,
                    tint               = HadesPurple,
                    modifier           = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = stringResource(R.string.qr_title),
                fontSize      = 13.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
                color         = HadesPurple
            )
            Text(
                text      = stringResource(R.string.qr_subtitle),
                fontSize  = 12.sp,
                color     = HadesOnDark.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Opción: Escanear QR ─────────────────────────────────────
             QrOptionCard(
                icon        = Icons.Filled.QrCodeScanner,
                title       = stringResource(R.string.qr_scan_title),
                description = stringResource(R.string.qr_scan_description),
                onClick     = { /* Próximamente */ }
            )

            // ── Opción: Generar QR ───────────────────────────────────────
            QrOptionCard(
                icon        = Icons.Filled.QrCode,
                title       = stringResource(R.string.qr_generate_title),
                description = stringResource(R.string.qr_generate_description),
                onClick     = { /* Próximamente */ }
            )
        }
    }
}

@Composable
private fun QrOptionCard(
    icon:        androidx.compose.ui.graphics.vector.ImageVector,
    title:       String,
    description: String,
    onClick:     () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HadesNavyDark)
            .border(1.dp, HadesOnDark.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier            = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HadesPurple.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = HadesOnDark.copy(alpha = 0.25f),
                    modifier           = Modifier.size(24.dp)
                )
            }

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = HadesOnDark.copy(alpha = 0.35f)
                    )
                    // Badge "Próximamente"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(HadesPurple.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text      = stringResource(R.string.qr_coming_soon),
                            fontSize  = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color     = HadesPurple.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text     = description,
                    fontSize = 11.sp,
                    color    = HadesOnDark.copy(alpha = 0.25f),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
