# 🏗️ IMPLEMENTACIÓN — Lista 2: Home funcional real

---

## Contexto

La cadena de datos ya existe completa en el proyecto:
`GetWalletDataUseCase → WalletRepositoryImpl → FirebaseTransactionDataSource + FirebaseUserDataSource`

Solo falta crear el ViewModel real y reescribir la pantalla para que los consuma.

Modelos disponibles:
- `AppUser` → campos: `id`, `documentNumber`, `phoneNumber`, `fullName`, `pin`, `balance: Double`, `createdAt`
- `WalletTransaction` → campos: `id`, `amount: Double`, `type`, `createdAt`
- Colores del tema: `HadesBlack`, `HadesNavyDark`, `HadesNavy`, `HadesPurple`, `HadesPurpleGlow`, `HadesCyan`, `HadesOrange`, `HadesOnDark`

---

## PASO 1 — Crear HomeViewModel.kt (archivo nuevo)

**Ruta:** `app/src/main/java/com/example/hadescoin/presentation/home/HomeViewModel.kt`

```kotlin
package com.example.hadescoin.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase

class HomeViewModel(
    private val getWalletDataUseCase: GetWalletDataUseCase = ServiceLocator.provideGetWalletDataUseCase()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var appUser by mutableStateOf<AppUser?>(null)
        private set

    var transactions by mutableStateOf<List<WalletTransaction>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var isDataLoaded = false

    fun loadWalletData(documentNumber: String) {
        if (isDataLoaded) return

        isLoading = true

        getWalletDataUseCase(documentNumber) { success, user, txList ->
            isLoading = false
            if (success && user != null) {
                appUser = user
                transactions = txList ?: emptyList()
                isDataLoaded = true
            } else {
                errorMessage = "No se pudo cargar la información. Intenta de nuevo."
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
```

---

## PASO 2 — Agregar provideHomeViewModel() al ServiceLocator

**Archivo:** `app/src/main/java/com/example/hadescoin/di/ServiceLocator.kt`

Agrega esta función al final del objeto, antes del cierre `}`:

```kotlin
fun provideGetWalletDataUseCase(): GetWalletDataUseCase {
    return GetWalletDataUseCase(walletRepository)
}
```

> ⚠️ Este método ya existe. Verificar que esté presente. Si ya está, no agregar nada.

---

## PASO 3 — Reescribir HomeScreen.kt completo

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/home/HomeScreen.kt`

Reemplaza el archivo completo con esto:

```kotlin
package com.example.hadescoin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.ShowMessageAlertDialog
import com.example.hadescoin.ui.theme.*
import java.util.Locale

private val BackgroundGradient = Brush.verticalGradient(listOf(HadesBlack, HadesNavyDark, HadesBlack))

@Composable
fun HomeScreen(
    phoneNumber: String,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(key1 = phoneNumber) {
        viewModel.loadWalletData(phoneNumber)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
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
                text = "Hola, ${viewModel.appUser?.fullName ?: ""}",
                fontSize = 14.sp,
                color = HadesOnDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de saldo
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
                        text = "$ ${String.format(Locale.US, "%,.2f", viewModel.appUser?.balance ?: 0.0)}",
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

            if (viewModel.transactions.isEmpty() && !viewModel.isLoading) {
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
                    items(viewModel.transactions) { tx ->
                        TransactionRow(tx = tx)
                    }
                }
            }
        }
    }

    if (viewModel.isLoading) {
        ShowLoadingAlertDialog()
    }

    viewModel.errorMessage?.let { message ->
        ShowMessageAlertDialog(
            onConfirmation = { viewModel.clearError() },
            dialogTitle = "Error",
            dialogText = message
        )
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val isIncome = tx.type == "INCOME" || tx.type == "DEPOSIT"
    val amountColor = if (isIncome) HadesCyan else HadesOrange
    val prefix = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HadesNavyDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = tx.type,
                fontWeight = FontWeight.Bold,
                color = HadesOnDark,
                fontSize = 14.sp
            )
            val dateText = if (tx.createdAt.length >= 10) tx.createdAt.take(10) else tx.createdAt
            Text(
                text = dateText,
                fontSize = 11.sp,
                color = HadesOnDark.copy(alpha = 0.5f)
            )
        }

        Text(
            text = "$prefix$ ${String.format(Locale.US, "%.2f", tx.amount)}",
            fontWeight = FontWeight.Black,
            color = amountColor,
            fontSize = 16.sp
        )
    }
}
```

---

## PASO 4 — Verificar que AppNavigation pasa documentNumber correctamente

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/navigation/AppNavigation.kt`

Confirmar que el bloque del Home quedó así (aplicado en la Lista 1):

```kotlin
composable(
    route = "home/{documentNumber}",
    arguments = listOf(navArgument("documentNumber") { type = NavType.StringType })
) { backStackEntry ->
    val documentNumber = backStackEntry.arguments?.getString("documentNumber") ?: ""
    HomeScreen(phoneNumber = documentNumber)
}
```

> Si ya se aplicó la Corrección 5 de la Lista 1, este paso no requiere ningún cambio.

---

## Orden de aplicación

1. Crear `HomeViewModel.kt` (archivo nuevo) — Paso 1
2. Verificar `ServiceLocator.kt` tiene `provideGetWalletDataUseCase()` — Paso 2
3. Reescribir `HomeScreen.kt` — Paso 3
4. Verificar `AppNavigation.kt` — Paso 4

---

## Flujo completo resultante

```
HomeScreen
    └── LaunchedEffect → viewModel.loadWalletData(documentNumber)
            └── GetWalletDataUseCase(documentNumber)
                    └── WalletRepositoryImpl
                            ├── FirebaseUserDataSource.getUser()  → AppUser
                            └── FirebaseTransactionDataSource.getTransactions() → List<WalletTransaction>
```

El ViewModel actualiza `isLoading`, `appUser`, `transactions` y `errorMessage`.
La pantalla reacciona automáticamente a cada cambio de estado.
