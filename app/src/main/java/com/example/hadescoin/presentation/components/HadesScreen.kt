package com.example.hadescoin.presentation.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Contenedor base para TODAS las pantallas de HadesCoin.
 *
 * Combina el fondo con gradiente (HadesBackground) con el respeto automático
 * de las barras del sistema (status bar arriba, navigation bar abajo).
 *
 * Uso:
 *   HadesScreen {
 *       Column(...) { ... }
 *   }
 *
 * Beneficio: ninguna pantalla se solapa con la barra de estado ni con
 * la barra de navegación de botones, sin importar el tamaño o modelo
 * del dispositivo.
 */
@Composable
fun HadesScreen(content: @Composable BoxScope.() -> Unit) {
    HadesBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            content = content
        )
    }
}
