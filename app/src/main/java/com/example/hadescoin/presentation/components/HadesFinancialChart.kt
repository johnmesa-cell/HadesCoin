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
    val total = ingresos + egresos
    // Calculamos los ángulos. Si no hay datos, no dibujamos arcos.
    val angularEgresos = if (total > 0) (egresos / total * 360f).toFloat() else 0f
    val angularIngresos = if (total > 0) (ingresos / total * 360f).toFloat() else 0f

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
            // Lado Izquierdo: Gráfico circular (Dona)
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2

                    // Fondo del círculo (pista vacía)
                    drawCircle(
                        color = HadesOnDark.copy(alpha = 0.05f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )

                    if (total > 0) {
                        // Arco de Egresos (Naranja)
                        drawArc(
                            color = HadesOrange,
                            startAngle = -90f,
                            sweepAngle = angularEgresos,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Arco de Ingresos (Cyan)
                        drawArc(
                            color = HadesCyan,
                            startAngle = -90f + angularEgresos,
                            sweepAngle = angularIngresos,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (total > 0) "${((ingresos / total) * 100).toInt()}%" else "0%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = HadesCyan
                    )
                    Text(
                        text = "ÉXITO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = HadesOnDark.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Lado Derecho: Leyenda detallada
            Column(modifier = Modifier.weight(1f)) {
                ChartLegendItem(
                    label = "Ingresos",
                    amount = ingresos,
                    color = HadesCyan,
                    percentage = if (total > 0) (ingresos / total * 100) else 0.0
                )
                Spacer(modifier = Modifier.height(14.dp))
                ChartLegendItem(
                    label = "Egresos",
                    amount = egresos,
                    color = HadesOrange,
                    percentage = if (total > 0) (egresos / total * 100) else 0.0
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

