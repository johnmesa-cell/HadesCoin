package com.example.hadescoin.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.presentation.utils.BiometricHelper
import com.example.hadescoin.presentation.utils.getInitials
import com.example.hadescoin.ui.theme.*

@Composable
fun ProfileView(
    navController: NavController,
    phoneNumber: String,
    viewModel: ProfileViewModel = viewModel()
) {
    val context        = LocalContext.current
    val user           by viewModel.user.observeAsState()
    val cargando       by viewModel.cargando.observeAsState(false)
    val mensajeExito   by viewModel.mensajeExito.observeAsState()
    val mensajeError   by viewModel.mensajeError.observeAsState()
    val codigoGenerado by viewModel.codigoGenerado.observeAsState()
    val codigoValidado by viewModel.codigoValidado.observeAsState(false)
    val noLeidas       by viewModel.notificacionesNoLeidas.observeAsState(0)
    val biometriaActiva by viewModel.biometriaActiva.observeAsState(false)
    val dispositivoTieneBiometria = remember { BiometricHelper.isDisponible(context) }

    var showPinDialog      by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showRecoveryFlow   by remember { mutableStateOf(false) }

    LaunchedEffect(phoneNumber) { viewModel.cargarPerfil(phoneNumber); viewModel.cargarNoLeidas(phoneNumber) }

    HadesScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── A. Header del perfil ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HadesPurple, HadesNavyDark)
                        )
                    )
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(HadesPurpleGlow, HadesNavyDark)
                                )
                            )
                            .border(2.dp, HadesCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(user?.fullName),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = HadesOnDark
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val displayName = user?.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?: user?.fullName?.split(" ")?.firstOrNull()
                        ?: ""
                    Text(
                        text = displayName.uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = HadesOnDark,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = user?.phoneNumber ?: "—",
                        fontSize = 13.sp,
                        color = HadesOnDark.copy(alpha = 0.55f)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                user?.let { u ->
                    // ── B. Sección INFORMACIÓN PERSONAL ───────────────────────
                    Spacer(modifier = Modifier.height(20.dp))
                    HadesSectionHeader(text = stringResource(R.string.profile_personal_section))
                    Spacer(modifier = Modifier.height(8.dp))
                    HadesCardBox {
                        ProfileItem(label = stringResource(R.string.label_full_name_profile),     value = u.fullName)
                        ProfileDivider()
                        ProfileItem(label = stringResource(R.string.label_document_profile), value = u.documentNumber, isMissing = u.documentNumber.isBlank())
                        ProfileDivider()
                        ProfileItem(label = stringResource(R.string.label_phone_profile),            value = u.phoneNumber)
                        ProfileDivider()
                        ProfileItem(label = stringResource(R.string.label_member_since_profile),       value = u.createdAt.take(10))
                    }

                    // ── C. Sección SEGURIDAD ──────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    HadesSectionHeader(text = stringResource(R.string.profile_security_section))
                    Spacer(modifier = Modifier.height(8.dp))
                    HadesCardBox {
                        ProfileItem(label = stringResource(R.string.label_pin_profile), value = "••••")
                        ProfileDivider()
                        HadesButton(
                            text = stringResource(R.string.btn_change_pin),
                            onClick = { showPinDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = { showRecoveryFlow = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                stringResource(R.string.btn_forgot_pin),
                                color = HadesCyan,
                                fontSize = 12.sp
                            )
                        }
                        if (dispositivoTieneBiometria) {
                            ProfileDivider()
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, tint = if (biometriaActiva) HadesCyan else HadesOnDark.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(text = stringResource(R.string.profile_biometry_title), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = HadesOnDark)
                                        Text(text = if (biometriaActiva) stringResource(R.string.profile_biometry_active) else stringResource(R.string.profile_biometry_inactive), fontSize = 11.sp, color = if (biometriaActiva) HadesCyan else HadesOnDark.copy(alpha = 0.4f))
                                    }
                                }
                                Switch(checked = biometriaActiva, onCheckedChange = { viewModel.setBiometriaActiva(it) }, colors = SwitchDefaults.colors(checkedThumbColor = HadesCyan, checkedTrackColor = HadesCyan.copy(alpha = 0.3f), uncheckedThumbColor = HadesOnDark.copy(alpha = 0.4f), uncheckedTrackColor = HadesOnDark.copy(alpha = 0.1f)))
                            }
                        }
                    }

                    // ── D. Sección PERSONALIZACIÓN ──────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    HadesSectionHeader(text = stringResource(R.string.profile_customize_section))
                    Spacer(modifier = Modifier.height(8.dp))
                    HadesCardBox {
                        ProfileItem(
                            label = stringResource(R.string.label_nickname_profile),
                            value = u.nickname.ifBlank {
                                stringResource(R.string.profile_nickname_not_assigned)
                            },
                            isMissing = u.nickname.isBlank()
                        )
                        ProfileDivider()
                        HadesButton(
                            text = if (u.nickname.isBlank())
                                stringResource(R.string.btn_add_nickname)
                            else stringResource(R.string.btn_edit_nickname),
                            onClick = { showNicknameDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { navController.navigate("notifications/$phoneNumber") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HadesCyan)
                        ) {
                            BadgedBox(badge = {
                                if (noLeidas > 0) Badge { Text(noLeidas.toString()) }
                            }) {
                                Icon(Icons.Filled.Notifications, null, Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_notifications))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HadesButton(text = stringResource(R.string.btn_back), onClick = { navController.popBackStack() }, modifier = Modifier.width(150.dp).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (cargando) ShowLoadingAlertDialog()
    mensajeExito?.let { ShowMessageAlertDialog(onConfirmation = { viewModel.clearMessages() }, dialogTitle = stringResource(R.string.dialog_success_title),  dialogText = it) }
    mensajeError?.let { ShowMessageAlertDialog(onConfirmation = { viewModel.clearMessages() }, dialogTitle = stringResource(R.string.dialog_error_title), dialogText = it) }

    if (showPinDialog) ChangePinDialog(onDismiss = { showPinDialog = false }, onConfirm = { actual, nuevo, confirm -> viewModel.cambiarPin(phoneNumber, actual, nuevo, confirm); showPinDialog = false })
    if (showRecoveryFlow) PinRecoveryFlow(codigoGenerado = codigoGenerado, codigoValidado = codigoValidado, onDismiss = { showRecoveryFlow = false }, onGenerate = { phone, doc -> viewModel.generarCodigoVerificacion(phone, doc) }, onValidate = { code -> viewModel.validarCodigo(code) }, onReset = { newPin -> viewModel.resetearPin(newPin) }, onClearState = { viewModel.clearMessages() })
    if (showNicknameDialog) ChangeNicknameDialog(currentNickname = user?.nickname.orEmpty(), onDismiss = { showNicknameDialog = false }, onConfirm = { nuevo -> viewModel.actualizarApodo(phoneNumber, nuevo); showNicknameDialog = false })
}

@Composable
fun ProfileItem(label: String, value: String, isMissing: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HadesCyan, letterSpacing = 1.sp)
        Text(text = if (isMissing) "$value ${stringResource(R.string.profile_pending_status)}" else value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (isMissing) HadesOrange else HadesOnDark)
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(1.dp)
            .background(HadesOnDark.copy(alpha = 0.08f))
    )
}

@Composable
fun ChangePinDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var pinActual    by remember { mutableStateOf("") }
    var pinNuevo     by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = HadesNavyDark,
        title = { Text(stringResource(R.string.dialog_change_pin_title), color = HadesPurple) },
        text  = { Column {
            HadesTextField(value = pinActual,    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinActual    = it }, label = stringResource(R.string.label_current_pin),          isPassword = true, keyboardType = KeyboardType.NumberPassword)
            HadesTextField(value = pinNuevo,     onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinNuevo     = it }, label = stringResource(R.string.label_new_pin),           isPassword = true, keyboardType = KeyboardType.NumberPassword)
            HadesTextField(value = confirmacion, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmacion = it }, label = stringResource(R.string.label_confirm_pin), isPassword = true, keyboardType = KeyboardType.NumberPassword)
        } },
        confirmButton = { TextButton(onClick = { onConfirm(pinActual, pinNuevo, confirmacion) }) { Text(stringResource(R.string.btn_accept),   color = HadesCyan) } },
        dismissButton = { TextButton(onClick = onDismiss)                                        { Text(stringResource(R.string.btn_cancel), color = HadesOnDark.copy(alpha = 0.6f)) } }
    )
}

@Composable
fun ChangeNicknameDialog(currentNickname: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var nuevoApodo by remember { mutableStateOf(currentNickname) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = HadesNavyDark,
        title = { Text(stringResource(R.string.dialog_update_nickname_title), color = HadesPurple) },
        text  = { HadesTextField(value = nuevoApodo, onValueChange = { nuevoApodo = it }, label = stringResource(R.string.label_your_nickname)) },
        confirmButton = { TextButton(onClick = { onConfirm(nuevoApodo) }) { Text(stringResource(R.string.btn_save),   color = HadesCyan) } },
        dismissButton = { TextButton(onClick = onDismiss)                  { Text(stringResource(R.string.btn_cancel), color = HadesOnDark.copy(alpha = 0.6f)) } }
    )
}
