package com.example.hadescoin.presentation.auth.register

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
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

private enum class RegisterStep {
    FORMULARIO, CAMARA_FRONTAL, CAMARA_TRASERA, CONFIRMACION
}

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

    var mensajeError      by remember { mutableStateOf("") }
    var showError         by remember { mutableStateOf(false) }
    var currentStep       by remember { mutableStateOf(RegisterStep.FORMULARIO) }
    var showPermRationale by remember { mutableStateOf(false) }

    val goToLogin: () -> Unit = { navController.popBackStack(); Unit }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) currentStep = RegisterStep.CAMARA_FRONTAL
        else showPermRationale = true
    }

    LaunchedEffect(registroExitoso) {
        if (registroExitoso == true) navController.popBackStack()
    }
    LaunchedEffect(registroError) {
        registroError?.let { err -> mensajeError = err; showError = true }
    }

    when (currentStep) {
        RegisterStep.CAMARA_FRONTAL -> {
            CameraCaptureView(side = CedulaSide.FRONTAL, onCaptured = { currentStep = RegisterStep.CAMARA_TRASERA }, onBack = { currentStep = RegisterStep.FORMULARIO }, onBackToLogin = goToLogin)
            return
        }
        RegisterStep.CAMARA_TRASERA -> {
            CameraCaptureView(side = CedulaSide.TRASERA, onCaptured = { viewModel.onDocumentCaptured(); currentStep = RegisterStep.CONFIRMACION }, onBack = { currentStep = RegisterStep.CAMARA_FRONTAL }, onBackToLogin = goToLogin)
            return
        }
        RegisterStep.CONFIRMACION -> {
            RegisterConfirmacionView(fullName = fullName, documentNumber = documentNumber, phoneNumber = phoneNumber, cargando = cargando, onCreateAccount = { viewModel.register(fullName, documentNumber, phoneNumber, pin, confirmPin) }, onBack = { currentStep = RegisterStep.CAMARA_TRASERA }, onBackToLogin = goToLogin)
            return
        }
        RegisterStep.FORMULARIO -> { /* continua abajo */ }
    }

    RegisterViewContent(
        fullName               = fullName,
        documentNumber         = documentNumber,
        phoneNumber            = phoneNumber,
        pin                    = pin,
        confirmPin             = confirmPin,
        onFullNameChange       = { value -> if (value.all { c -> c.isLetter() || c.isWhitespace() }) { fullName = value; viewModel.clearError() } },
        onDocumentNumberChange = { value -> if (value.length <= 10 && value.all { c -> c.isDigit() }) { documentNumber = value; viewModel.clearError() } },
        onPhoneChange          = { value -> if (value.length <= 10 && value.all { c -> c.isDigit() } && (value.isEmpty() || value[0] == '3')) { phoneNumber = value; viewModel.clearError() } },
        onPinChange            = { value -> if (value.length <= 4 && value.all { c -> c.isDigit() }) { pin = value; viewModel.clearError() } },
        onConfirmPinChange     = { value -> if (value.length <= 4 && value.all { c -> c.isDigit() }) { confirmPin = value; viewModel.clearError() } },
        onContinueClick        = { cameraPermLauncher.launch(Manifest.permission.CAMERA) },
        onBackToLoginClick     = goToLogin
    )

    if (cargando) ShowLoadingAlertDialog()
    if (showError) ShowMessageAlertDialog(onConfirmation = { showError = false }, dialogTitle = stringResource(R.string.dialog_error_title), dialogText = mensajeError)
    if (showPermRationale) ShowMessageAlertDialog(onConfirmation = { showPermRationale = false }, dialogTitle = "Permiso requerido", dialogText = "La cámara es necesaria para verificar tu identidad. Actívala desde los ajustes del dispositivo.")
}

@Composable
fun RegisterViewContent(
    fullName: String, documentNumber: String, phoneNumber: String,
    pin: String, confirmPin: String,
    onFullNameChange: (String) -> Unit, onDocumentNumberChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit, onPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit, onContinueClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    val formValid = fullName.isNotBlank() && documentNumber.length in 5..10 &&
        phoneNumber.length == 10 && pin.length == 4 && confirmPin.length == 4 && pin == confirmPin

    HadesScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.ic_hadescoin_logo), contentDescription = stringResource(R.string.cd_logo), modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = stringResource(R.string.login_title), fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp, color = HadesPurple, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.width(180.dp).height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, HadesCyan, HadesOrange, Color.Transparent))))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = stringResource(R.string.register_subtitle), fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = HadesCyan.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            StepIndicator(currentStep = 1, totalSteps = 4)
            Spacer(modifier = Modifier.height(20.dp))

            HadesCardBox {
                Text(text = stringResource(R.string.register_section_header), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesCyan)
                HadesTextField(value = fullName, onValueChange = onFullNameChange, label = stringResource(R.string.label_full_name))
                HadesTextField(value = documentNumber, onValueChange = onDocumentNumberChange, label = stringResource(R.string.label_document_number), keyboardType = KeyboardType.Number)
                HadesTextField(value = phoneNumber, onValueChange = onPhoneChange, label = stringResource(R.string.label_phone_number_register), keyboardType = KeyboardType.Phone)
                HadesTextField(value = pin, onValueChange = onPinChange, label = stringResource(R.string.label_pin), isPassword = true, keyboardType = KeyboardType.NumberPassword)
                HadesTextField(
                    value = confirmPin, onValueChange = onConfirmPinChange, label = stringResource(R.string.label_confirm_pin),
                    isPassword = true, keyboardType = KeyboardType.NumberPassword,
                    isError = confirmPin.isNotEmpty() && pin != confirmPin,
                    supportingText = { if (confirmPin.isNotEmpty() && pin != confirmPin) Text(text = stringResource(R.string.error_pins_no_match), color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onContinueClick, enabled = formValid,
                    colors = ButtonDefaults.buttonColors(containerColor = HadesOrange, contentColor = HadesBlack),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "VERIFICAR CÉDULA", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            LoginRow(onBackToLoginClick)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegisterConfirmacionView(
    fullName: String, documentNumber: String, phoneNumber: String,
    cargando: Boolean, onCreateAccount: () -> Unit, onBack: () -> Unit, onBackToLogin: () -> Unit
) {
    HadesScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(currentStep = 4, totalSteps = 4)
            Spacer(modifier = Modifier.height(24.dp))
            Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = HadesCyan, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "¡Verificación completa!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = HadesCyan, textAlign = TextAlign.Center)
            Text(text = "Ambos lados de tu cédula fueron capturados correctamente.", fontSize = 13.sp, color = HadesOnDark.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp, bottom = 24.dp))

            HadesCardBox {
                Text(text = "RESUMEN DE REGISTRO", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = HadesCyan)
                Spacer(modifier = Modifier.height(8.dp))
                ResumenFila(label = "Nombre",    valor = fullName)
                ResumenFila(label = "Documento", valor = documentNumber)
                ResumenFila(label = "Teléfono",  valor = phoneNumber)
                ResumenFila(label = "Cédula",    valor = "✅ Verificada (frontal + trasera)")
                Spacer(modifier = Modifier.height(16.dp))
                HadesButton(text = "CREAR CUENTA", textCargando = "Creando cuenta...", onClick = onCreateAccount, enabled = !cargando, cargando = cargando)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack) { Text(text = "← Volver a tomar fotos", color = HadesOrange, fontSize = 13.sp) }
            LoginRow(onBackToLogin)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LoginRow(onBackToLoginClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "¿Ya tienes cuenta? ", fontSize = 13.sp, color = HadesOnDark.copy(alpha = 0.5f))
        TextButton(onClick = onBackToLoginClick, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text(text = "Iniciar sesión", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = HadesOrange)
        }
    }
}

@Composable
private fun ResumenFila(label: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 12.sp, color = HadesOnDark.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
        Text(text = valor, fontSize = 12.sp, color = HadesOnDark, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — Formulario")
@Composable
fun RegisterViewPreview() {
    HadesCoinTheme { RegisterViewContent(fullName = "", documentNumber = "", phoneNumber = "", pin = "", confirmPin = "", onFullNameChange = {}, onDocumentNumberChange = {}, onPhoneChange = {}, onPinChange = {}, onConfirmPinChange = {}, onContinueClick = {}, onBackToLoginClick = {}) }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register — Confirmación")
@Composable
fun RegisterConfirmPreview() {
    HadesCoinTheme { RegisterConfirmacionView(fullName = "Juan Pérez", documentNumber = "1010101010", phoneNumber = "3001234567", cargando = false, onCreateAccount = {}, onBack = {}, onBackToLogin = {}) }
}
