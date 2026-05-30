package com.example.hadescoin.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * El campo de texto real es invisible — captura el input detras de los puntos.
 *
 * @param value      PIN actual (max 4 digitos)
 * @param onValueChange  callback con el nuevo valor
 * @param dotSize    tamano de cada circulo (default 18.dp)
 * @param spacing    separacion entre circulos (default 20.dp)
 */
@Composable
fun HadesPinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dotSize: Dp = 18.dp,
    spacing: Dp = 20.dp
) {
    val focusRequester = remember { FocusRequester() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Campo invisible que captura el input
        BasicTextField(
            value               = value,
            onValueChange       = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onValueChange(it) },
            keyboardOptions     = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine          = true,
            cursorBrush         = SolidColor(Color.Transparent),
            modifier            = Modifier
                .size(1.dp)  // invisible pero enfocable
                .focusRequester(focusRequester),
            decorationBox       = { it() }
        )

        // 4 circulos visuales
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val filled = index < value.length
                val dotColor by animateColorAsState(
                    targetValue = if (filled) HadesPurple else Color.Transparent,
                    animationSpec = tween(durationMillis = 150),
                    label = "pin_dot_$index"
                )
                val borderColor by animateColorAsState(
                    targetValue = when {
                        filled && index == value.length - 1 -> HadesCyan   // ultimo ingresado: cyan
                        filled                              -> HadesPurple
                        else                               -> HadesOrange.copy(alpha = 0.4f)
                    },
                    animationSpec = tween(durationMillis = 150),
                    label = "pin_border_$index"
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(
                            brush = if (filled) Brush.radialGradient(
                                listOf(HadesCyan.copy(alpha = 0.3f), dotColor)
                            ) else Brush.radialGradient(
                                listOf(HadesNavyDark, HadesNavyDark)
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
