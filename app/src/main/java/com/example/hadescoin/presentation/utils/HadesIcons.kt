package com.example.hadescoin.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Íconos personalizados de trazo fino (estilo Lucide) para HadesCoin.
 * Sin dependencias externas — vectores embebidos directamente.
 */
object HadesIcons {

    /**
     * Flecha hacia abajo con línea base — representa DEPÓSITO.
     * Equivalente a lucide:arrow-down-to-line
     */
    val ArrowDownToLine: ImageVector by lazy {
        ImageVector.Builder(
            name            = "ArrowDownToLine",
            defaultWidth   = 24.dp,
            defaultHeight  = 24.dp,
            viewportWidth  = 24f,
            viewportHeight = 24f
        ).apply {
            // línea horizontal en la base
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(5f, 20f)
                lineTo(19f, 20f)
            }
            // flecha hacia abajo (cuerpo + cabeza)
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(12f, 4f)
                lineTo(12f, 16f)
            }
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(8f, 12f)
                lineTo(12f, 16f)
                lineTo(16f, 12f)
            }
        }.build()
    }

    /**
     * Flecha hacia arriba con línea base — representa RETIRO.
     * Equivalente a lucide:arrow-up-from-line
     */
    val ArrowUpFromLine: ImageVector by lazy {
        ImageVector.Builder(
            name            = "ArrowUpFromLine",
            defaultWidth   = 24.dp,
            defaultHeight  = 24.dp,
            viewportWidth  = 24f,
            viewportHeight = 24f
        ).apply {
            // línea horizontal en la base
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(5f, 20f)
                lineTo(19f, 20f)
            }
            // flecha hacia arriba (cuerpo + cabeza)
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(12f, 16f)
                lineTo(12f, 4f)
            }
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(8f, 8f)
                lineTo(12f, 4f)
                lineTo(16f, 8f)
            }
        }.build()
    }

    /**
     * Edificio con columnas (banco/cajero) — representa RETIRAR EN CAJERO en el SpeedDial.
     * Equivalente a lucide:landmark
     */
    val Landmark: ImageVector by lazy {
        ImageVector.Builder(
            name            = "Landmark",
            defaultWidth   = 24.dp,
            defaultHeight  = 24.dp,
            viewportWidth  = 24f,
            viewportHeight = 24f
        ).apply {
            // línea base inferior
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(3f, 22f)
                lineTo(21f, 22f)
            }
            // friso superior (línea sobre columnas)
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(6f, 18f)
                lineTo(18f, 18f)
            }
            // triángulo del techo
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(2f, 10f)
                lineTo(12f, 2f)
                lineTo(22f, 10f)
                lineTo(2f, 10f)
                close()
            }
            // columna izquierda
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(8f, 18f)
                lineTo(8f, 10f)
            }
            // columna central
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(12f, 18f)
                lineTo(12f, 10f)
            }
            // columna derecha
            path(
                stroke            = SolidColor(Color.Black),
                strokeLineWidth   = 2f,
                strokeLineCap     = StrokeCap.Round,
                strokeLineJoin    = StrokeJoin.Round,
                fillAlpha         = 0f,
                fill              = SolidColor(Color.Transparent),
                pathFillType      = PathFillType.NonZero
            ) {
                moveTo(16f, 18f)
                lineTo(16f, 10f)
            }
        }.build()
    }
}
