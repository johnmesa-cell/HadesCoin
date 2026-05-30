package com.example.hadescoin.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hadescoin.ui.theme.HadesCyan
import com.example.hadescoin.ui.theme.HadesNavyDark
import com.example.hadescoin.ui.theme.HadesOrange
import com.example.hadescoin.ui.theme.HadesPurple

/**
 * Indicador visual de PIN con 4 circulos al estilo apps bancarias.
 * Toca cualquier parte del componente para enfocar y desplegar el teclado.
 */
@Composable
fun HadesPinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dotSize: Dp = 18.dp,
    spacing: Dp = 20.dp
) {
    val focusRequester    = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    // Area de toque real que envuelve todo — clickable con area generosa
    Box(
        modifier         = modifier
            .clickable(
                interactionSource = interactionSource,
                indication        = null   // sin ripple visible
            ) {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
        contentAlignment = Alignment.Center
    ) {
        // Campo invisible con tamano real (0dp visual pero enfocable) —
        // ocupa todo el Box para que el click area coincida
        BasicTextField(
            value                = value,
            onValueChange        = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onValueChange(it) },
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine           = true,
            cursorBrush          = SolidColor(Color.Transparent),
            modifier             = Modifier
                .matchParentSize()             // mismo tamano que el Box
                .focusRequester(focusRequester),
            decorationBox        = { /* sin decoracion visual — solo los circulos */ }
        )

        // 4 circulos visuales (encima del campo invisible)
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val filled = index < value.length

                val dotColor by animateColorAsState(
                    targetValue   = if (filled) HadesPurple else Color.Transparent,
                    animationSpec = tween(150),
                    label         = "dot_fill_$index"
                )
                val borderColor by animateColorAsState(
                    targetValue = when {
                        filled && index == value.length - 1 -> HadesCyan
                        filled                              -> HadesPurple
                        else                                -> HadesOrange.copy(alpha = 0.4f)
                    },
                    animationSpec = tween(150),
                    label         = "dot_border_$index"
                )

                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(
                            brush = if (filled)
                                Brush.radialGradient(listOf(HadesCyan.copy(alpha = 0.3f), dotColor))
                            else
                                Brush.radialGradient(listOf(HadesNavyDark, HadesNavyDark))
                        )
                        .border(width = 2.dp, color = borderColor, shape = CircleShape)
                )
            }
        }
    }
}
