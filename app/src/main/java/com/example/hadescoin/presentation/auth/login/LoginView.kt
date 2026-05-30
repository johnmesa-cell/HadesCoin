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
    val telefonoGuardado by viewModel.telefonoGuardado.observeAsState("")

    var phoneNumber by remember { mutableStateOf("") }
    var pin         by remember { mutableStateOf("") }

    // Al iniciar, si hay un teléfono guardado, lo precargamos en el campo
    LaunchedEffect(telefonoGuardado) {
        if (telefonoGuardado.isNotBlank()) {
            phoneNumber = telefonoGuardado
        }
    }

    val cargando          by viewModel.cargando.observeAsState(false)
    val loginExitoso      by viewModel.loginExitoso.observeAsState()
    val loginError        by viewModel.loginError.observeAsState()
    val codigoGenerado    by viewModel.codigoGenerado.observeAsState()
    val codigoValidado    by viewModel.codigoValidado.observeAsState(false)
    val errorRecuperacion by viewModel.errorRecuperacion.observeAsState()

    var mensajeError     by remember { mutableStateOf("") }
    var showError        by remember { mutableStateOf(false) }
    var showRecoveryFlow by remember { mutableStateOf(false) }

    LaunchedEffect(loginExitoso) {
        loginExitoso?.let { phone ->
            navController.navigate("home/$phone") { popUpTo("login") { inclusive = true } }
        }
    }
    LaunchedEffect(loginError)        { loginError?.let        { mensajeError = it; showError = true } }
    LaunchedEffect(errorRecuperacion) { errorRecuperacion?.let { mensajeError = it; showError = true } }

    LoginContent(
        phoneNumber           = phoneNumber,
        pin                   = pin,
        cargando              = cargando,
        loginError            = loginError,
        onPhoneChange         = { value ->
            if (value.length <= 10 && value.all { c -> c.isDigit() } && (value.isEmpty() || value[0] == '3')) {
                phoneNumber = value; viewModel.clearError()
            }
        },
        onPinChange           = { value ->
            if (value.length <= 4 && value.all { c -> c.isDigit() }) { pin = value; viewModel.clearError() }
        },
        onLoginClick          = { viewModel.login(phoneNumber, pin) },
        onRegisterClick       = { navController.navigate("register") },
        onForgotPasswordClick = { showRecoveryFlow = true }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false; viewModel.clearError() },
            dialogTitle    = stringResource(R.string.dialog_error_title),
            dialogText     = mensajeError
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
            onClearState   = { viewModel.clearError() }
        )
    }
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
            modifier            = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(R.drawable.ic_hadescoin_logo), contentDescription = stringResource(R.string.cd_logo), modifier = Modifier.size(110.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = stringResource(R.string.login_title), fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp, color = HadesPurple, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.width(180.dp).height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent))))
            Spacer(Modifier.height(6.dp))
            Text(text = stringResource(R.string.login_subtitle), fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = HadesCyan.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.width(100.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, HadesPurple.copy(alpha = 0.5f), Color.Transparent))))
            Spacer(Modifier.height(32.dp))
            HadesCardBox {
                Text(text = stringResource(R.string.login_section_header), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesCyan)
                HadesTextField(value = phoneNumber, onValueChange = onPhoneChange, label = stringResource(R.string.label_phone_number), keyboardType = KeyboardType.Number, isError = loginError != null && phoneNumber.isBlank())
                HadesTextField(value = pin, onValueChange = onPinChange, label = stringResource(R.string.label_pin), isPassword = true, keyboardType = KeyboardType.NumberPassword, isError = loginError != null && pin.length < 4)
                Spacer(Modifier.height(4.dp))
                HadesButton(text = stringResource(R.string.btn_login), textCargando = stringResource(R.string.btn_login_loading), onClick = onLoginClick, enabled = phoneNumber.length >= 5 && pin.length == 4, cargando = cargando)
                TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
                    Text("¿Olvidaste tu PIN?", color = HadesCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.text_no_account), fontSize = 13.sp, color = HadesOnDark.copy(alpha = 0.5f))
                TextButton(onClick = onRegisterClick, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text(text = stringResource(R.string.btn_register_link), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = HadesOrange)
                }
            }
        }
    }
}

// ─── Previews ───────────────────────────────────────────────────────────────────────
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
