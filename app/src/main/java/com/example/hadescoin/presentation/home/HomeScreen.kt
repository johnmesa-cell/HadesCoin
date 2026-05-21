package com.example.hadescoin.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hadescoin.R
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import java.util.Locale

// ==========================================
// 1. EL VIEWMODEL CORREGIDO
// ==========================================
class HomeViewModel : ViewModel() {
    var isLoading by mutableStateOf(true)
    var appUser by mutableStateOf<AppUser?>(null)
    var transactions by mutableStateOf<List<WalletTransaction>>(emptyList())

    private var isDataLoaded = false

    fun loadWalletData(phoneNumber: String) {
        if (isDataLoaded) return

        isLoading = true
        viewModelScopeLoadingSim(phoneNumber)
    }

    private fun viewModelScopeLoadingSim(phoneNumber: String) {
        // 1. CAMBIO CLAVE: Cambiamos el estado a falso para que desaparezca el "Cargando..."
        isLoading = false
        isDataLoaded = true

        // 2. Cargamos datos de prueba para que la pantalla no aparezca en blanco ($0.00)
        appUser = AppUser(
            fullName = "Usuario de Prueba",
            balance = 1250.50 // O el saldo que quieras probar
        )

        transactions = listOf(
            WalletTransaction(type = "DEPOSIT", createdAt = "2026-05-21", amount = 500.0),
            WalletTransaction(type = "WITHDRAW", createdAt = "2026-05-20", amount = 50.25)
        )

        /* NOTA PARA EL FUTURO: Cuando conectes Firebase, tu código real debería verse así:

        tuRepositorio.loadWalletData(phoneNumber) { success, user, txList ->
            isLoading = false // Apaga el cargando tras recibir respuesta
            if (success && user != null) {
                appUser = user
                transactions = txList ?: emptyList()
                isDataLoaded = true
            }
        }
        */
    }
}

// ==========================================
// 2. LA PANTALLA PRINCIPAL (Sin cambios requeridos)
// ==========================================
@Composable
fun HomeScreen(
    phoneNumber: String,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(key1 = phoneNumber) {
        viewModel.loadWalletData(phoneNumber)
    }

    if (viewModel.isLoading) {
        ShowLoadingAlertDialog()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "HadesCoin Wallet",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "${stringResource(id = R.string.text_welcome)} ${viewModel.appUser?.fullName ?: ""}",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = stringResource(id = R.string.label_available_balance), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", viewModel.appUser?.balance ?: 0.0)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.label_transactions_history),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.transactions) { tx ->
                TransactionRow(tx = tx)
            }
        }
    }
}

// ==========================================
// 3. COMPONENTE PARA CADA FILA (Sin cambios requeridos)
// ==========================================
@Composable
fun TransactionRow(tx: WalletTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = tx.type, fontWeight = FontWeight.Bold)
                val dateText = if (tx.createdAt.length >= 10) tx.createdAt.take(10) else tx.createdAt
                Text(text = dateText, fontSize = 12.sp)
            }

            val isIncome = tx.type == "INCOME" || tx.type == "DEPOSIT"
            val amountColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            val prefix = if (isIncome) "+" else "-"

            Text(
                text = "$prefix$${String.format(Locale.US, "%.2f", tx.amount)}",
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
