package com.example.hadescoin.presentation.auth.login

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "💰", fontSize = 50.sp)
        Text(
            text = "HadesCoin",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
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

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.login(phoneNumber, pin) },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(text = "¿No tienes cuenta? ")
            TextButton(onClick = { navController.navigate("register") }) {
                Text(text = "Regístrate", fontWeight = FontWeight.Bold)
            }
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
