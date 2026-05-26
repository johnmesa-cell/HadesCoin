package com.example.hadescoin.presentation.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.*

@Composable
fun TransferView(
    senderPhone: String,
    navController: NavController,
    viewModel: TransferViewModel = viewModel()
) {
    var receiverPhone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    val cargando        by viewModel.cargando.observeAsState(false)
    val transferExitosa by viewModel.transferExitosa.observeAsState()
    val transferError   by viewModel.transferError.observeAsState()

    var mensajeError by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }
    var showExito    by remember { mutableStateOf(false) }

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
        }
    }

    TransferViewContent(
        senderPhone      = senderPhone,
        receiverPhone    = receiverPhone,
        amount           = amount,
        pin              = pin,
        cargando         = cargando,
        onReceiverChange = { receiverPhone = it },
        onAmountChange   = { amount = it },
        onPinChange      = { pin = it },
        onTransferClick  = {
            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
            viewModel.transfer(senderPhone, receiverPhone, parsedAmount, pin)
        },
        onBackClick      = { navController.popBackStack() }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showExito) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showExito = false
                navController.popBackStack()
            },
            dialogTitle    = "Éxito",
            dialogText     = "Transferencia exitosa"
        )
    }

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showError = false
                viewModel.clearError()
            },
            dialogTitle    = "Error",
            dialogText     = mensajeError
        )
    }
}

@Composable
fun TransferViewContent(
    senderPhone: String,
    receiverPhone: String,
    amount: String,
    pin: String,
    cargando: Boolean,
    onReceiverChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onTransferClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(HadesBlack, HadesNavyDark, HadesBlack)
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(HadesOrange, HadesPurpleGlow)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = HadesCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "TRANSFERENCIA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = HadesPurple,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(HadesPurple, HadesCyan.copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HadesNavyDark, HadesNavy)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text(
                        text = "> ENVIAR DINERO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = HadesCyan
                    )

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = HadesCyan,
                        unfocusedBorderColor = HadesPurple.copy(alpha = 0.5f),
                        focusedLabelColor    = HadesCyan,
                        unfocusedLabelColor  = HadesOnDark.copy(alpha = 0.5f),
                        cursorColor          = HadesCyan,
                        focusedTextColor     = HadesOnDark,
                        unfocusedTextColor   = HadesOnDark
                    )

                    OutlinedTextField(
                        value = senderPhone,
                        onValueChange = {},
                        label = { Text("Desde") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors,
                        enabled = false
                    )

                    OutlinedTextField(
                        value = receiverPhone,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() } && (it.isEmpty() || it[0] == '3')) onReceiverChange(it) },
                        label = { Text("Teléfono destinatario") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = fieldColors,
                        prefix = { Text("$") }
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onPinChange(it) },
                        label = { Text("PIN de confirmación") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val isButtonEnabled = !cargando && receiverPhone.length == 10 &&
                                          amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0.0 &&
                                          pin.length == 4

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isButtonEnabled) buttonGradient
                                else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                            )
                    ) {
                        Button(
                            onClick = onTransferClick,
                            enabled = isButtonEnabled,
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = if (cargando) "PROCESANDO..." else "[ TRANSFERIR ]",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}




