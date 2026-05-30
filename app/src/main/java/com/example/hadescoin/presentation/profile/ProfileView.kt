package com.example.hadescoin.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.presentation.auth.login.RecoverPinDialog
import com.example.hadescoin.presentation.auth.login.ResetPinDialog
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*

@Composable
fun ProfileView(
    navController: NavController,
    phoneNumber: String,
    viewModel: ProfileViewModel = viewModel()
) {
    val user           by viewModel.user.observeAsState()
    val cargando       by viewModel.cargando.observeAsState(false)
    val mensajeExito   by viewModel.mensajeExito.observeAsState()
    val mensajeError   by viewModel.mensajeError.observeAsState()
    val pinRecuperado  by viewModel.pinRecuperado.observeAsState()

    var showPinDialog      by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showRecoverDialog  by remember { mutableStateOf(false) }
    var showResetDialog    by remember { mutableStateOf(false) }
    var recoveredPinMsg    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(phoneNumber) {
        viewModel.cargarPerfil(phoneNumber)
    }

    // Cuando se recupera el PIN, mostrar el dialogo con el PIN encontrado
    LaunchedEffect(pinRecuperado) {
        pinRecuperado?.let {
            recoveredPinMsg = "Tu PIN es: $it"
            showRecoverDialog = false
        }
    }

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            user?.let { u ->
                val displayGreeting = if (u.nickname.isNotBlank()) u.nickname
                                      else u.fullName.split(" ").firstOrNull() ?: ""
                Text(
                    text          = "HOLA, $displayGreeting".uppercase(),
                    fontSize      = 28.sp,
                    fontWeight    = FontWeight.Black,
                    color         = HadesPurple,
                    letterSpacing = 4.sp
                )
            } ?: Text(
                text          = "MI PERFIL",
                fontSize      = 28.sp,
                fontWeight    = FontWeight.Black,
                color         = HadesPurple,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            user?.let { u ->
                HadesCardBox {
                    ProfileItem(label = "Nombre Completo",    value = u.fullName)
                    ProfileItem(label = "Apodo",              value = u.nickname.ifBlank { "No asignado" }, isMissing = u.nickname.isBlank())
                    ProfileItem(label = "Número de Documento",value = u.documentNumber, isMissing = u.documentNumber.isBlank())
                    ProfileItem(label = "Teléfono",           value = u.phoneNumber)
                    ProfileItem(label = "Miembro desde",      value = u.createdAt.take(10))
                    ProfileItem(label = "PIN",                value = "****")

                    Spacer(modifier = Modifier.height(16.dp))

                    HadesButton(
                        text     = "Cambiar PIN",
                        onClick  = { showPinDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Recuperar PIN — reutiliza RecoverPinDialog y ResetPinDialog del login
                    TextButton(
                        onClick  = { showRecoverDialog = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text       = "¿Olvidaste tu PIN?",
                            color      = HadesCyan,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HadesButton(
                        text     = if (u.nickname.isBlank()) "Agregar Apodo" else "Cambiar Apodo",
                        onClick  = { showNicknameDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HadesButton(
                text     = "Volver",
                onClick  = { navController.popBackStack() },
                modifier = Modifier.width(150.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────
    if (cargando) ShowLoadingAlertDialog()

    if (mensajeExito != null) {
        ShowMessageAlertDialog(
            onConfirmation = { viewModel.clearMessages() },
            dialogTitle    = "Éxito",
            dialogText     = mensajeExito!!
        )
    }

    if (mensajeError != null) {
        ShowMessageAlertDialog(
            onConfirmation = { viewModel.clearMessages() },
            dialogTitle    = "Error",
            dialogText     = mensajeError!!
        )
    }

    // Dialogo: cambiar PIN (requiere saber el PIN actual)
    if (showPinDialog) {
        ChangePinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { actual, nuevo, confirm ->
                viewModel.cambiarPin(phoneNumber, actual, nuevo, confirm)
                showPinDialog = false
            }
        )
    }

    // Dialogo: recuperar PIN con telefono + documento
    // Reutiliza RecoverPinDialog definido en LoginView.kt
    if (showRecoverDialog) {
        RecoverPinDialog(
            onDismiss = { showRecoverDialog = false; viewModel.clearMessages() },
            onRecover = { phone, doc ->
                viewModel.recuperarPin(phone, doc)
            }
        )
    }

    // Dialogo: muestra el PIN encontrado y ofrece cambiarlo
    if (recoveredPinMsg != null) {
        AlertDialog(
            onDismissRequest = { recoveredPinMsg = null; viewModel.clearMessages() },
            containerColor   = HadesNavyDark,
            title = { Text("PIN Recuperado", color = HadesPurple, fontWeight = FontWeight.Bold) },
            text  = {
                Column {
                    Text(recoveredPinMsg!!, color = HadesOnDark)
                    Spacer(Modifier.height(12.dp))
                    Text("¿Deseas cambiarlo por uno nuevo ahora?", fontSize = 13.sp, color = HadesCyan)
                }
            },
            confirmButton = {
                TextButton(onClick = { recoveredPinMsg = null; showResetDialog = true }) {
                    Text("CAMBIAR PIN", color = HadesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recoveredPinMsg = null; viewModel.clearMessages() }) {
                    Text("ENTENDIDO", color = HadesCyan)
                }
            }
        )
    }

    // Dialogo: ingresar el nuevo PIN tras recuperacion
    // Reutiliza ResetPinDialog definido en LoginView.kt
    if (showResetDialog) {
        ResetPinDialog(
            onDismiss = { showResetDialog = false; viewModel.clearMessages() },
            onReset   = { nuevoPin ->
                viewModel.resetearPinDespuesDeRecuperar(nuevoPin)
                showResetDialog = false
            }
        )
    }

    if (showNicknameDialog) {
        ChangeNicknameDialog(
            currentNickname = user?.nickname ?: "",
            onDismiss       = { showNicknameDialog = false },
            onConfirm       = { nuevo ->
                viewModel.actualizarApodo(phoneNumber, nuevo)
                showNicknameDialog = false
            }
        )
    }
}

@Composable
fun ProfileItem(label: String, value: String, isMissing: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text          = label,
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Bold,
            color         = HadesCyan,
            letterSpacing = 1.sp
        )
        Text(
            text       = if (isMissing) "$value (Pendiente)" else value,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isMissing) HadesOrange else HadesOnDark
        )
    }
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var pinActual    by remember { mutableStateOf("") }
    var pinNuevo     by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = { Text("Cambiar PIN", color = HadesPurple) },
        text  = {
            Column {
                HadesTextField(
                    value         = pinActual,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinActual = it },
                    label         = "PIN Actual",
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )
                HadesTextField(
                    value         = pinNuevo,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinNuevo = it },
                    label         = "Nuevo PIN",
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )
                HadesTextField(
                    value         = confirmacion,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmacion = it },
                    label         = "Confirmar Nuevo PIN",
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pinActual, pinNuevo, confirmacion) }) {
                Text("ACEPTAR", color = HadesCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}

@Composable
fun ChangeNicknameDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nuevoApodo by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = { Text("Actualizar Apodo", color = HadesPurple) },
        text  = {
            HadesTextField(
                value         = nuevoApodo,
                onValueChange = { nuevoApodo = it },
                label         = "Tu Apodo"
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nuevoApodo) }) {
                Text("GUARDAR", color = HadesCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}
