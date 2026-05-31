package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.presentation.components.*
import com.example.hadescoin.presentation.utils.BiometricHelper
import com.example.hadescoin.ui.theme.*

@Composable
fun LoginView(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val context  = LocalContext.current
    // MainActivity ahora extiende FragmentActivity — este cast siempre es exitoso
    val activity = context as? FragmentActivity

    val telefonoGuardado by viewModel.telefonoGuardado.observeAsState("")
    val nombreGuardado   by viewModel.nombreGuardado.observeAsState("")
    val haySession       by viewModel.haySessionGuardada.observeAsState(false)
    val biometriaActiva  by viewModel.biometriaActiva.observeAsState(false)

    var phoneNumber by remember { mutableStateOf("") }
    var pin         by remember { mutableStateOf("") }

    LaunchedEffect(telefonoGuardado) {
        if (telefonoGuardado.isNotBlank()) phoneNumber = telefonoGuardado
    }

    // Lanza el prompt biométrico automáticamente al abrir si hay sesión + biometría activa
    LaunchedEffect(haySession, biometriaActiva) {
        if (haySession && biometriaActiva && activity != null &&
            BiometricHelper.isDisponible(context)) {
            BiometricHelper.mostrar(
                activity  = activity,
                titulo    = "HadesCoin",
                subtitulo = "Usa tu huella para iniciar sesión",
                onExito   = { viewModel.loginConBiometria() },
                onError   = { }
            )
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
        haySession            = haySession,
        nombreGuardado        = nombreGuardado,
        biometriaActiva       = biometriaActiva && BiometricHelper.isDisponible(context),
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
        onForgotPasswordClick = { showRecoveryFlow = true },
        onOtroUsuario         = { viewModel.olvidarSesionGuardada() },
        onHuellaClick         = {
            if (activity != null) {
                BiometricHelper.mostrar(
                    activity  = activity,
                    titulo    = "HadesCoin",
                    subtitulo = "Usa tu huella para iniciar sesión",
                    onExito   = { viewModel.loginConBiometria() },
                    onError   = { }
                )
            }
        }
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
    haySession: Boolean = false,
    nombreGuardado: String = "",
    biometriaActiva: Boolean = false,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onOtroUsuario: () -> Unit = {},
    onHuellaClick: () -> Unit = {}
) {
    HadesBackground {
        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter            = painterResource(R.drawable.ic_hadescoin_logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier           = Modifier.size(110.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text          = stringResource(R.string.login_title),
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 6.sp,
                color         = HadesPurple,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.width(180.dp).height(2.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent))
            ))
            Spacer(Modifier.height(6.dp))
            Text(
                text          = stringResource(R.string.login_subtitle),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 2.sp,
                color         = HadesCyan.copy(alpha = 0.7f),
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.width(100.dp).height(1.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, HadesPurple.copy(alpha = 0.5f), Color.Transparent))
            ))
            Spacer(Modifier.height(32.dp))

            HadesCardBox {
                if (haySession && nombreGuardado.isNotBlank()) {
                    Text(
                        text          = "¡Bienvenido de nuevo,",
                        fontSize      = 11.sp,
                        letterSpacing = 1.sp,
                        color         = HadesOnDark.copy(alpha = 0.55f)
                    )
                    Text(
                        text       = nombreGuardado,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Black,
                        color      = HadesCyan
                    )
                    Spacer(Modifier.height(16.dp))

                    if (biometriaActiva) {
                        Box(
                            modifier         = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick  = onHuellaClick,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(HadesCyan.copy(alpha = 0.10f))
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Fingerprint,
                                    contentDescription = "Autenticar con huella",
                                    tint               = HadesCyan,
                                    modifier           = Modifier.size(40.dp)
                                )
                            }
                        }
                        Text(
                            text      = "Toca para usar tu huella",
                            fontSize  = 11.sp,
                            color     = HadesOnDark.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text      = "— o ingresa tu PIN —",
                            fontSize  = 10.sp,
                            color     = HadesOnDark.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    Text(
                        text          = stringResource(R.string.login_section_header),
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color         = HadesCyan
                    )
                }

                HadesTextField(
                    value         = phoneNumber,
                    onValueChange = onPhoneChange,
                    label         = stringResource(R.string.label_phone_number),
                    keyboardType  = KeyboardType.Number,
                    isError       = loginError != null && phoneNumber.isBlank(),
                    enabled       = !haySession
                )
                HadesTextField(
                    value         = pin,
                    onValueChange = onPinChange,
                    label         = stringResource(R.string.label_pin),
                    isPassword    = true,
                    keyboardType  = KeyboardType.NumberPassword,
                    isError       = loginError != null && pin.length < 4
                )
                Spacer(Modifier.height(4.dp))
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

                if (haySession) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick  = onOtroUsuario,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text       = "Iniciar sesión como otro usuario",
                            color      = HadesOnDark.copy(alpha = 0.4f),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!haySession) {
                Spacer(Modifier.height(20.dp))
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
}

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

@Preview(showBackground = true, showSystemUi = true, name = "Login — inteligente con huella")
@Composable
fun LoginViewBiometriaPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "3001234567", pin = "", cargando = false, loginError = null,
            haySession = true, nombreGuardado = "Juan Pérez", biometriaActiva = true,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {},
            onForgotPasswordClick = {}, onOtroUsuario = {}, onHuellaClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — inteligente sin huella")
@Composable
fun LoginViewSinHuellaPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "3001234567", pin = "", cargando = false, loginError = null,
            haySession = true, nombreGuardado = "Juan Pérez", biometriaActiva = false,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {},
            onForgotPasswordClick = {}, onOtroUsuario = {}
        )
    }
}
