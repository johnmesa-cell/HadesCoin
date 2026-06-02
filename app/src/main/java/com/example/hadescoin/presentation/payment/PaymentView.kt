package com.example.hadescoin.presentation.payment

import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.domain.model.ServiceItem
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.presentation.utils.BiometricHelper
import com.example.hadescoin.ui.theme.*

@Composable
fun PaymentView(
    phoneNumber: String,
    navController: NavController,
    viewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val cargando by viewModel.cargando.observeAsState(false)
    val pagoExitoso by viewModel.pagoExitoso.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val biometriaActiva by viewModel.biometriaActiva.observeAsState(false)

    var pasoActual by remember { mutableStateOf(1) }
    var servicioSeleccionado by remember { mutableStateOf<ServiceItem?>(null) }
    var referencia by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val errorReferencia = stringResource(R.string.payment_error_referencia)
    val errorMonto = stringResource(R.string.payment_error_monto)
    val errorTitle = stringResource(R.string.dialog_error_title)
    
    val bioTitulo = stringResource(R.string.payment_paso3_titulo)
    val bioSubtitulo = stringResource(R.string.withdraw_biometry_confirmation_subtitle)

    LaunchedEffect(error) {
        error?.let { mensajeError = it; showError = true }
    }

    HadesScreen {
        when (pasoActual) {
            1 -> Paso1_SeleccionarServicio(
                servicios = viewModel.servicios,
                onServicioSeleccionado = {
                    servicioSeleccionado = it
                    pasoActual = 2
                }
            )
            2 -> Paso2_DatosDelPago(
                servicioSeleccionado = servicioSeleccionado,
                referencia = referencia,
                monto = monto,
                onReferenciaChange = { referencia = it },
                onMontoChange = { monto = it },
                onContinuar = {
                    if (referencia.isBlank()) {
                        mensajeError = errorReferencia
                        showError = true
                    } else if (monto.toDoubleOrNull() == null || monto.toDouble() <= 0) {
                        mensajeError = errorMonto
                        showError = true
                    } else {
                        pasoActual = 3
                    }
                },
                onVolver = { navController.popBackStack() }
            )
            3 -> Paso3_ConfirmarPago(
                servicioSeleccionado = servicioSeleccionado,
                referencia = referencia,
                monto = monto,
                pin = pin,
                onPinChange = { pin = it },
                biometriaActiva = biometriaActiva,
                cargando = cargando,
                activity = activity,
                bioTitulo = bioTitulo,
                bioSubtitulo = bioSubtitulo,
                onPagar = { conHuella ->
                    val montoDouble = monto.toDoubleOrNull() ?: 0.0
                    viewModel.pagar(
                        phoneNumber = phoneNumber,
                        servicioId = servicioSeleccionado?.id ?: "",
                        amount = montoDouble,
                        referencia = referencia,
                        pin = pin,
                        autenticadoConHuella = conHuella
                    )
                },
                onVolver = { pasoActual = 2 }
            )
        }
    }

    // Diálogo de éxito
    if (pagoExitoso) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearPagoExitoso()
                navController.popBackStack()
            },
            containerColor = HadesNavyDark,
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = HadesCyan,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(stringResource(R.string.payment_exito_titulo), color = HadesCyan) },
            text = { Text(stringResource(R.string.payment_exito_mensaje), color = HadesOnDark) },
            confirmButton = {
                HadesButton(
                    text = stringResource(R.string.btn_accept),
                    onClick = {
                        viewModel.clearPagoExitoso()
                        navController.popBackStack()
                    }
                )
            }
        )
    }

    // Diálogo de error
    if (showError && mensajeError.isNotEmpty()) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false; viewModel.clearError() },
            dialogTitle = errorTitle,
            dialogText = mensajeError
        )
    }
}

@Composable
private fun Paso1_SeleccionarServicio(
    servicios: List<ServiceItem>,
    onServicioSeleccionado: (ServiceItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.payment_paso1_titulo),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HadesCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        items(servicios) { servicio ->
            ServicioCard(servicio) {
                onServicioSeleccionado(servicio)
            }
        }
    }
}

@Composable
private fun ServicioCard(
    servicio: ServiceItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(HadesNavyDark)
            .border(1.dp, HadesOnDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HadesCyan.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = servicio.icono,
                    contentDescription = null,
                    tint = HadesCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = stringResource(servicio.nombreRes),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HadesOnDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun Paso2_DatosDelPago(
    servicioSeleccionado: ServiceItem?,
    referencia: String,
    monto: String,
    onReferenciaChange: (String) -> Unit,
    onMontoChange: (String) -> Unit,
    onContinuar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (servicioSeleccionado != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(HadesNavyDark)
                    .border(1.dp, HadesOnDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = servicioSeleccionado.icono,
                        contentDescription = null,
                        tint = HadesCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(servicioSeleccionado.nombreRes),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HadesCyan
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.payment_paso2_titulo),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = HadesOnDark.copy(alpha = 0.8f)
        )

        HadesTextField(
            value = referencia,
            onValueChange = onReferenciaChange,
            label = stringResource(R.string.payment_referencia_hint),
            keyboardType = KeyboardType.Number
        )

        HadesTextField(
            value = monto,
            onValueChange = onMontoChange,
            label = stringResource(R.string.payment_monto_hint),
            keyboardType = KeyboardType.Decimal
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HadesButton(
                text = stringResource(R.string.payment_btn_volver),
                onClick = onVolver,
                modifier = Modifier.weight(1f)
            )
            HadesButton(
                text = stringResource(R.string.payment_btn_continuar),
                onClick = onContinuar,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Paso3_ConfirmarPago(
    servicioSeleccionado: ServiceItem?,
    referencia: String,
    monto: String,
    pin: String,
    onPinChange: (String) -> Unit,
    biometriaActiva: Boolean,
    cargando: Boolean,
    activity: FragmentActivity?,
    bioTitulo: String,
    bioSubtitulo: String,
    onPagar: (Boolean) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.payment_paso3_titulo),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = HadesOnDark.copy(alpha = 0.8f)
        )

        if (servicioSeleccionado != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(HadesNavyDark)
                    .border(1.dp, HadesOnDark.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            imageVector = servicioSeleccionado.icono,
                            contentDescription = null,
                            tint = HadesCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(text = stringResource(R.string.action_pay), fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f))
                            Text(text = stringResource(servicioSeleccionado.nombreRes), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HadesOnDark.copy(alpha = 0.05f)))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = stringResource(R.string.payment_resumen_referencia), fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f))
                            Text(text = referencia, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesOnDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = stringResource(R.string.payment_resumen_monto), fontSize = 10.sp, color = HadesOnDark.copy(alpha = 0.5f))
                            Text(text = monto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadesCyan)
                        }
                    }
                }
            }
        }

        if (!biometriaActiva) {
            HadesPinInput(
                value = pin,
                onValueChange = onPinChange,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(HadesCyan.copy(alpha = 0.05f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            if (activity != null) {
                                BiometricHelper.mostrar(
                                    activity = activity,
                                    titulo = bioTitulo,
                                    subtitulo = bioSubtitulo,
                                    onExito = { onPagar(true) },
                                    onError = {}
                                )
                            }
                        },
                        modifier = Modifier.size(64.dp).background(HadesCyan.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = HadesCyan, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.cd_authenticate_fingerprint), fontSize = 12.sp, color = HadesCyan)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HadesButton(
                text = stringResource(R.string.payment_btn_volver),
                onClick = onVolver,
                modifier = Modifier.weight(1f),
                enabled = !cargando
            )
            if (!biometriaActiva) {
                HadesButton(
                    text = stringResource(R.string.payment_btn_pagar),
                    onClick = { onPagar(false) },
                    modifier = Modifier.weight(1f),
                    enabled = !cargando && pin.length == 4,
                    cargando = cargando
                )
            }
        }
    }
}
