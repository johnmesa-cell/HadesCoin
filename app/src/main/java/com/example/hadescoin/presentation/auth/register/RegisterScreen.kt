package com.example.hadescoin.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.HadesCoinTheme

// ─────────────────────────────────────────────────────────────────────────────
// VISTA REAL — recibe NavController y ViewModel reales
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    var fullName       by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var phoneNumber    by remember { mutableStateOf("") }
    var pin            by remember { mutableStateOf("") }

    val cargando        by viewModel.cargando.observeAsState(false)
    val registroExitoso by viewModel.registroExitoso.observeAsState()
    val registroError   by viewModel.registroError.observeAsState()

    var mensajeError by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }

    LaunchedEffect(registroExitoso) {
        registroExitoso?.let { navController.popBackStack() }
    }

    LaunchedEffect(registroError) {
        registroError?.let {
            mensajeError = it
            showError = true
        }
    }

    RegisterContent(
        fullName       = fullName,
        documentNumber = documentNumber,
        phoneNumber    = phoneNumber,
        pin            = pin,
        cargando       = cargando,
        onFullNameChange       = { fullName = it },
        onDocumentNumberChange = { documentNumber = it },
        onPhoneChange          = { phoneNumber = it },
        onPinChange            = { pin = it },
        onRegisterClick        = { viewModel.register(fullName, documentNumber, phoneNumber, pin) },
        onBackToLoginClick     = { navController.popBackStack() }
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
fun RegisterContent(
    fullName: String,
    documentNumber: String,
    phoneNumber: String,
    pin: String,
    cargando: Boolean,
    onFullNameChange: (String) -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // —— Logo y título ———————————————————————————————————
            Text(text = "📝", fontSize = 64.sp)
            Text(
                text = "HadesCoin",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Crea tu billetera virtual",
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
                        value = fullName,
                        onValueChange = onFullNameChange,
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = onDocumentNumberChange,
                        label = { Text("Número de Documento") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("Número de Teléfono") },
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
                        onClick = onRegisterClick,
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor   = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Registrarme",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                TextButton(onClick = onBackToLoginClick) {
                    Text(
                        text = "Inicia sesión",
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
@Preview(showBackground = true, showSystemUi = true, name = "Register — vacío")
@Composable
fun RegisterScreenPreview() {
    HadesCoinTheme {
        RegisterContent(
            fullName       = "",
            documentNumber = "",
            phoneNumber    = "",
            pin            = "",
            cargando       = false,
            onFullNameChange       = {},
            onDocumentNumberChange = {},
            onPhoneChange          = {},
            onPinChange            = {},
            onRegisterClick        = {},
            onBackToLoginClick     = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — con datos")
@Composable
fun RegisterScreenFilledPreview() {
    HadesCoinTheme {
        RegisterContent(
            fullName       = "Juan Pérez",
            documentNumber = "1010101010",
            phoneNumber    = "3001234567",
            pin            = "1234",
            cargando       = false,
            onFullNameChange       = {},
            onDocumentNumberChange = {},
            onPhoneChange          = {},
            onPinChange            = {},
            onRegisterClick        = {},
            onBackToLoginClick     = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — cargando")
@Composable
fun RegisterScreenLoadingPreview() {
    HadesCoinTheme {
        RegisterContent(
            fullName       = "Juan Pérez",
            documentNumber = "1010101010",
            phoneNumber    = "3001234567",
            pin            = "1234",
            cargando       = true,
            onFullNameChange       = {},
            onDocumentNumberChange = {},
            onPhoneChange          = {},
            onPinChange            = {},
            onRegisterClick        = {},
            onBackToLoginClick     = {}
        )
    }
}
