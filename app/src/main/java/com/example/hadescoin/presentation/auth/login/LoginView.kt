package com.example.hadescoin.presentation.auth.login

// IMPORTACIONES: Solo las que el profesor usa.
// Eliminamos LiveData, FocusManager porque él no los explicó.
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
// Importamos los componentes de alerta (deben estar en tu carpeta components)
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog

@Composable
fun LoginView(
    // El profesor usa NavController directamente para la navegación
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    // --- ESTADOS DE LA INTERFAZ ---
    // Variables para capturar lo que el usuario escribe
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    // --- ESTADOS DE LAS ALERTAS (Lógica del profesor) ---
    // Booleanos para mostrar u ocultar los diálogos
    var showLoadingAlert by remember { mutableStateOf(false) }
    var showMessageAlert by remember { mutableStateOf(false) }

    // Variables para guardar los IDs de los textos (R.string) que se mostrarán
    var titleDialog by remember { mutableIntStateOf(0) }
    var messageDialog by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logotipo y Nombre (HadesCoin)
        Text(text = "💰", fontSize = 50.sp)
        Text(
            text = "HadesCoin",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Campo de entrada: Teléfono
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de entrada: PIN
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN de 4 dígitos") },
            modifier = Modifier.fillMaxWidth(),
            // PasswordVisualTransformation oculta los caracteres (los vuelve puntos)
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // BOTÓN DE INGRESAR
        Button(
            onClick = {
                // 1. Mostramos el diálogo de carga inmediatamente
                showLoadingAlert = true

                // 2. Ejecutamos la lógica del ViewModel
                // Al final de la función, el ViewModel nos "responde" mediante las llaves { }
                viewModel.login(phoneNumber, pin) { success, message ->

                    // 3. Cuando llega la respuesta, quitamos la carga
                    showLoadingAlert = false

                    if (success) {
                        // Si los datos son correctos, navegamos al Home
                        navController.navigate("home")
                    } else {
                        // Si falló, preparamos el diálogo de error con los textos de strings.xml
                        titleDialog = R.string.dialog_error_title
                        messageDialog = message // El mensaje de error viene del ViewModel
                        showMessageAlert = true // Mostramos la alerta
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navegación hacia la pantalla de Registro
        Row {
            Text(text = "¿No tienes cuenta? ")
            TextButton(onClick = { navController.navigate("register") }) {
                Text(text = "Regístrate", fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- COMPONENTES DE ALERTA ---
    // Estos if controlan si el diálogo aparece en pantalla o no
    if (showLoadingAlert) {
        ShowLoadingAlertDialog()
    }

    if (showMessageAlert) {
        ShowMessageAlertDialog(
            onConfirmation = { showMessageAlert = false }, // Se cierra al presionar el botón
            dialogTitle = titleDialog,
            dialogText = messageDialog
        )
    }
}