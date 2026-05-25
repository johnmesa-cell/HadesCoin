package com.example.hadescoin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
    navController: NavController,
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
        cargando     = cargando,
        onRefresh    = { viewModel.refresh() },
        onLogout     = {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    )

    if (cargando) ShowLoadingAlertDialog()

    if (showError) {
        ShowMessageAlertDialog(
            onConfirmation = {
                viewModel.clearError()
                showError = false
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
    cargando: Boolean,
    onRefresh: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(HadesBlack, HadesNavyDark, HadesBlack)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── HEADER ──────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(40.dp))
                HomeHeader(appUser = appUser, onRefresh = onRefresh, onLogout = onLogout)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── TARJETA DE SALDO ─────────────────────────────────────────
            item {
                BalanceCard(appUser = appUser)
                Spacer(modifier = Modifier.height(28.dp))
            }

            // ── TÍTULO MOVIMIENTOS ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> MOVIMIENTOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = HadesCyan
                    )
                    Text(
                        text = "${transactions.size} registros",
                        fontSize = 11.sp,
                        color = HadesOnDark.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── LISTA VACÍA ──────────────────────────────────────────────
            if (transactions.isEmpty() && !cargando) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "// SIN MOVIMIENTOS",
                                color = HadesOnDark.copy(alpha = 0.3f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No hay transacciones registradas",
                                color = HadesOnDark.copy(alpha = 0.25f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── FILAS DE TRANSACCIONES ───────────────────────────────────
            items(transactions) { tx ->
                TransactionRow(tx = tx)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// HEADER CON INICIALES Y BOTÓN REFRESH
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    appUser: AppUser?,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Círculo con iniciales
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(HadesPurple, HadesNavyDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(appUser?.fullName),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = HadesOnDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "HADESCOIN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = HadesPurple
                )
                Text(
                    text = "Hola, ${appUser?.fullName ?: "..."}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = HadesOnDark.copy(alpha = 0.8f)
                )
            }
        }

        // Botones refresh y logout
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Actualizar",
                    tint = HadesCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Salir",
                    tint = HadesOrange,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// TARJETA DE SALDO CON TELÉFONO
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun BalanceCard(appUser: AppUser?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(HadesPurple, HadesPurpleGlow, HadesNavyDark)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Saldo disponible",
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = HadesOnDark.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$ ${String.format(Locale.US, "%,.2f", appUser?.balance ?: 0.0)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = HadesOnDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Separador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HadesOnDark.copy(alpha = 0.15f))
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TELÉFONO",
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = HadesOnDark.copy(alpha = 0.5f)
                    )
                    Text(
                        text = appUser?.phoneNumber ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HadesOnDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DOCUMENTO",
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = HadesOnDark.copy(alpha = 0.5f)
                    )
                    Text(
                        text = appUser?.documentNumber ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HadesOnDark
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// FILA DE TRANSACCIÓN CON ÍCONO
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val isIncome    = tx.type == "INCOME" || tx.type == "DEPOSIT"
    val amountColor = if (isIncome) HadesCyan else HadesOrange
    val prefix      = if (isIncome) "+" else "-"
    val icon        = if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward
    val typeLabel   = translateTransactionType(tx.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HadesNavyDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Ícono de dirección
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = typeLabel,
                    tint = amountColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = typeLabel,
                    fontWeight = FontWeight.Bold,
                    color = HadesOnDark,
                    fontSize = 14.sp
                )
                val dateText = if (tx.timestamp.length >= 10) tx.timestamp.take(10) else tx.timestamp
                Text(
                    text = dateText,
                    fontSize = 11.sp,
                    color = HadesOnDark.copy(alpha = 0.45f)
                )
            }
        }

        Text(
            text = "$prefix$ ${String.format(Locale.US, "%,.2f", tx.amount)}",
            fontWeight = FontWeight.Black,
            color = amountColor,
            fontSize = 15.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────
private fun getInitials(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "?"
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase(java.util.Locale.getDefault())
        parts.size == 1 -> parts[0].take(2).uppercase(java.util.Locale.getDefault())
        else -> "?"
    }
}

private fun translateTransactionType(type: String): String {
    return when (type.uppercase()) {
        "DEPOSIT"  -> "Depósito"
        "WITHDRAW" -> "Retiro"
        "TRANSFER" -> "Transferencia"
        "INCOME"   -> "Ingreso"
        "PAYMENT"  -> "Pago"
        else       -> type
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
            appUser      = AppUser(fullName = "Juan Pérez", balance = 0.0, phoneNumber = "3001234567", documentNumber = "1010101010"),
            transactions = emptyList(),
            cargando     = false,
            onRefresh    = {},
            onLogout     = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — con datos")
@Composable
fun HomeScreenFilledPreview() {
    HadesCoinTheme {
        HomeContent(
            appUser = AppUser(
                fullName = "Juan Pérez",
                balance = 1250.50,
                phoneNumber = "3001234567",
                documentNumber = "1010101010"
            ),
            transactions = listOf(
                WalletTransaction(type = "DEPOSIT",  amount = 500.0,  timestamp = "2026-05-21T10:00:00Z"),
                WalletTransaction(type = "WITHDRAW", amount = 50.25,  timestamp = "2026-05-20T08:00:00Z"),
                WalletTransaction(type = "TRANSFER", amount = 200.0,  timestamp = "2026-05-19T15:00:00Z"),
                WalletTransaction(type = "INCOME",   amount = 1000.0, timestamp = "2026-05-18T09:00:00Z"),
                WalletTransaction(type = "PAYMENT",  amount = 75.0,   timestamp = "2026-05-17T12:00:00Z")
            ),
            cargando  = false,
            onRefresh = {},
            onLogout  = {}
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
            cargando     = true,
            onRefresh    = {},
            onLogout     = {}
        )
    }
}
