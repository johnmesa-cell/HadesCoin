package com.example.hadescoin.presentation.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.hadescoin.R // Importante: usa el R de tu proyecto

/**
 * Diálogo de Carga: Muestra un círculo de progreso.
 * El profesor lo usa para bloquear la pantalla mientras Firebase responde.
 */
@Composable
fun ShowLoadingAlertDialog() {
    AlertDialog(
        onDismissRequest = { }, // No se cierra al tocar fuera (bloqueante)
        title = {
            // Usa un string de recursos para decir "Cargando..."
            Text(stringResource(id = R.string.text_loading))
        },
        text = {
            // Contenedor para centrar el círculo de carga
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // El círculo giratorio
            }
        },
        confirmButton = { } // No tiene botones porque se cierra automáticamente por código
    )
}

/**
 * Diálogo de Mensaje: Se usa para mostrar errores o éxitos.
 * Recibe una función (onConfirmation) para saber qué hacer cuando el usuario da OK.
 */
@Composable
fun ShowMessageAlertDialog(
    onConfirmation: () -> Unit, // Acción que ocurre al pulsar el botón
    dialogTitle: Int,           // ID del recurso string para el título
    dialogText: Int             // ID del recurso string para el mensaje
) {
    AlertDialog(
        // Cargamos los textos usando stringResource y los IDs recibidos
        title = { Text(text = stringResource(id = dialogTitle)) },
        text = { Text(text = stringResource(id = dialogText)) },
        onDismissRequest = { }, // Obliga al usuario a presionar el botón
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation() // Ejecuta la lógica (ej: cerrar el diálogo)
                }
            ) {
                // Texto del botón, usualmente "Aceptar"
                Text(stringResource(id = R.string.btn_accept))
            }
        }
    )
}