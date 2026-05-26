package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.presentation.components.HadesBackground
import com.example.hadescoin.presentation.components.HadesButton
import com.example.hadescoin.presentation.components.HadesCardBox
import com.example.hadescoin.presentation.components.HadesTextField
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────
// VISTA REAL
// ─────────────────────────────────────────────────────────────────────────
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

    var mensajeError by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }

    LaunchedEffect(loginExitoso) {
        loginExitoso?.let { phoneNumber ->
            navController.navigate("home/$phoneNumber") {
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

    LoginContent(
        phoneNumber = phoneNumber,
        pin           = pin,
        cargando      = cargando,
        loginError    = loginError,
        onPhoneChange = { if (it.length <= 10 && it.all { char -> char.isDigit() } && (it.isEmpty() || it[0] == '3')) { phoneNumber = it; viewModel.clearError() } },
        onPinChange   = { if (it.length <= 4 && it.all { char -> char.isDigit() }) { pin = it; viewModel.clearError() } },
        onLoginClick  = { viewModel.login(phoneNumber, pin) },
        onRegisterClick = { navController.navigate("register") }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false },
            dialogTitle    = "Error",
            dialogText     = mensajeError
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// CONTENIDO VISUAL PURO (apto para @Preview)
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun LoginContent(
    phoneNumber: String,
    pin: String,
    cargando: Boolean,
    loginError: String?,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_hadescoin_logo),
                contentDescription = "HadesCoin Logo",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HADESCOIN",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = HadesPurple,
                textAlign = TextAlign.Center
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
                text = "// TU BILLETERA DEL FUTURO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = HadesCyan.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            HadesCardBox {

                Text(
                    text = "> INICIAR SESIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = HadesCyan
                )

                HadesTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    label = "Número de teléfono",
                    keyboardType = KeyboardType.Number,
                    isError = loginError != null && phoneNumber.isBlank()
                )

                HadesTextField(
                    value = pin,
                    onValueChange = onPinChange,
                    label = "PIN de 4 dígitos",
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword,
                    isError = loginError != null && pin.length < 4
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesButton(
                    text = "[ INGRESAR ]",
                    textCargando = "VERIFICANDO...",
                    onClick = onLoginClick,
                    enabled = phoneNumber.length >= 5 && pin.length == 4,
                    cargando = cargando
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿Sin cuenta? ",
                    fontSize = 13.sp,
                    color = HadesOnDark.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick = onRegisterClick,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "REGISTRARSE ›",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = HadesOrange
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "Login — vacío")
@Composable
fun LoginViewPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "", pin = "", cargando = false, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — con datos")
@Composable
fun LoginViewFilledPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "3001234567", pin = "1234", cargando = false, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — cargando")
@Composable
fun LoginViewLoadingPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber = "3001234567", pin = "1234", cargando = true, loginError = null,
            onPhoneChange = {}, onPinChange = {}, onLoginClick = {}, onRegisterClick = {}
        )
    }
}
