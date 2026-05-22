package com.example.hadescoin.presentation.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.hadescoin.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

// 1. Degradados extraídos para evitar su recreación en cada recomposición
private val BackgroundGradient = Brush.verticalGradient(listOf(HadesBlack, HadesNavyDark, HadesBlack))
private val CardBorderGradient = Brush.verticalGradient(listOf(HadesPurple, HadesCyan.copy(alpha = 0.5f)))
private val CardBackgroundGradient = Brush.verticalGradient(listOf(HadesNavyDark, HadesNavy))
private val ButtonActiveGradient = Brush.horizontalGradient(listOf(HadesOrange, HadesPurpleGlow))
private val ButtonDisabledGradient = Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    var fullName by remember { mutableStateOf("") }
    var docNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    // 2. Optimización: mutableIntStateOf para identificadores numéricos
    var dialogMsgId by remember { mutableIntStateOf(0) }
    var esExitoso by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HadesCyan,
        unfocusedBorderColor = HadesPurple.copy(alpha = 0.5f),
        focusedLabelColor = HadesCyan,
        unfocusedLabelColor = HadesOnDark.copy(alpha = 0.5f),
        focusedTextColor = HadesOnDark,
        unfocusedTextColor = HadesOnDark,
        cursorColor = HadesCyan
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
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
                contentDescription = "Logo Hadescoin", // 3. Accesibilidad
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "NUEVA CUENTA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = HadesPurple,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorderGradient, RoundedCornerShape(20.dp))
                    .background(CardBackgroundGradient)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "> CREAR PERFIL DE ACCESO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HadesCyan
                )

                // 4. ImeAction integrados para saltar de un campo a otro cómodamente
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text("Número de documento") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número de teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    singleLine = true,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN (4 dígitos)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    singleLine = true,
                    colors = fieldColors
                )

                Button(
                    onClick = {
                        cargando = true
                        viewModel.register(docNumber, phone, fullName, pin) { success, resId ->
                            cargando = false
                            esExitoso = success
                            dialogMsgId = resId
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!cargando) ButtonActiveGradient else ButtonDisabledGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
                ) {
                    Text(
                        text = if (cargando) "REGISTRANDO..." else "[ CONFIRMAR ALTA ]",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "‹ VOLVER AL INICIO DE SESIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HadesOrange
                )
            }
        }
    }

    if (cargando) ShowLoadingAlertDialog()

    if (dialogMsgId > 0) {
        // 5. Corrección de Int a String para ambos diálogos
        ShowMessageAlertDialog(
            onConfirmation = {
                dialogMsgId = 0
                if (esExitoso) navController.popBackStack()
            },
            dialogTitle = stringResource(id = if (esExitoso) R.string.dialog_success_title else R.string.dialog_error_title),
            dialogText = stringResource(id = dialogMsgId)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRegisterScreen() {
    HadesCoinTheme {
        RegisterScreen(navController = rememberNavController())
    }
}
