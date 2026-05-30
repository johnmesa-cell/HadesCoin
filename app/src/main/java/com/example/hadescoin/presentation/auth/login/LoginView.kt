package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    var phoneNumber by remember { mutableStateOf("") }
    var pin         by remember { mutableStateOf("") }

    val cargando     by viewModel.cargando.observeAsState(false)
    val loginExitoso by viewModel.loginExitoso.observeAsState()
    val loginError   by viewModel.loginError.observeAsState()
    val pinRecuperado by viewModel.pinRecuperado.observeAsState()
    val errorRecuperacion by viewModel.errorRecuperacion.observeAsState()

    var mensajeError by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }
    var showRecoverDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var recoveredPinMessage by remember { mutableStateOf<String?>(null) }
    var phoneForReset by remember { mutableStateOf("") }

    LaunchedEffect(loginExitoso) {
        loginExitoso?.let { phone ->
            navController.navigate("home/$phone") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(loginError) {
        loginError?.let {
            mensajeError = it
            showError = true
        }
    }

    LaunchedEffect(pinRecuperado) {
        pinRecuperado?.let { pin ->
            recoveredPinMessage = "Tu PIN es: $pin"
            // phoneForReset = phoneNumber // SE ELIMINA: Ya no depende del campo de la pantalla de login
            showRecoverDialog = false
        }
    }

    LaunchedEffect(errorRecuperacion) {
        errorRecuperacion?.let {
            mensajeError = it
            showError = true
        }
    }

    LoginContent(
        phoneNumber   = phoneNumber,
        pin           = pin,
        cargando      = cargando,
        loginError    = loginError,
        onPhoneChange = {
            if (it.length <= 10 && it.all { c -> c.isDigit() } && (it.isEmpty() || it[0] == '3')) {
                phoneNumber = it; viewModel.clearError()
            }
        },
        onPinChange   = {
            if (it.length <= 4 && it.all { c -> c.isDigit() }) { pin = it; viewModel.clearError() }
        },
        onLoginClick    = { viewModel.login(phoneNumber, pin) },
        onRegisterClick = { navController.navigate("register") },
        onForgotPasswordClick = { showRecoverDialog = true }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false; viewModel.clearError() },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensajeError
        )
    }

    if (recoveredPinMessage != null) {
        AlertDialog(
            onDismissRequest = { recoveredPinMessage = null; viewModel.clearError() },
            containerColor = HadesNavyDark,
            title = { Text("PIN Recuperado", color = HadesPurple, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(recoveredPinMessage!!, color = HadesOnDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("¿Deseas cambiarlo por uno nuevo ahora?", fontSize = 13.sp, color = HadesCyan)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    recoveredPinMessage = null
                    showResetDialog = true
                }) {
                    Text("CAMBIAR PIN", color = HadesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recoveredPinMessage = null; viewModel.clearError() }) {
                    Text("ENTENDIDO", color = HadesCyan)
                }
            }
        )
    }

    if (showRecoverDialog) {
        RecoverPinDialog(
            onDismiss = { showRecoverDialog = false; viewModel.clearError() },
            onRecover = { phone, doc ->
                phoneForReset = phone // SE AGREGA: Capturamos el teléfono del diálogo para evitar errores
                viewModel.recuperarPin(phone, doc)
            }
        )
    }

    if (showResetDialog) {
        ResetPinDialog(
            onDismiss = { showResetDialog = false; viewModel.clearError() },
            onReset = { nuevoPin ->
                viewModel.resetearPinDespuesDeRecuperar(phoneForReset, nuevoPin)
                showResetDialog = false
            }
        )
    }
}

@Composable
fun ResetPinDialog(
    onDismiss: () -> Unit,
    onReset: (String) -> Unit
) {
    var nuevoPin by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HadesNavyDark,
        title = { Text("Nuevo PIN", color = HadesPurple, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HadesTextField(
                    value = nuevoPin,
                    onValueChange = { if (it.length <= 4 && it.all { it.isDigit() }) nuevoPin = it },
                    label = "Ingresa tu nuevo PIN",
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword
                )
                HadesTextField(
                    value = confirmacion,
                    onValueChange = { if (it.length <= 4 && it.all { it.isDigit() }) confirmacion = it },
                    label = "Confirma tu nuevo PIN",
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = nuevoPin.length == 4 && nuevoPin == confirmacion,
                onClick = { onReset(nuevoPin) }
            ) {
                Text("ACTUALIZAR", color = HadesCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}

@Composable
fun RecoverPinDialog(
    onDismiss: () -> Unit,
    onRecover: (String, String) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var doc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HadesNavyDark,
        title = { Text("Recuperar PIN", color = HadesPurple, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa tus datos para recuperar el PIN", color = HadesOnDark, fontSize = 14.sp)
                HadesTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    label = "Teléfono",
                    keyboardType = KeyboardType.Number
                )
                HadesTextField(
                    value = doc,
                    onValueChange = { if (it.all { c -> c.isDigit() }) doc = it },
                    label = "Número de Documento",
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onRecover(phone, doc) }) {
                Text("RECUPERAR", color = HadesCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
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
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent)
                        )
                    )
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
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "¿Olvidaste tu PIN?",
                        color = HadesCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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

@Preview(showBackground = true, showSystemUi = true, name = "Login — vacío")
@Composable
fun LoginViewPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "", pin = "", cargando = false, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — con datos")
@Composable
fun LoginViewFilledPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "3001234567", pin = "1234", cargando = false, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — cargando")
@Composable
fun LoginViewLoadingPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "3001234567", pin = "1234", cargando = true, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}
