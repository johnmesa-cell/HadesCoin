package com.example.hadescoin.presentation.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog

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
        registroExitoso?.let {
            navController.popBackStack()
        }
    }

    LaunchedEffect(registroError) {
        registroError?.let {
            mensajeError = it
            showError = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📝", fontSize = 50.sp)
        Text(
            text = "HadesCoin",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Crea tu billetera virtual", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = documentNumber,
            onValueChange = { documentNumber = it },
            label = { Text("Número de Documento") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN de 4 dígitos") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.register(fullName, documentNumber, phoneNumber, pin) },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Registrarme")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }

    if (cargando) {
        ShowLoadingAlertDialog()
    }

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = { showError = false },
            dialogTitle = "Error",
            dialogText = mensajeError
        )
    }
}
