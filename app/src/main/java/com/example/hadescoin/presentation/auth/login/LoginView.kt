package com.example.hadescoin.presentation.auth.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val haySession    by viewModel.haySessionGuardada.observeAsState(false)
    val telefonoLocal by viewModel.telefonoGuardado.observeAsState("")
    val nombreLocal   by viewModel.nombreGuardado.observeAsState("")

    var phoneNumber by remember { mutableStateOf("") }
    var pin         by remember { mutableStateOf("") }

    val cargando          by viewModel.cargando.observeAsState(false)
    val loginExitoso      by viewModel.loginExitoso.observeAsState()
    val loginError        by viewModel.loginError.observeAsState()
    val codigoGenerado    by viewModel.codigoGenerado.observeAsState()
    val codigoValidado    by viewModel.codigoValidado.observeAsState(false)
    val errorRecuperacion by viewModel.errorRecuperacion.observeAsState()

    var mensajeError     by remember { mutableStateOf("") }
    var showError        by remember { mutableStateOf(false) }
    var showRecoveryFlow by remember { mutableStateOf(false) }

    LaunchedEffect(haySession, telefonoLocal) {
        if (haySession && telefonoLocal.isNotBlank()) phoneNumber = telefonoLocal
    }
    LaunchedEffect(loginExitoso) {
        loginExitoso?.let { phone ->
            navController.navigate("home/$phone") { popUpTo("login") { inclusive = true } }
        }
    }
    LaunchedEffect(loginError)        { loginError?.let        { mensajeError = it; showError = true } }
    LaunchedEffect(errorRecuperacion) { errorRecuperacion?.let { mensajeError = it; showError = true } }

    AnimatedContent(
        targetState    = haySession && telefonoLocal.isNotBlank(),
        transitionSpec = {
            (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 })
                .togetherWith(fadeOut(tween(200)))
        },
        label = "login_mode_transition"
    ) { mostrarInteligente ->
        if (mostrarInteligente) {
            LoginInteligenteContent(
                nombre             = nombreLocal,
                telefonoPrecargado = telefonoLocal,
                pin                = pin,
                cargando           = cargando,
                onPinChange        = { pin = it; viewModel.clearError() },
                onLoginClick       = { viewModel.login(telefonoLocal, pin) },
                onOtroUsuario      = { pin = ""; viewModel.olvidarSesionGuardada() },
                onForgotPin        = { showRecoveryFlow = true }
            )
        } else {
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
        }
    }

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

// ─── Pantalla inteligente ─────────────────────────────────────────────────────────────────
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
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    HadesBackground {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -30 }) {
                Image(painter = painterResource(R.drawable.ic_hadescoin_logo), contentDescription = stringResource(R.string.cd_logo), modifier = Modifier.size(90.dp))
            }
            Spacer(Modifier.height(20.dp))
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(400, 100)) + scaleIn(tween(400, 100))) {
                GradientAvatarBorder {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = HadesPurple, modifier = Modifier.size(38.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(400, 200)) + slideInVertically(tween(400, 200)) { 20 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "¡Bienvenido de nuevo!", fontSize = 13.sp, color = HadesCyan.copy(alpha = 0.7f), letterSpacing = 1.sp)
                    Text(text = nombre.ifBlank { "Usuario" }, fontSize = 22.sp, fontWeight = FontWeight.Black, color = HadesPurple, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(400, 280))) {
                Row(
                    modifier = Modifier
                        .background(brush = Brush.horizontalGradient(listOf(HadesNavyDark, HadesPurple.copy(alpha = 0.15f), HadesNavyDark)), shape = RoundedCornerShape(50))
                        .border(width = 1.dp, brush = Brush.horizontalGradient(listOf(HadesPurple.copy(alpha = 0.5f), HadesCyan.copy(alpha = 0.3f))), shape = RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Phone, contentDescription = null, tint = HadesCyan.copy(alpha = 0.6f), modifier = Modifier.size(15.dp))
                    Text(text = telefonoPrecargado, fontSize = 15.sp, color = HadesOnDark.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(28.dp))
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, 350)) + slideInVertically(tween(500, 350)) { 40 }) {
                HadesCardBox {
                    Text(text = "INGRESA TU PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesCyan)
                    Spacer(Modifier.height(20.dp))
                    HadesPinInput(
                        value         = pin,
                        onValueChange = onPinChange,
                        modifier      = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    HadesButton(text = stringResource(R.string.btn_login), textCargando = stringResource(R.string.btn_login_loading), onClick = onLoginClick, enabled = pin.length == 4, cargando = cargando)
                    TextButton(onClick = onForgotPin, modifier = Modifier.align(Alignment.End)) {
                        Text("¿Olvidaste tu PIN?", color = HadesCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(400, 450))) {
                OutlinedButton(
                    onClick  = onOtroUsuario,
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = HadesOnDark.copy(alpha = 0.6f)),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Brush.horizontalGradient(listOf(HadesPurple.copy(alpha = 0.3f), HadesCyan.copy(alpha = 0.3f)))),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar sesión como otro usuario", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun GradientAvatarBorder(content: @Composable BoxScope.() -> Unit) {
    val gradient = listOf(HadesPurple, HadesCyan, HadesOrange, HadesPurple)
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(76.dp)) {
            drawCircle(brush = Brush.sweepGradient(gradient), radius = size.minDimension / 2f, style = Stroke(width = 3.dp.toPx()))
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(HadesPurple.copy(alpha = 0.12f)), contentAlignment = Alignment.Center, content = content)
    }
}

// ─── Pantalla normal ───────────────────────────────────────────────────────────────────
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

@Preview(showBackground = true, showSystemUi = true, name = "Login — inteligente")
@Composable
fun LoginInteligentePreview() {
    HadesCoinTheme {
        LoginInteligenteContent(
            nombre = "Juan Pérez", telefonoPrecargado = "3001234567", pin = "12", cargando = false,
            onPinChange = {}, onLoginClick = {}, onOtroUsuario = {}, onForgotPin = {}
        )
    }
}
