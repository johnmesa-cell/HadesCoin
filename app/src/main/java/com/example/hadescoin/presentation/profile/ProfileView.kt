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
    val codigoGenerado by viewModel.codigoGenerado.observeAsState()
    val codigoValidado by viewModel.codigoValidado.observeAsState(false)

    var showPinDialog      by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showRecoveryFlow   by remember { mutableStateOf(false) }

    LaunchedEffect(phoneNumber) { viewModel.cargarPerfil(phoneNumber) }

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
                val displayGreeting = u.nickname.ifBlank { u.fullName.split(" ").firstOrNull() ?: "" }
                Text(
                    text       = "HOLA, $displayGreeting".uppercase(),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Black,
                    color      = HadesPurple,
                    letterSpacing = 4.sp
                )
            } ?: Text(text = "MI PERFIL", fontSize = 28.sp, fontWeight = FontWeight.Black, color = HadesPurple, letterSpacing = 4.sp)

            Spacer(modifier = Modifier.height(24.dp))

            user?.let { u ->
                HadesCardBox {
                    ProfileItem(label = "Nombre Completo",     value = u.fullName)
                    ProfileItem(label = "Apodo",               value = u.nickname.ifBlank { "No asignado" }, isMissing = u.nickname.isBlank())
                    ProfileItem(label = "Número de Documento", value = u.documentNumber, isMissing = u.documentNumber.isBlank())
                    ProfileItem(label = "Teléfono",            value = u.phoneNumber)
                    ProfileItem(label = "Miembro desde",       value = u.createdAt.take(10))
                    ProfileItem(label = "PIN",                 value = "****")

                    Spacer(modifier = Modifier.height(16.dp))

                    HadesButton(text = "Cambiar PIN", onClick = { showPinDialog = true }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = { showRecoveryFlow = true }, modifier = Modifier.align(Alignment.End)) {
                        Text("¿Olvidaste tu PIN?", color = HadesCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    HadesButton(
                        text     = if (u.nickname.isBlank()) "Agregar Apodo" else "Cambiar Apodo",
                        onClick  = { showNicknameDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HadesButton(text = "Volver", onClick = { navController.popBackStack() }, modifier = Modifier.width(150.dp))
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (cargando) ShowLoadingAlertDialog()

    mensajeExito?.let {
        ShowMessageAlertDialog(onConfirmation = { viewModel.clearMessages() }, dialogTitle = "Éxito", dialogText = it)
    }
    mensajeError?.let {
        ShowMessageAlertDialog(onConfirmation = { viewModel.clearMessages() }, dialogTitle = "Error", dialogText = it)
    }

    if (showPinDialog) {
        ChangePinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { actual, nuevo, confirm ->
                viewModel.cambiarPin(phoneNumber, actual, nuevo, confirm)
                showPinDialog = false
            }
        )
    }

    if (showRecoveryFlow) {
        PinRecoveryFlow(
            codigoGenerado = codigoGenerado,
            codigoValidado = codigoValidado,
            onDismiss      = { showRecoveryFlow = false },
            onGenerate     = { phone, doc -> viewModel.generarCodigoVerificacion(phone, doc) },
            onValidate     = { code -> viewModel.validarCodigo(code) },
            onReset        = { newPin -> viewModel.resetearPin(newPin) },
            onClearState   = { viewModel.clearMessages() }
        )
    }

    if (showNicknameDialog) {
        ChangeNicknameDialog(
            currentNickname = user?.nickname.orEmpty(),
            onDismiss       = { showNicknameDialog = false },
            onConfirm       = { nuevo -> viewModel.actualizarApodo(phoneNumber, nuevo); showNicknameDialog = false }
        )
    }
}

@Composable
fun ProfileItem(label: String, value: String, isMissing: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HadesCyan, letterSpacing = 1.sp)
        Text(
            text       = if (isMissing) "$value (Pendiente)" else value,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isMissing) HadesOrange else HadesOnDark
        )
    }
}

@Composable
fun ChangePinDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var pinActual    by remember { mutableStateOf("") }
    var pinNuevo     by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = { Text("Cambiar PIN", color = HadesPurple) },
        text  = {
            Column {
                HadesTextField(value = pinActual,    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinActual    = it }, label = "PIN Actual",          isPassword = true, keyboardType = KeyboardType.NumberPassword)
                HadesTextField(value = pinNuevo,     onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinNuevo     = it }, label = "Nuevo PIN",           isPassword = true, keyboardType = KeyboardType.NumberPassword)
                HadesTextField(value = confirmacion, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmacion = it }, label = "Confirmar Nuevo PIN", isPassword = true, keyboardType = KeyboardType.NumberPassword)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(pinActual, pinNuevo, confirmacion) }) { Text("ACEPTAR",   color = HadesCyan) } },
        dismissButton = { TextButton(onClick = onDismiss)                                        { Text("CANCELAR", color = Color.Gray) } }
    )
}

@Composable
fun ChangeNicknameDialog(currentNickname: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var nuevoApodo by remember { mutableStateOf(currentNickname) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = { Text("Actualizar Apodo", color = HadesPurple) },
        text  = { HadesTextField(value = nuevoApodo, onValueChange = { nuevoApodo = it }, label = "Tu Apodo") },
        confirmButton = { TextButton(onClick = { onConfirm(nuevoApodo) }) { Text("GUARDAR",   color = HadesCyan) } },
        dismissButton = { TextButton(onClick = onDismiss)                  { Text("CANCELAR", color = Color.Gray) } }
    )
}
