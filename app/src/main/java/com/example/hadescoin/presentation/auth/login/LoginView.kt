package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
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
        onPhoneChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) { phoneNumber = it; viewModel.clearError() } },
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
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(HadesBlack, HadesNavyDark, HadesBlack)
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(HadesOrange, HadesPurpleGlow)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(HadesPurple, HadesCyan.copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HadesNavyDark, HadesNavy)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text(
                        text = "> INICIAR SESIÓN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = HadesCyan
                    )

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = HadesCyan,
                        unfocusedBorderColor = HadesPurple.copy(alpha = 0.5f),
                        focusedLabelColor    = HadesCyan,
                        unfocusedLabelColor  = HadesOnDark.copy(alpha = 0.5f),
                        cursorColor          = HadesCyan,
                        focusedTextColor     = HadesOnDark,
                        unfocusedTextColor   = HadesOnDark
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("Número de teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = fieldColors,
                        isError = loginError != null && phoneNumber.isBlank()
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = onPinChange,
                        label = { Text("PIN de 4 dígitos") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = fieldColors,
                        isError = loginError != null && pin.length < 4
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!cargando) buttonGradient
                                else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                            )
                    ) {
                        Button(
                            onClick = onLoginClick,
                            enabled = !cargando && phoneNumber.length >= 5 && pin.length == 4,
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = if (cargando) "VERIFICANDO..." else "[ INGRESAR ]",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = Color.White
                            )
                        }
                    }
                }
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
