# Instrucciones para GitHub Copilot — HadesCoin

## Contexto del proyecto
HadesCoin es una billetera digital Android (similar a Nequi/Daviplata) desarrollada en **Android Studio** con **Kotlin nativo** y **Jetpack Compose**. El proyecto sigue arquitectura **Clean Architecture + MVVM**.

---

## Reglas generales — SIEMPRE respetar

- Lenguaje: **Kotlin** únicamente
- UI: **Jetpack Compose** (NO usar XML layouts)
- Base de datos: **Firebase Realtime Database** únicamente
- **NO usar** Firebase Authentication
- **NO usar** AuthRepository ni ningún Repository para autenticación
- **NO usar** ViewModelFactory
- **NO usar** UiState (LoginUiState, RegisterUiState)
- **NO usar** Hilt ni inyección de dependencias
- **NO encriptar** contraseñas — el PIN se guarda en **texto plano**
- El ViewModel instancia `FirebaseDatabase.getInstance()` directamente
- Usar `LiveData` y `MutableLiveData` para exponer estado a la UI
- Usar `viewModelScope.launch` + `kotlinx.coroutines.tasks.await` para operaciones async

---

## Estructura de paquetes

```
com.example.hadescoin
├── presentation/
│   ├── auth/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt       ← Jetpack Compose UI
│   │   │   └── LoginViewModel.kt    ← ViewModel con FirebaseDatabase directo
│   │   └── register/
│   │       ├── RegisterScreen.kt    ← Jetpack Compose UI
│   │       └── RegisterViewModel.kt ← ViewModel con FirebaseDatabase directo
│   └── home/
│       └── HomeScreen.kt
├── domain/
│   └── model/
│       └── AppUser.kt
└── data/
    └── remote/
        └── firebase/
            └── realtime/
                └── UserRealtimeDataSource.kt
```

---

## Modelo de datos — AppUser.kt

```kotlin
data class AppUser(
    val id: String = "",
    val documentNumber: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val pin: String = "",         // texto plano, sin encriptar
    val balance: Double = 0.0,
    val createdAt: String = ""
)
```

---

## Estructura de Firebase Realtime Database

```json
{
  "users": {
    "user_prueba_001": {
      "documentNumber": "1010101010",
      "phoneNumber": "3001234567",
      "fullName": "Juan Pérez",
      "pin": "1234",
      "balance": 150000.0,
      "createdAt": "2026-04-24T00:00:00Z"
    },
    "user_prueba_002": {
      "documentNumber": "2020202020",
      "phoneNumber": "3119876543",
      "fullName": "María López",
      "pin": "5678",
      "balance": 75000.0,
      "createdAt": "2026-04-24T00:00:00Z"
    },
    "user_prueba_003": {
      "documentNumber": "3030303030",
      "phoneNumber": "3204567890",
      "fullName": "Carlos Gómez",
      "pin": "9012",
      "balance": 200000.0,
      "createdAt": "2026-04-24T00:00:00Z"
    }
  },
  "transactions": {
    "tx_001": {
      "senderId": "user_prueba_001",
      "receiverId": "user_prueba_002",
      "amount": 50000.0,
      "type": "TRANSFER",
      "timestamp": "2026-04-24T10:00:00Z"
    }
  }
}
```

---

## LoginViewModel.kt — Patrón correcto

```kotlin
package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    private val _loginExitoso = MutableLiveData<String>()
    val loginExitoso: LiveData<String> = _loginExitoso

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun login(phoneNumber: String, pin: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Completa todos los campos"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val snapshot = database.getReference("users").get().await()
                var encontrado = false
                for (userSnapshot in snapshot.children) {
                    val phone = userSnapshot.child("phoneNumber").getValue(String::class.java)
                    val storedPin = userSnapshot.child("pin").getValue(String::class.java)
                    val fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "Usuario"
                    if (phone == phoneNumber && storedPin == pin) {
                        encontrado = true
                        _loginExitoso.value = "¡Bienvenido, $fullName!"
                        break
                    }
                }
                if (!encontrado) _loginError.value = "Teléfono o PIN incorrectos"
            } catch (e: Exception) {
                _loginError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
```

---

## RegisterViewModel.kt — Patrón correcto

```kotlin
package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    private val _registroExitoso = MutableLiveData<String>()
    val registroExitoso: LiveData<String> = _registroExitoso

    private val _registroError = MutableLiveData<String>()
    val registroError: LiveData<String> = _registroError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun register(documentNumber: String, phoneNumber: String, pin: String) {
        if (documentNumber.isBlank() || phoneNumber.isBlank() || pin.isBlank()) {
            _registroError.value = "Completa todos los campos"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val fechaActual = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()
                ).format(Date())
                val nuevoUsuario = mapOf(
                    "documentNumber" to documentNumber,
                    "phoneNumber" to phoneNumber,
                    "pin" to pin,
                    "fullName" to "",
                    "balance" to 0.0,
                    "createdAt" to fechaActual
                )
                database.getReference("users").push().setValue(nuevoUsuario).await()
                _registroExitoso.value = "¡Cuenta creada exitosamente!"
            } catch (e: Exception) {
                _registroError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
```

---

## Cómo instanciar ViewModels en las Screens

```kotlin
// SIN factory, SIN Hilt — así de simple
val viewModel: LoginViewModel = viewModel()
val viewModel: RegisterViewModel = viewModel()
```

---

## Snackbar — patrón para mensajes de feedback al usuario

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val loginExitoso by viewModel.loginExitoso.observeAsState()
val loginError by viewModel.loginError.observeAsState()

LaunchedEffect(loginExitoso) {
    loginExitoso?.let { snackbarHostState.showSnackbar(it) }
}
LaunchedEffect(loginError) {
    loginError?.let { snackbarHostState.showSnackbar(it) }
}

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
    // contenido de la pantalla
}
```

---

## Archivos que NO deben existir — eliminar si están presentes

- `LoginUiState.kt`
- `RegisterUiState.kt`
- `LoginViewModelFactory.kt`
- `RegisterViewModelFactory.kt`
- Cualquier archivo en `domain/repository/` relacionado con autenticación

---

## Dependencias necesarias en app/build.gradle.kts

```kotlin
implementation(libs.firebase.database)                       // Realtime Database
implementation(libs.androidx.lifecycle.viewmodel.compose)    // viewModel()
implementation(libs.androidx.lifecycle.livedata.ktx)         // LiveData
implementation(libs.androidx.runtime.livedata)               // observeAsState()
implementation(libs.kotlinx.coroutines.play.services)        // .await() en coroutines
```
