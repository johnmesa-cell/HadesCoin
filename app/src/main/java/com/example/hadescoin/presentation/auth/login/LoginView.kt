package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.ui.theme.*

@Composable
fun LoginView(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val haySession      by viewModel.haySessionGuardada.collectAsState()
    val telefonoLocal   by viewModel.telefonoGuardado.collectAsState()
    val nombreLocal     by viewModel.nombreGuardado.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var pin         by remember { mutableStateOf("") }

    val cargando          by viewModel.cargando.observeAsState(false)
    val loginExitoso      by viewModel.loginExitoso.observeAsState()
    val loginError        by viewModel.loginError.observeAsState()
    val pinRecuperado     by viewModel.pinRecuperado.observeAsState()
    val errorRecuperacion by viewModel.errorRecuperacion.observeAsState()

    var mensajeError       by remember { mutableStateOf("") }
    var showError          by remember { mutableStateOf(false) }
    var showRecoverDialog  by remember { mutableStateOf(false) }
    var showResetDialog    by remember { mutableStateOf(false) }
    var recoveredPinMsg    by remember { mutableStateOf<String?>(null) }
    var phoneForReset      by remember { mutableStateOf("") }

    // Cuando hay sesión guardada, precargamos el teléfono en el campo
    LaunchedEffect(haySession, telefonoLocal) {
        if (haySession && telefonoLocal.isNotBlank()) phoneNumber = telefonoLocal
    }

    LaunchedEffect(loginExitoso) {
        loginExitoso?.let { phone ->
            navController.navigate("home/$phone") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    LaunchedEffect(loginError) { loginError?.let { mensajeError = it; showError = true } }
    LaunchedEffect(pinRecuperado) { pinRecuperado?.let { recoveredPinMsg = "Tu PIN es: $it"; showRecoverDialog = false } }
    LaunchedEffect(errorRecuperacion) { errorRecuperacion?.let { mensajeError = it; showError = true } }

    if (haySession && telefonoLocal.isNotBlank()) {
        // Modo inteligente — sesión guardada
        LoginInteligenteContent(
            nombre             = nombreLocal,
            telefonoPrecargado = telefonoLocal,
            pin                = pin,
            cargando           = cargando,
            onPinChange        = { value -> if (value.length <= 4 && value.all { c -> c.isDigit() }) { pin = value; viewModel.clearError() } },
            onLoginClick       = { viewModel.login(telefonoLocal, pin) },
            onOtroUsuario      = { pin = ""; viewModel.olvidarSesionGuardada() },
            onForgotPin        = { showRecoverDialog = true }
        )
    } else {
        // Modo normal — sin sesión guardada
        LoginContent(
            phoneNumber   = phoneNumber,
            pin           = pin,
            cargando      = cargando,
            loginError    = loginError,
            onPhoneChange = { value ->
                if (value.length <= 10 && value.all { c -> c.isDigit() } && (value.isEmpty() || value[0] == '3')) {
                    phoneNumber = value; viewModel.clearError()
                }
            },
            onPinChange   = { value ->
                if (value.length <= 4 && value.all { c -> c.isDigit() }) { pin = value; viewModel.clearError() }
            },
            onLoginClick         = { viewModel.login(phoneNumber, pin) },
            onRegisterClick      = { navController.navigate("register") },
            onForgotPasswordClick = { showRecoverDialog = true }
        )
    }

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false; viewModel.clearError() },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensajeError
        )
    }

    if (recoveredPinMsg != null) {
        AlertDialog(
            onDismissRequest = { recoveredPinMsg = null; viewModel.clearError() },
            containerColor   = HadesNavyDark,
            title = { Text("PIN Recuperado", color = HadesPurple, fontWeight = FontWeight.Bold) },
            text  = {
                Column {
                    Text(recoveredPinMsg!!, color = HadesOnDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("¿Deseas cambiarlo por uno nuevo ahora?", fontSize = 13.sp, color = HadesCyan)
                }
            },
            confirmButton = {
                TextButton(onClick = { recoveredPinMsg = null; showResetDialog = true }) {
                    Text("CAMBIAR PIN", color = HadesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recoveredPinMsg = null; viewModel.clearError() }) {
                    Text("ENTENDIDO", color = HadesCyan)
                }
            }
        )
    }

    if (showRecoverDialog) {
        RecoverPinDialog(
            onDismiss = { showRecoverDialog = false; viewModel.clearError() },
            onRecover = { phone, doc -> phoneForReset = phone; viewModel.recuperarPin(phone, doc) }
        )
    }

    if (showResetDialog) {
        ResetPinDialog(
            onDismiss = { showResetDialog = false; viewModel.clearError() },
            onReset   = { nuevoPin -> viewModel.resetearPinDespuesDeRecuperar(phoneForReset, nuevoPin); showResetDialog = false }
        )
    }
}

// ─── Pantalla inteligente (sesión guardada) ────────────────────────────────
@Composable
fun LoginInteligenteContent(
    nombre: String,
    telefonoPrecargado: String,
    pin: String,
    cargando: Boolean,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onOtroUsuario: () -> Unit,
    onForgotPin: () -> Unit
) {
    HadesBackground {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Image(
                painter            = painterResource(id = R.drawable.ic_hadescoin_logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier           = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Avatar + saludo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(HadesPurple.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .then(Modifier.background(Color.Transparent)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Person,
                    contentDescription = null,
                    tint               = HadesPurple,
                    modifier           = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = "¡Bienvenido de nuevo!",
                fontSize   = 13.sp,
                color      = HadesCyan.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            Text(
                text       = nombre.ifBlank { "Usuario" },
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = HadesPurple,
                textAlign  = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Teléfono precargado (solo lectura)
            Box(
                modifier = Modifier
                    .background(HadesNavyDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text      = telefonoPrecargado,
                    fontSize  = 15.sp,
                    color     = HadesOnDark.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HadesCardBox {
                Text(
                    text          = "INGRESA TU PIN",
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = HadesCyan
                )

                HadesTextField(
                    value         = pin,
                    onValueChange = onPinChange,
                    label         = stringResource(R.string.label_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesButton(
                    text         = stringResource(R.string.btn_login),
                    textCargando = stringResource(R.string.btn_login_loading),
                    onClick      = onLoginClick,
                    enabled      = pin.length == 4,
                    cargando     = cargando
                )

                TextButton(
                    onClick  = onForgotPin,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text      = "¿Olvidaste tu PIN?",
                        color     = HadesCyan,
                        fontSize  = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón secundario — otro usuario
            OutlinedButton(
                onClick  = onOtroUsuario,
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = HadesOnDark.copy(alpha = 0.6f)),
                border   = androidx.compose.foundation.BorderStroke(1.dp, HadesOnDark.copy(alpha = 0.2f)),
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "Iniciar sesión como otro usuario",
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ─── Pantalla normal (sin sesión guardada) ─────────────────────────────────
@Composable
fun LoginContent(
    phoneNumber: String,
    pin: String,
    cargando: Boolean,
    loginError: String?,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    HadesBackground {
        Column(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Image(
                painter            = painterResource(id = R.drawable.ic_hadescoin_logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier           = Modifier.size(110.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text          = stringResource(R.string.login_title),
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 6.sp,
                color         = HadesPurple,
                textAlign     = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent)))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text          = stringResource(R.string.login_subtitle),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 2.sp,
                color         = HadesCyan.copy(alpha = 0.7f),
                textAlign     = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            HadesCardBox {
                Text(
                    text          = stringResource(R.string.login_section_header),
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = HadesCyan
                )
                HadesTextField(
                    value         = phoneNumber,
                    onValueChange = onPhoneChange,
                    label         = stringResource(R.string.label_phone_number),
                    keyboardType  = KeyboardType.Number,
                    isError       = loginError != null && phoneNumber.isBlank()
                )
                HadesTextField(
                    value         = pin,
                    onValueChange = onPinChange,
                    label         = stringResource(R.string.label_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword,
                    isError       = loginError != null && pin.length < 4
                )
                Spacer(modifier = Modifier.height(4.dp))
                HadesButton(
                    text         = stringResource(R.string.btn_login),
                    textCargando = stringResource(R.string.btn_login_loading),
                    onClick      = onLoginClick,
                    enabled      = phoneNumber.length >= 5 && pin.length == 4,
                    cargando     = cargando
                )
                TextButton(
                    onClick  = onForgotPasswordClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("¿Olvidaste tu PIN?", color = HadesCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = stringResource(R.string.text_no_account),
                    fontSize = 13.sp,
                    color    = HadesOnDark.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick        = onRegisterClick,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text          = stringResource(R.string.btn_register_link),
                        fontSize      = 13.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color         = HadesOrange
                    )
                }
            }
        }
    }
}

// ─── Diálogos de recuperación ────────────────────────────────────────────────
@Composable
fun ResetPinDialog(onDismiss: () -> Unit, onReset: (String) -> Unit) {
    var nuevoPin     by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = { Text("Nuevo PIN", color = HadesPurple, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HadesTextField(
                    value = nuevoPin, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) nuevoPin = it },
                    label = "Ingresa tu nuevo PIN", isPassword = true, keyboardType = KeyboardType.NumberPassword
                )
                HadesTextField(
                    value = confirmacion, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmacion = it },
                    label = "Confirma tu nuevo PIN", isPassword = true, keyboardType = KeyboardType.NumberPassword
                )
            }
        },
        confirmButton = {
            TextButton(enabled = nuevoPin.length == 4 && nuevoPin == confirmacion, onClick = { onReset(nuevoPin) }) {
                Text("ACTUALIZAR", color = HadesCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}

@Composable
fun RecoverPinDialog(onDismiss: () -> Unit, onRecover: (String, String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var doc   by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = HadesNavyDark,
        title = { Text("Recuperar PIN", color = HadesPurple, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa tus datos para recuperar el PIN", color = HadesOnDark, fontSize = 14.sp)
                HadesTextField(
                    value = phone, onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    label = "Teléfono", keyboardType = KeyboardType.Number
                )
                HadesTextField(
                    value = doc, onValueChange = { if (it.all { c -> c.isDigit() }) doc = it },
                    label = "Número de Documento", keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = { TextButton(onClick = { onRecover(phone, doc) }) { Text("RECUPERAR", color = HadesCyan, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}

// ─── Previews ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "Login — normal")
@Composable
fun LoginViewPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "", pin = "", cargando = false, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {}, onForgotPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — inteligente")
@Composable
fun LoginInteligentePreview() {
    HadesCoinTheme {
        LoginInteligenteContent(
            nombre             = "Juan Pérez",
            telefonoPrecargado = "3001234567",
            pin                = "",
            cargando           = false,
            onPinChange        = {},
            onLoginClick       = {},
            onOtroUsuario      = {},
            onForgotPin        = {}
        )
    }
}
