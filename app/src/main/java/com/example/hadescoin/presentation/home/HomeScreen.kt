package com.example.hadescoin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.*
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────
// VISTA REAL
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    phoneNumber: String,
    viewModel: HomeViewModel = viewModel()
) {
    val cargando     by viewModel.cargando.observeAsState(false)
    val appUser      by viewModel.appUser.observeAsState()
    val transactions by viewModel.transactions.observeAsState(emptyList())
    val error        by viewModel.error.observeAsState()

    var showError    by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    LaunchedEffect(phoneNumber) {
        viewModel.loadWalletData(phoneNumber)
    }

    LaunchedEffect(error) {
        error?.let {
            mensajeError = it
            showError = true
        }
    }

    HomeContent(
        appUser      = appUser,
        transactions = transactions,
        cargando     = cargando
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = {
                showError = false
                viewModel.clearError()
            },
            dialogTitle = "Error",
            dialogText  = mensajeError
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// CONTENIDO VISUAL PURO (apto para @Preview)
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun HomeContent(
    appUser: AppUser?,
    transactions: List<WalletTransaction>,
    cargando: Boolean
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(HadesBlack, HadesNavyDark, HadesBlack)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "HADESCOIN",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = HadesPurple
            )

            Text(
                text = "Hola, ${appUser?.fullName ?: ""}",
                fontSize = 14.sp,
                color = HadesOnDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(listOf(HadesPurple, HadesPurpleGlow))
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Saldo disponible",
                        fontSize = 12.sp,
                        color = HadesOnDark.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$ ${String.format(Locale.US, "%,.2f", appUser?.balance ?: 0.0)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = HadesOnDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "> MOVIMIENTOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HadesCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty() && !cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin movimientos registrados",
                        color = HadesOnDark.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { tx ->
                        TransactionRow(tx = tx)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// COMPONENTE DE FILA DE TRANSACCIÓN
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val isIncome   = tx.type == "INCOME" || tx.type == "DEPOSIT"
    val amountColor = if (isIncome) HadesCyan else HadesOrange
    val prefix     = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HadesNavyDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = tx.type,
                fontWeight = FontWeight.Bold,
                color      = HadesOnDark,
                fontSize   = 14.sp
            )
            val dateText = if (tx.createdAt.length >= 10) tx.createdAt.take(10) else tx.createdAt
            Text(
                text     = dateText,
                fontSize = 11.sp,
                color    = HadesOnDark.copy(alpha = 0.5f)
            )
        }

        Text(
            text       = "$prefix$ ${String.format(Locale.US, "%.2f", tx.amount)}",
            fontWeight = FontWeight.Black,
            color      = amountColor,
            fontSize   = 16.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "Home — vacío")
@Composable
fun HomeScreenEmptyPreview() {
    HadesCoinTheme {
        HomeContent(
            appUser      = AppUser(fullName = "Juan Pérez", balance = 0.0),
            transactions = emptyList(),
            cargando     = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — con datos")
@Composable
fun HomeScreenFilledPreview() {
    HadesCoinTheme {
        HomeContent(
            appUser = AppUser(fullName = "Juan Pérez", balance = 1250.50),
            transactions = listOf(
                WalletTransaction(type = "DEPOSIT",  amount = 500.0,  createdAt = "2026-05-21"),
                WalletTransaction(type = "WITHDRAW", amount = 50.25,  createdAt = "2026-05-20"),
                WalletTransaction(type = "INCOME",   amount = 200.0,  createdAt = "2026-05-19")
            ),
            cargando = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — cargando")
@Composable
fun HomeScreenLoadingPreview() {
    HadesCoinTheme {
        HomeContent(
            appUser      = null,
            transactions = emptyList(),
            cargando     = true
        )
    }
}
