package com.example.hadescoin.presentation.transfer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*
import java.util.Locale

@Composable
fun TransferView(
    senderPhone: String,
    navController: NavController,
    viewModel: TransferViewModel = viewModel()
) {
    var receiverPhone by remember { mutableStateOf("") }
    var amount        by remember { mutableStateOf("") }
    var pin           by remember { mutableStateOf("") }
    var showConfirm   by remember { mutableStateOf(false) }

    val cargando        by viewModel.cargando.observeAsState(false)
    val senderBalance   by viewModel.senderBalance.observeAsState(0.0)
    val transferExitosa by viewModel.transferExitosa.observeAsState()
    val transferError   by viewModel.transferError.observeAsState()

    var mensajeError by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }
    var showExito    by remember { mutableStateOf(false) }

    LaunchedEffect(senderPhone) {
        viewModel.loadSenderBalance(senderPhone)
    }

    LaunchedEffect(transferExitosa) {
        if (transferExitosa == true) {
            showExito = true
            viewModel.clearExito()
        }
    }

    LaunchedEffect(transferError) {
        transferError?.let {
            mensajeError = it
            showError = true
            showConfirm = false
        }
    }

    TransferViewContent(
        senderPhone      = senderPhone,
        senderBalance    = senderBalance,
        receiverPhone    = receiverPhone,
        amount           = amount,
        pin              = pin,
        cargando         = cargando,
        showConfirm      = showConfirm,
        onReceiverChange = { receiverPhone = it },
        onAmountChange   = { amount = it },
        onPinChange      = { pin = it },
        onReviewClick    = { showConfirm = true },
        onConfirmTransfer = {
            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
            viewModel.transfer(senderPhone, receiverPhone, parsedAmount, pin)
        },
        onDismissConfirm = { showConfirm = false },
        onBackClick      = { navController.popBackStack() }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showExito) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showExito = false
                navController.popBackStack()
            },
            dialogTitle = "Éxito",
            dialogText  = "Transferencia de $${ String.format(Locale.US, "%,.2f", amount.toDoubleOrNull() ?: 0.0) } enviada correctamente."
        )
    }

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showError = false
                viewModel.clearError()
            },
            dialogTitle = "Error",
            dialogText  = mensajeError
        )
    }
}

@Composable
fun TransferViewContent(
    senderPhone: String,
    senderBalance: Double,
    receiverPhone: String,
    amount: String,
    pin: String,
    cargando: Boolean,
    showConfirm: Boolean,
    onReceiverChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onReviewClick: () -> Unit,
    onConfirmTransfer: () -> Unit,
    onDismissConfirm: () -> Unit,
    onBackClick: () -> Unit
) {
    val parsedAmount   = amount.toDoubleOrNull() ?: 0.0
    val remaining      = senderBalance - parsedAmount
    val exceedsBalance = parsedAmount > senderBalance && parsedAmount > 0.0
    val isFormValid    = !cargando
        && receiverPhone.length == 10
        && parsedAmount > 0.0
        && !exceedsBalance
        && pin.length == 4

    var saldoVisible by remember { mutableStateOf(true) }

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // ── Header ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint               = HadesCyan,
                        modifier           = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text          = "TRANSFERENCIA",
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color         = HadesPurple
                    )
                    Text(
                        text     = "Envía dinero de forma segura",
                        fontSize = 12.sp,
                        color    = HadesOnDark.copy(alpha = 0.45f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Saldo disponible ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(HadesPurple.copy(alpha = 0.6f), HadesNavyDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text          = "SALDO DISPONIBLE",
                            fontSize      = 9.sp,
                            letterSpacing = 1.sp,
                            color         = HadesOnDark.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        HadesBalanceText(
                            balance  = senderBalance,
                            visible  = saldoVisible,
                            onToggle = { saldoVisible = !saldoVisible }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HadesCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            tint               = HadesCyan,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Formulario ─────────────────────────────────────────────
            HadesCardBox {
                Text(
                    text          = "> DATOS DE ENVÍO",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = HadesCyan
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesTextField(
                    value         = senderPhone,
                    onValueChange = {},
                    label         = "Desde (tu teléfono)",
                    enabled       = false
                )

                HadesTextField(
                    value         = receiverPhone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() } &&
                            (it.isEmpty() || it[0] == '3')
                        ) onReceiverChange(it)
                    },
                    label        = "Teléfono destinatario",
                    keyboardType = KeyboardType.Phone
                )

                HadesTextField(
                    value         = amount,
                    onValueChange = { if (it.length <= 12) onAmountChange(it) },
                    label         = if (exceedsBalance) "Monto (insuficiente)" else "Monto a enviar",
                    keyboardType  = KeyboardType.Decimal,
                    prefix        = { Text("$", color = if (exceedsBalance) HadesOrange else HadesCyan) }
                )

                AnimatedVisibility(visible = exceedsBalance) {
                    Text(
                        text       = "⚠ Saldo insuficiente para esta transferencia",
                        fontSize   = 11.sp,
                        color      = HadesOrange,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(top = 2.dp, start = 4.dp)
                    )
                }

                HadesTextField(
                    value         = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onPinChange(it) },
                    label         = "PIN de confirmación (4 dígitos)",
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )
            }

            // ── Resumen previo al envío ────────────────────────────────
            AnimatedVisibility(
                visible = parsedAmount > 0.0 && !exceedsBalance,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text          = "> RESUMEN",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color         = HadesCyan,
                        modifier      = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    HadesSummaryRow(
                        items = listOf(
                            HadesSummaryItem(
                                label   = "A ENVIAR",
                                valor   = parsedAmount,
                                color   = HadesOrange,
                                prefijo = "- "
                            ),
                            HadesSummaryItem(
                                label   = "QUEDARÁ",
                                valor   = remaining.coerceAtLeast(0.0),
                                color   = HadesCyan,
                                prefijo = "  "
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HadesButton(
                text     = "[ REVISAR TRANSFERENCIA ]",
                onClick  = onReviewClick,
                enabled  = isFormValid,
                cargando = cargando
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ── Modal de confirmación ──────────────────────────────────────
        if (showConfirm) {
            ConfirmTransferSheet(
                senderPhone   = senderPhone,
                receiverPhone = receiverPhone,
                amount        = parsedAmount,
                onConfirm     = onConfirmTransfer,
                onDismiss     = onDismissConfirm
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmTransferSheet(
    senderPhone: String,
    receiverPhone: String,
    amount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HadesOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint               = HadesOrange,
                    modifier           = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = "¿Confirmar transferencia?",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Black,
                color      = HadesOnDark,
                textAlign  = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text      = "Esta acción no se puede deshacer",
                fontSize  = 12.sp,
                color     = HadesOnDark.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text       = "$${String.format(Locale.US, "%,.2f", amount)}",
                fontSize   = 36.sp,
                fontWeight = FontWeight.Black,
                color      = HadesOrange
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HadesNavy.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "DESDE", fontSize = 9.sp, letterSpacing = 1.sp, color = HadesOnDark.copy(alpha = 0.45f))
                        Text(text = senderPhone, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                    }
                    Icon(
                        imageVector        = Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        tint               = HadesCyan.copy(alpha = 0.5f),
                        modifier           = Modifier.size(20.dp).align(Alignment.CenterVertically)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "HACIA", fontSize = 9.sp, letterSpacing = 1.sp, color = HadesOnDark.copy(alpha = 0.45f))
                        Text(text = receiverPhone, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HadesButton(
                text    = "[ CONFIRMAR ENVIO ]",
                onClick = onConfirm
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "Cancelar",
                    color    = HadesOnDark.copy(alpha = 0.45f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
