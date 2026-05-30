package com.example.hadescoin.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.ui.theme.*
import java.util.Locale

@Composable
fun HadesFinancialChart(
    ingresos: Double,
    egresos: Double,
    modifier: Modifier = Modifier
) {
    val totalMovimiento = ingresos + egresos

    // 1. Cálculo exacto asegurando el 100%
    val porcentajeIngresos = if (totalMovimiento > 0) {
        kotlin.math.round((ingresos / totalMovimiento) * 100).toInt()
    } else 0

    val porcentajeEgresos = if (totalMovimiento > 0) 100 - porcentajeIngresos else 0

    // Ángulos para el Canvas (360 grados proporcionales)
    val angularIngresos = (porcentajeIngresos / 100f) * 360f
    val angularEgresos = 360f - angularIngresos

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HadesNavyDark.copy(alpha = 0.4f))
            .padding(20.dp)
    ) {
        Text(
            text = "RESUMEN FINANCIERO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = HadesPurple,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lado Izquierdo: Gráfico circular (Dona) con texto central dinámico
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2

                    // Fondo del círculo
                    drawCircle(
                        color = HadesOnDark.copy(alpha = 0.05f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )

                    if (totalMovimiento > 0) {
                        // Arco de Ingresos (Cyan) - Primero para que sea el dominante arriba
                        drawArc(
                            color = HadesCyan,
                            startAngle = -90f,
                            sweepAngle = angularIngresos,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Arco de Egresos (Naranja)
                        drawArc(
                            color = HadesOrange,
                            startAngle = -90f + angularIngresos,
                            sweepAngle = angularEgresos,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // 2. Texto central dinámico y centrado
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$porcentajeIngresos%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = HadesCyan,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ÉXITO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = HadesOnDark.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // 3. Estructura limpia (Lado Derecho: Leyenda)
            Column(modifier = Modifier.weight(1f)) {
                ChartLegendItem(
                    label = "Ingresos",
                    amount = ingresos,
                    color = HadesCyan,
                    percentage = porcentajeIngresos.toDouble()
                )
                Spacer(modifier = Modifier.height(14.dp))
                ChartLegendItem(
                    label = "Egresos",
                    amount = egresos,
                    color = HadesOrange,
                    percentage = porcentajeEgresos.toDouble()
                )
            }
        }
    }
}

@Composable
private fun ChartLegendItem(
    label: String,
    amount: Double,
    color: Color,
    percentage: Double
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = HadesOnDark.copy(alpha = 0.6f)
                )
                Text(
                    text = "${percentage.toInt()}%",
                    fontSize = 11.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$ ${String.format(Locale.US, "%,.2f", amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HadesOnDark
            )
        }
    }
}
