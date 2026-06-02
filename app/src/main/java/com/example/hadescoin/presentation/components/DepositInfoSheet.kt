package com.example.hadescoin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
 * Bottom sheet informativo: "Cómo depositar saldo"
 *
 * Muestra 4 pasos claros y una nota informativa sobre depositar en HadesCoin
 * a través del terminal físico HadesCoin Cajero.
 *
 * El teléfono se muestra destacado para que el usuario lo tenga claro
 * al acercarse al terminal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositInfoSheet(
    phoneNumber: String,
    onDismiss: () -> Unit
) {
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
                    .background(HadesCyan.copy(alpha = 0.10f))
                    .border(1.dp, HadesCyan.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Info,
                    contentDescription = null,
                    tint               = HadesCyan,
                    modifier           = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = stringResource(R.string.deposit_info_titulo),
                fontSize      = 13.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
                color         = HadesCyan
            )
            Text(
                text      = stringResource(R.string.deposit_info_subtitulo),
                fontSize  = 12.sp,
                color     = HadesOnDark.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Paso 1 ─────────────────────────────────────────────────────
            StepCard(
                stepNumber = 1,
                title      = stringResource(R.string.deposit_info_paso_1_titulo),
                description = stringResource(R.string.deposit_info_paso_1_desc)
            )

            // ── Paso 2 ─────────────────────────────────────────────────────
            StepCard(
                stepNumber = 2,
                title      = stringResource(R.string.deposit_info_paso_2_titulo),
                description = stringResource(R.string.deposit_info_paso_2_desc),
                phoneNumber = phoneNumber
            )

            // ── Paso 3 ─────────────────────────────────────────────────────
            StepCard(
                stepNumber = 3,
                title      = stringResource(R.string.deposit_info_paso_3_titulo),
                description = stringResource(R.string.deposit_info_paso_3_desc)
            )

            // ── Paso 4 ─────────────────────────────────────────────────────
            StepCard(
                stepNumber = 4,
                title      = stringResource(R.string.deposit_info_paso_4_titulo),
                description = stringResource(R.string.deposit_info_paso_4_desc)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Nota informativa ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HadesOnDark.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                Text(
                    text      = stringResource(R.string.deposit_info_nota),
                    fontSize  = 11.sp,
                    color     = HadesOnDark.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    description: String,
    phoneNumber: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HadesNavyDark)
            .border(1.dp, HadesOnDark.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier            = Modifier.fillMaxWidth(),
            verticalAlignment   = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Número del paso
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(HadesCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = stepNumber.toString(),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = HadesCyan
                )
            }

            // Contenido del paso
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = HadesOnDark.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = description,
                    fontSize = 12.sp,
                    color    = HadesOnDark.copy(alpha = 0.45f),
                    lineHeight = 16.sp
                )

                // Si hay teléfono (paso 2), mostrarlo destacado
                if (phoneNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(HadesCyan.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text       = stringResource(R.string.deposit_info_tu_numero),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = HadesOnDark.copy(alpha = 0.5f)
                            )
                            Text(
                                text       = phoneNumber,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = HadesCyan
                            )
                        }
                    }
                }
            }
        }
    }
}



