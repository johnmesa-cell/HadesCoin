# ✅ CORRECCIONES — Lista 1: Código actual (sin tocar Home)

---

## CORRECCIÓN 1 — Conectar LoginViewModel al ServiceLocator

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/auth/login/LoginViewModel.kt`

Reemplaza el archivo completo con esto:

```kotlin
package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.ViewModel
import com.example.hadescoin.R
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.LoginUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase = ServiceLocator.provideLoginUseCase()
) : ViewModel() {

    fun login(documentNumber: String, pin: String, onResult: (Boolean, Int) -> Unit) {
        if (documentNumber.trim().isEmpty() || pin.trim().isEmpty()) {
            onResult(false, R.string.error_login_failed)
            return
        }

        loginUseCase(documentNumber, pin) { success, messageResId ->
            onResult(success, messageResId)
        }
    }
}
```

**Qué cambió:** Se reemplazó `LoginUseCase(AuthRepositoryImpl())` por `ServiceLocator.provideLoginUseCase()`.

---

## CORRECCIÓN 2 — Conectar RegisterViewModel al ServiceLocator

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/auth/register/RegisterViewModel.kt`

Reemplaza el archivo completo con esto:

```kotlin
package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.ViewModel
import com.example.hadescoin.R
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.RegisterUseCase

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase = ServiceLocator.provideRegisterUseCase()
) : ViewModel() {

    fun register(
        documentNumber: String,
        phoneNumber: String,
        fullName: String,
        pin: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        if (documentNumber.trim().isEmpty() || phoneNumber.trim().isEmpty() ||
            fullName.trim().isEmpty() || pin.trim().isEmpty()) {
            onResult(false, R.string.error_register_failed)
            return
        }

        val nuevoUsuario = AppUser(
            documentNumber = documentNumber,
            phoneNumber = phoneNumber,
            fullName = fullName,
            pin = pin,
            balance = 0.0
        )

        registerUseCase(nuevoUsuario) { success, messageResId ->
            onResult(success, messageResId)
        }
    }
}
```

**Qué cambió:** Se reemplazó `RegisterUseCase(AuthRepositoryImpl())` por `ServiceLocator.provideRegisterUseCase()`.

---

## CORRECCIÓN 3 — Quitar los @Suppress("UNUSED") del ServiceLocator

**Archivo:** `app/src/main/java/com/example/hadescoin/di/ServiceLocator.kt`

Reemplaza el archivo completo con esto:

```kotlin
package com.example.hadescoin.di

import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.repository.AuthRepository
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.RegisterUseCase

object ServiceLocator {

    private val firebaseUserDataSource by lazy { FirebaseUserDataSource() }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(firebaseUserDataSource)
    }

    val walletRepository by lazy {
        WalletRepositoryImpl(firebaseUserDataSource)
    }

    fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    fun provideRegisterUseCase(): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }

    fun provideGetWalletDataUseCase(): GetWalletDataUseCase {
        return GetWalletDataUseCase(walletRepository)
    }
}
```

**Qué cambió:** Se eliminaron todos los `@Suppress("UNUSED")` porque los métodos ahora sí se usan.

---

## CORRECCIÓN 4 — Extraer la referencia de Firebase a un datasource propio

**Paso 4a — Crear nuevo archivo:**
`app/src/main/java/com/example/hadescoin/data/datasource/FirebaseTransactionDataSource.kt`

Contenido:

```kotlin
package com.example.hadescoin.data.datasource

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FirebaseTransactionDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("transactions")

    fun getTransactions(): Task<DataSnapshot> {
        return database.get()
    }
}
```

---

**Paso 4b — Actualizar WalletRepositoryImpl para usar el nuevo datasource:**

**Archivo:** `app/src/main/java/com/example/hadescoin/data/repository/WalletRepositoryImpl.kt`

Reemplaza el archivo completo con esto:

```kotlin
package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository

class WalletRepositoryImpl(
    private val userDataSource: FirebaseUserDataSource = FirebaseUserDataSource(),
    private val transactionDataSource: FirebaseTransactionDataSource = FirebaseTransactionDataSource()
) : WalletRepository {

    override fun getWalletData(
        documentNumber: String,
        onResult: (success: Boolean, user: AppUser?, transactions: List<WalletTransaction>?) -> Unit
    ) {
        userDataSource.getUser(documentNumber)
            .addOnSuccessListener { userSnapshot ->
                if (!userSnapshot.exists()) {
                    onResult(false, null, null)
                    return@addOnSuccessListener
                }

                val balanceStr = userSnapshot.child("balance").value?.toString() ?: "0.0"
                val appUser = AppUser(
                    documentNumber = userSnapshot.child("documentNumber").value?.toString() ?: "",
                    phoneNumber = userSnapshot.child("phoneNumber").value?.toString() ?: "",
                    fullName = userSnapshot.child("fullName").value?.toString() ?: "",
                    pin = userSnapshot.child("pin").value?.toString() ?: "",
                    balance = balanceStr.toDoubleOrNull() ?: 0.0,
                    createdAt = userSnapshot.child("createdAt").value?.toString() ?: ""
                )

                transactionDataSource.getTransactions()
                    .addOnSuccessListener { txSnapshot ->
                        val transactionsList = mutableListOf<WalletTransaction>()

                        for (child in txSnapshot.children) {
                            val senderId = child.child("senderId").value?.toString() ?: ""
                            val receiverId = child.child("receiverId").value?.toString() ?: ""

                            if (senderId == appUser.phoneNumber || receiverId == appUser.phoneNumber) {
                                val amountStr = child.child("amount").value?.toString() ?: "0.0"
                                val transaction = WalletTransaction(
                                    id = child.key ?: "",
                                    amount = amountStr.toDoubleOrNull() ?: 0.0,
                                    type = child.child("type").value?.toString() ?: "TRANSFER",
                                    createdAt = child.child("timestamp").value?.toString() ?: ""
                                )
                                transactionsList.add(transaction)
                            }
                        }

                        onResult(true, appUser, transactionsList)
                    }
                    .addOnFailureListener {
                        onResult(false, appUser, null)
                    }
            }
            .addOnFailureListener {
                onResult(false, null, null)
            }
    }
}
```

**Qué cambió:** Se eliminó `FirebaseDatabase.getInstance()` del repositorio y se reemplazó por `transactionDataSource.getTransactions()`.

---

## CORRECCIÓN 5 — Unificar el nombre del argumento de navegación

El dato que se pasa al Home es el número de documento, pero hoy está nombrado de 3 formas distintas
(`userId` en la ruta, `docNumber` en el login, `phoneNumber` en HomeScreen). Hay que unificar todo a `documentNumber`.

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/navigation/AppNavigation.kt`

Reemplaza únicamente el bloque `composable` de home:

```kotlin
composable(
    route = "home/{documentNumber}",
    arguments = listOf(navArgument("documentNumber") { type = NavType.StringType })
) { backStackEntry ->
    val documentNumber = backStackEntry.arguments?.getString("documentNumber") ?: ""
    HomeScreen(phoneNumber = documentNumber)
}
```

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/auth/login/LoginView.kt`

Busca la línea donde se navega al home y cámbiala:

```kotlin
// ANTES
navController.navigate("home/$docNumber") {

// DESPUÉS
navController.navigate("home/$docNumber") {   // <-- docNumber ya es el número de documento, solo cambia la ruta arriba
```

> ⚠️ La ruta ya queda consistente con el cambio en AppNavigation. No hay que tocar nada más en LoginView.

---

## CORRECCIÓN 6 — Limpiar los comentarios numerados estilo tutorial

**Archivos afectados:**
- `LoginView.kt` — eliminar los comentarios `// 1.`, `// 2.`, `// 3.`
- `RegisterScreen.kt` — eliminar los comentarios `// 1.` al `// 5.`

**Regla:** Eliminar cualquier comentario que empiece con un número seguido de punto, o que diga palabras como "Corrección:", "Optimización:", "Sin cambios requeridos". Los comentarios que explican una decisión de diseño no obvia se pueden quedar.

---

## CORRECCIÓN 7 — Limpiar el HomeScreen placeholder

**Archivo:** `app/src/main/java/com/example/hadescoin/presentation/home/HomeScreen.kt`

Reemplaza el archivo completo con esto (mínimo y limpio hasta que se implemente de verdad):

```kotlin
package com.example.hadescoin.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(phoneNumber: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Bienvenido: $phoneNumber")
    }
}
```

**Qué cambió:** Se eliminó el ViewModel falso con datos hardcodeados, los comentarios "NOTA PARA EL FUTURO", y la lógica simulada. Queda solo la navegación funcional.

---

## Orden recomendado para aplicar los cambios

1. Corrección 4a — Crear `FirebaseTransactionDataSource.kt` (archivo nuevo)
2. Corrección 4b — Actualizar `WalletRepositoryImpl.kt`
3. Corrección 3 — Actualizar `ServiceLocator.kt`
4. Corrección 1 — Actualizar `LoginViewModel.kt`
5. Corrección 2 — Actualizar `RegisterViewModel.kt`
6. Corrección 5 — Actualizar `AppNavigation.kt`
7. Corrección 7 — Limpiar `HomeScreen.kt`
8. Corrección 6 — Limpiar comentarios en `LoginView.kt` y `RegisterScreen.kt`
