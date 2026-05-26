package com.example.hadescoin.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun RegisterView(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    var fullName       by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var phoneNumber    by remember { mutableStateOf("") }
    var pin            by remember { mutableStateOf("") }
    var confirmPin     by remember { mutableStateOf("") }

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

    RegisterViewContent(
        fullName               = fullName,
        documentNumber         = documentNumber,
        phoneNumber            = phoneNumber,
        pin                    = pin,
        confirmPin             = confirmPin,
        cargando               = cargando,
        onFullNameChange       = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) { fullName = it; viewModel.clearError() } },
        onDocumentNumberChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) { documentNumber = it; viewModel.clearError() } },
        onPhoneChange          = { if (it.length <= 10 && it.all { char -> char.isDigit() } && (it.isEmpty() || it[0] == '3')) { phoneNumber = it; viewModel.clearError() } },
        onPinChange            = { if (it.length <= 4 && it.all { char -> char.isDigit() }) { pin = it; viewModel.clearError() } },
        onConfirmPinChange     = { if (it.length <= 4 && it.all { char -> char.isDigit() }) { confirmPin = it; viewModel.clearError() } },
        onRegisterClick        = { viewModel.register(fullName, documentNumber, phoneNumber, pin, confirmPin) },
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

@Composable
fun RegisterViewContent(
    fullName: String,
    documentNumber: String,
    phoneNumber: String,
    pin: String,
    confirmPin: String,
    cargando: Boolean,
    onFullNameChange: (String) -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    HadesBackground {
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

            HadesCardBox {

                Text(
                    text = "> NUEVO USUARIO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = HadesCyan
                )

                HadesTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = "Nombre completo"
                )

                HadesTextField(
                    value = documentNumber,
                    onValueChange = onDocumentNumberChange,
                    label = "Número de Documento",
                    keyboardType = KeyboardType.Number
                )

                HadesTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    label = "Número de Teléfono",
                    keyboardType = KeyboardType.Phone
                )

                HadesTextField(
                    value = pin,
                    onValueChange = onPinChange,
                    label = "PIN de 4 dígitos",
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword
                )

                HadesTextField(
                    value = confirmPin,
                    onValueChange = onConfirmPinChange,
                    label = "Confirmar PIN",
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword,
                    isError = confirmPin.isNotEmpty() && pin != confirmPin,
                    supportingText = {
                        if (confirmPin.isNotEmpty() && pin != confirmPin) {
                            Text(
                                text = "Los PINs no coinciden",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                HadesButton(
                    text = "[ CREAR CUENTA ]",
                    textCargando = "PROCESANDO...",
                    onClick = onRegisterClick,
                    enabled = pin.length == 4 && confirmPin.length == 4 && pin == confirmPin,
                    cargando = cargando
                )
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

@Preview(showBackground = true, showSystemUi = true, name = "Register — vacío")
@Composable
fun RegisterViewPreview() {
    HadesCoinTheme {
        RegisterViewContent(
            fullName = "", documentNumber = "", phoneNumber = "", pin = "", confirmPin = "", cargando = false,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onConfirmPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — con datos")
@Composable
fun RegisterViewFilledPreview() {
    HadesCoinTheme {
        RegisterViewContent(
            fullName = "Juan Pérez", documentNumber = "1010101010",
            phoneNumber = "3001234567", pin = "1234", confirmPin = "1234", cargando = false,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onConfirmPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — cargando")
@Composable
fun RegisterViewLoadingPreview() {
    HadesCoinTheme {
        RegisterViewContent(
            fullName = "Juan Pérez", documentNumber = "1010101010",
            phoneNumber = "3001234567", pin = "1234", confirmPin = "1234", cargando = true,
            onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {},
            onPinChange = {}, onConfirmPinChange = {}, onRegisterClick = {}, onBackToLoginClick = {}
        )
    }
}

