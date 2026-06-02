package com.example.hadescoin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadescoin.ui.theme.HadesCyan

@Composable
fun HadesSectionHeader(
    text     : String,
    modifier : Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text          = text,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.sp,
            color         = HadesCyan
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            HadesCyan.copy(alpha = 0.4f),
                            HadesCyan.copy(alpha = 0f)
                        )
                    )
                )
        )
    }
}
