# Reporte de Wildcard Imports — HadesCoin
Fecha: 2026-05-01
Generado por: Perplexity AI

---

## Análisis por Archivo

### Archivo: `presentation/auth/login/LoginScreen.kt`

  ✅ `androidx.compose.foundation.*`
     (reemplaza: background, rememberScrollState, verticalScroll)
  ✅ `androidx.compose.foundation.layout.*`
     (reemplaza: Arrangement, Column, Row, Spacer, fillMaxSize, fillMaxWidth, height, padding, size)
  ✅ `androidx.compose.material3.*`
     (reemplaza: Button, CircularProgressIndicator, IconButton, MaterialTheme, OutlinedTextField, Scaffold, SnackbarHost, SnackbarHostState, Text, TextButton)
  ✅ `androidx.compose.runtime.*`
     (reemplaza: Composable, LaunchedEffect, getValue, mutableStateOf, remember, setValue)
  ✅ `androidx.compose.ui.text.input.*`
     (reemplaza: ImeAction, KeyboardType, PasswordVisualTransformation, VisualTransformation)
  ℹ️  Paquetes con <3 clases (no candidatos): foundation.text (KeyboardActions, KeyboardOptions); runtime.livedata (observeAsState); compose.ui (Alignment, Modifier); ui.focus (FocusDirection); ui.platform (LocalFocusManager); text.font (FontWeight); tooling.preview (Preview); ui.unit (dp); viewmodel.compose (viewModel); ui.theme (HadesCoinTheme)

---

### Archivo: `presentation/auth/login/LoginViewModel.kt`

  ✅ `androidx.lifecycle.*`
     (reemplaza: LiveData, MutableLiveData, ViewModel, viewModelScope)
  ⛔ `com.google.firebase.database` → dejar explícito (FirebaseDatabase)
  ℹ️  Paquetes con <3 clases (no candidatos): kotlinx.coroutines (launch); coroutines.tasks (await)

---

### Archivo: `presentation/auth/login/LoginViewModelFactory.kt`

  ℹ️  Sin candidatos a wildcard — solo 1-2 clases por paquete (androidx.lifecycle.ViewModel, ViewModelProvider; domain.usecase.auth.LoginUseCase)

---

### Archivo: `presentation/auth/register/RegisterScreen.kt`

  ✅ `androidx.compose.foundation.*`
     (reemplaza: background, rememberScrollState, verticalScroll)
  ✅ `androidx.compose.foundation.layout.*`
     (reemplaza: Arrangement, Column, Row, Spacer, fillMaxSize, fillMaxWidth, height, padding, size)
  ✅ `androidx.compose.material3.*`
     (reemplaza: Button, CircularProgressIndicator, ExperimentalMaterial3Api, IconButton, MaterialTheme, OutlinedTextField, Scaffold, SnackbarHost, SnackbarHostState, Text, TextButton, TopAppBar)
  ✅ `androidx.compose.runtime.*`
     (reemplaza: Composable, LaunchedEffect, getValue, mutableStateOf, remember, setValue)
  ✅ `androidx.compose.ui.text.input.*`
     (reemplaza: ImeAction, KeyboardType, PasswordVisualTransformation, VisualTransformation)
  ℹ️  Paquetes con <3 clases (no candidatos): foundation.text (KeyboardActions, KeyboardOptions); runtime.livedata (observeAsState); compose.ui (Alignment, Modifier); ui.focus (FocusDirection); ui.platform (LocalFocusManager); text.font (FontWeight); tooling.preview (Preview); ui.unit (dp); viewmodel.compose (viewModel); ui.theme (HadesCoinTheme)

---

### Archivo: `presentation/auth/register/RegisterViewModel.kt`

  ✅ `androidx.lifecycle.*`
     (reemplaza: LiveData, MutableLiveData, ViewModel, viewModelScope)
  ⛔ `com.google.firebase.database` → dejar explícito (FirebaseDatabase)
  ℹ️  Paquetes con <3 clases (no candidatos): java.text (SimpleDateFormat); java.util (Date, UUID); kotlinx.coroutines (launch); coroutines.tasks (await)

---

### Archivo: `presentation/auth/register/RegisterViewModelFactory.kt`

  ℹ️  Sin candidatos a wildcard — solo 1-2 clases por paquete

---

### Archivo: `presentation/home/HomeScreen.kt`

  ✅ `androidx.compose.foundation.layout.*`
     (reemplaza: Column, PaddingValues, Row, Spacer, fillMaxSize, fillMaxWidth, height, padding, size)
  ✅ `androidx.compose.material3.*`
     (reemplaza: Card, CircularProgressIndicator, MaterialTheme, Scaffold, Text, TopAppBar)
  ✅ `androidx.compose.runtime.*`
     (reemplaza: Composable, collectAsState, getValue)
  ℹ️  Paquetes con <3 clases (no candidatos): compose.ui (Alignment, Modifier); ui.unit (dp, sp); tooling.preview (Preview); viewmodel.compose (viewModel); ui.theme (HadesCoinTheme)

---

### Archivo: `presentation/home/HomeViewModel.kt`

  ℹ️  Sin candidatos a wildcard — paquetes con <3 clases (androidx.lifecycle.ViewModel; kotlinx.coroutines.flow.StateFlow; etc.)

---

### Archivo: `presentation/navigation/AppNavHost.kt`

  ℹ️  Sin candidatos a wildcard — usa clases individuales de navigation.compose y presentation.*

---

### Archivo: `data/repository/AuthRepositoryImpl.kt`

  ℹ️  Sin candidatos a wildcard — implementación delgada con pocas dependencias de cada paquete

---

### Archivo: `data/repository/WalletRepositoryImpl.kt`

  ℹ️  Sin candidatos a wildcard

---

### Archivo: `data/remote/firebase/auth/FirebaseAuthDataSource.kt`

  ⛔ `com.google.firebase.auth` → dejar explícito (FirebaseAuth, FirebaseUser)
  ℹ️  Sin candidatos a wildcard por regla Firebase

---

### Archivo: `data/remote/firebase/realtime/WalletFirestoreDataSource.kt`

  ⛔ `com.google.firebase.database` → dejar explícito (DataSnapshot, DatabaseError, DatabaseReference, FirebaseDatabase, ValueEventListener)
  ℹ️  Sin candidatos a wildcard por regla Firebase

---

### Archivo: `data/remote/firebase/realtime/UserFirestoreDataSource.kt`

  ⛔ `com.google.firebase.database` → dejar explícito (DataSnapshot, DatabaseError, DatabaseReference, FirebaseDatabase, ValueEventListener)
  ℹ️  Sin candidatos a wildcard por regla Firebase

---

### Archivo: `data/remote/firebase/realtime/TransactionFirestoreDataSource.kt`

  ⛔ `com.google.firebase.database` → dejar explícito (DataSnapshot, DatabaseError, DatabaseReference, FirebaseDatabase, ValueEventListener)
  ℹ️  Sin candidatos a wildcard por regla Firebase

---

### Archivo: `data/mapper/TransactionMapper.kt`

  ℹ️  Sin candidatos a wildcard — mappers simples con pocas dependencias externas

---

### Archivo: `domain/repository/AuthRepository.kt`

  ℹ️  Sin candidatos — interfaz con imports mínimos

---

### Archivo: `domain/repository/WalletRepository.kt`

  ℹ️  Sin candidatos — interfaz con imports mínimos

---

### Archivo: `domain/usecase/auth/LoginUseCase.kt`

  ℹ️  Sin candidatos a wildcard

---

### Archivo: `domain/usecase/auth/RegisterUseCase.kt`

  ℹ️  Sin candidatos a wildcard

---

### Archivo: `di/FirebaseModule.kt`

  ⛔ `com.google.firebase.auth` → dejar explícito
  ⛔ `com.google.firebase.database` → dejar explícito
  ℹ️  Sin candidatos a wildcard por regla Firebase/Hilt

---

### Archivo: `ui/theme/Theme.kt`

  ✅ `androidx.compose.material3.*`
     (reemplaza: ColorScheme, MaterialTheme, darkColorScheme, lightColorScheme)
  ℹ️  Paquetes con <3 clases (no candidatos): compose.runtime (Composable); compose.ui (Actual, LocalContext)

---

### Archivo: `ui/theme/Color.kt`

  ℹ️  Sin candidatos — solo usa `androidx.compose.ui.graphics.Color`

---

### Archivo: `ui/theme/Type.kt`

  ℹ️  Sin candidatos — pocos imports de material3 y ui.text

---

### Archivo: `androidTest/ExampleInstrumentedTest.kt`

  ℹ️  Sin candidatos a wildcard

---

### Archivo: `test/ExampleUnitTest.kt`

  ℹ️  Sin candidatos a wildcard

---

## Resumen General

- **Total de archivos analizados:** 26
- **Total de wildcards posibles:** 15
- **Archivos con al menos un wildcard candidato:** 7

## Paquetes más repetidos en el proyecto

| Paquete | Archivos que lo usan (≥3 clases) |
|---------|----------------------------------|
| `androidx.compose.material3` | 3 archivos |
| `androidx.compose.foundation` | 2 archivos |
| `androidx.compose.foundation.layout` | 2 archivos |
| `androidx.compose.runtime` | 2 archivos |
| `androidx.compose.ui.text.input` | 2 archivos |
| `androidx.lifecycle` | 2 archivos |
| `kotlinx.coroutines.flow` | 1 archivo |
| `androidx.navigation.compose` | 1 archivo |

---

## Reglas aplicadas

- ✅ Wildcard sugerido SOLO cuando ≥3 clases del mismo paquete son importadas
- ⛔ Firebase (`com.google.firebase`), Hilt/Dagger (`dagger.hilt`, `dagger`, `javax.inject`) → siempre explícitos
- ℹ️  Paquetes con 1-2 clases → no se sugiere wildcard
- ⚠️  Ningún archivo `.kt` fue modificado — este es solo un reporte de análisis
