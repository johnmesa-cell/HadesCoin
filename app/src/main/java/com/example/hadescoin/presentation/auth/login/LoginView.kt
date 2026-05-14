package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.HadesCoinTheme

// ─────────────────────────────────────────────────────────────────────────────
// VISTA REAL — recibe NavController y ViewModel reales
// ─────────────────────────────────────────────────────────────────────────────
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
        loginExitoso?.let { userId ->
            navController.navigate("home/$userId") {
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
        phoneNumber   = phoneNumber,
        pin           = pin,
        cargando      = cargando,
        onPhoneChange = { phoneNumber = it },
        onPinChange   = { pin = it },
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

// ─────────────────────────────────────────────────────────────────────────────
// CONTENIDO VISUAL — función pura sin ViewModel ni NavController
// Es la que usa el @Preview. No tiene ninguna dependencia externa.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginContent(
    phoneNumber: String,
    pin: String,
    cargando: Boolean,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // —— Logo y título ———————————————————————————————————
            Text(text = "💰", fontSize = 64.sp)
            Text(
                text = "HadesCoin",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tu billetera del futuro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // —— Card con fondo navy ————————————————————————————
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("Número de teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = onPinChange,
                        label = { Text("PIN de 4 dígitos") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onLoginClick,
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor   = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Ingresar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Regístrate",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEW — datos quemados, sin Firebase, sin NavController
// Solo se ejecuta en Android Studio, no afecta la app real
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "Login — vacío")
@Composable
fun LoginViewPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber     = "",
            pin             = "",
            cargando        = false,
            onPhoneChange   = {},
            onPinChange     = {},
            onLoginClick    = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — con datos")
@Composable
fun LoginViewFilledPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber     = "3001234567",
            pin             = "1234",
            cargando        = false,
            onPhoneChange   = {},
            onPinChange     = {},
            onLoginClick    = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — cargando")
@Composable
fun LoginViewLoadingPreview() {
    HadesCoinTheme {
        LoginContent(
            phoneNumber     = "3001234567",
            pin             = "1234",
            cargando        = true,
            onPhoneChange   = {},
            onPinChange     = {},
            onLoginClick    = {},
            onRegisterClick = {}
        )
    }
}
