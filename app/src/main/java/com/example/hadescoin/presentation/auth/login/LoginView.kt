package com.example.hadescoin.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog

// 1. Colores extraídos para evitar su recreación en cada recomposición
private val ColorFondo = Color(0xFF0A0B10)
private val ColorTarjeta = Color(0xFF121626)
private val ColorMorado = Color(0xFF9D4EDD)
private val ColorCian = Color(0xFF00F0FF)
private val ColorNaranja = Color(0xFFFF5400)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    var docNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // 2. Corrección: Uso de mutableIntStateOf para optimizar el manejo del entero
    var errorResId by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
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
                contentDescription = "Logo Hadescoin",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HADESCOIN",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = ColorMorado
            )

            Text(
                text = "// TU BILLETERA DEL FUTURO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ColorCian.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorTarjeta)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "> INICIAR SESIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorCian
                )

                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text("Número de documento") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN de 4 dígitos") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!cargando) ColorNaranja else Color.Gray)
                        .clickable(enabled = !cargando) {
                            cargando = true
                            viewModel.login(docNumber, pin) { success, resId ->
                                cargando = false
                                if (success) {
                                    navController.navigate("home/$docNumber") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorResId = resId
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cargando) "VERIFICANDO..." else "[ INGRESAR ]",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = "¿Sin cuenta? ",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        text = "REGISTRARSE ›",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorNaranja
                    )
                }
            }
        }
    }

    if (cargando) {
        ShowLoadingAlertDialog()
    }

    if (errorResId > 0) {
        // 3. Corrección: Conversión de Int (Resource ID) a String usando stringResource()
        ShowMessageAlertDialog(
            onConfirmation = { errorResId = 0 },
            dialogTitle = stringResource(id = R.string.dialog_error_title),
            dialogText = stringResource(id = errorResId)
        )
    }
}