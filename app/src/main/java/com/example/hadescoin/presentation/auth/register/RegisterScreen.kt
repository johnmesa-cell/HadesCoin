package com.example.hadescoin.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
// VISTA REAL (LÓGICA)
// ─────────────────────────────────────────────────────────────────────────
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
        fullName               = fullName,
        documentNumber         = documentNumber,
        phoneNumber            = phoneNumber,
        pin                    = pin,
        cargando               = cargando,
        registroError          = registroError, // Pasamos el error a la vista
        onFullNameChange       = {
            fullName = it
            viewModel.clearError() // Limpiamos error al escribir
        },
        onDocumentNumberChange = {
            documentNumber = it
            viewModel.clearError() // Limpiamos error al escribir
        },
        onPhoneChange          = {
            phoneNumber = it
            viewModel.clearError() // Limpiamos error al escribir
        },
        onPinChange            = {
            pin = it
            viewModel.clearError() // Limpiamos error al escribir
        },
        // ¡Aquí ya estabas enviando los 4 argumentos correctamente!
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

// ─────────────────────────────────────────────────────────────────────────
// CONTENIDO VISUAL PURO (DISEÑO)
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun RegisterContent(
    fullName: String,
    documentNumber: String,
    phoneNumber: String,
    pin: String,
    cargando: Boolean,
    registroError: String?, // Recibe el estado del error para pintar de rojo los campos
    onFullNameChange: (String) -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
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
                text = "// CREA TU CUENTA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = HadesCyan.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                        text = "> NUEVO USUARIO",
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
                        value = fullName,
                        onValueChange = onFullNameChange,
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors,
                        isError = registroError != null && fullName.isBlank()
                    )

                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = onDocumentNumberChange,
                        label = { Text("Número de Documento") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = fieldColors,
                        isError = registroError != null && documentNumber.isBlank()
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("Número de Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = fieldColors,
                        isError = registroError != null && phoneNumber.isBlank()
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
                        isError = registroError != null && pin.isBlank()
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
                            onClick = onRegisterClick,
                            enabled = !cargando,
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = if (cargando) "PROCESANDO..." else "[ CREAR CUENTA ]",
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
                    text = "¿Ya tienes cuenta? ",
                    fontSize = 13.sp,
                    color = HadesOnDark.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick = onBackToLoginClick,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "INICIAR SESIÓN ›",
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
@Preview(showBackground = true, showSystemUi = true, name = "Register — vacío")
@Composable
fun RegisterScreenPreview() {
    HadesCoinTheme {
        RegisterContent(
            fullName = "", documentNumber = "", phoneNumber = "", pin = "", cargando = false, registroError = null,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — con datos")
@Composable
fun RegisterScreenFilledPreview() {
    HadesCoinTheme {
        RegisterContent(
            fullName = "Juan Pérez", documentNumber = "1010101010",
            phoneNumber = "3001234567", pin = "1234", cargando = false, registroError = null,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — cargando")
@Composable
fun RegisterScreenLoadingPreview() {
    HadesCoinTheme {
        RegisterContent(
            fullName = "Juan Pérez", documentNumber = "1010101010",
            phoneNumber = "3001234567", pin = "1234", cargando = true, registroError = null,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}
