# HadesCoin — Documentación Técnica del Proyecto

**Repositorio:** [github.com/johnmesa-cell/HadesCoin](https://github.com/johnmesa-cell/HadesCoin)  
**Plataforma:** Android Nativo (Android Studio)  
**Lenguaje:** Kotlin  
**Arquitectura:** MVVM (Model – View – ViewModel) con separación en capas  
**UI Framework:** Jetpack Compose + Material Design 3  
**Backend:** Firebase Realtime Database

---

## 1. Descripción General

HadesCoin es una aplicación móvil nativa de Android que funciona como **billetera digital**. Permite a los usuarios registrarse con número de documento, teléfono y PIN; iniciar sesión; y acceder a un dashboard (aún en construcción) donde visualizarán su saldo y transacciones.

El proyecto sigue el patrón **MVVM** y está organizado en capas claras: `domain` (modelos de datos), `presentation` (pantallas + ViewModels) y `ui` (tema visual). Se comunica con **Firebase Realtime Database** como base de datos en la nube.

---

## 2. Estructura de Carpetas del Proyecto

### 2.1 Raíz del repositorio

```
HadesCoin/
├── .github/          → Configuraciones de GitHub (workflows, templates)
├── .idea/            → Archivos internos de Android Studio (no se versiona en equipos)
├── app/              → Módulo principal de la aplicación Android
├── docs/             → Documentación del proyecto (esta carpeta)
├── gradle/           → Wrapper de Gradle (versión del build system)
├── build.gradle.kts  → Script de build a nivel raíz del proyecto
├── gradle.properties → Propiedades globales de Gradle (JVM flags, etc.)
├── gradlew           → Script para ejecutar Gradle en Linux/Mac
├── gradlew.bat       → Script para ejecutar Gradle en Windows
└── settings.gradle.kts → Define el nombre del proyecto y qué módulos incluye
```

| Carpeta / Archivo | Propósito |
|---|---|
| `.github/` | Automatizaciones de GitHub: CI/CD, plantillas de issues y PRs |
| `.idea/` | Metadatos del proyecto para Android Studio (SDK, configuraciones locales) |
| `app/` | Contiene todo el código fuente, recursos y configuración de la app |
| `docs/` | Documentación técnica del proyecto |
| `gradle/` | Contiene `gradle-wrapper.jar` y `gradle-wrapper.properties` para fijar la versión de Gradle |
| `build.gradle.kts` | Declara los plugins disponibles para todos los módulos del proyecto |
| `gradle.properties` | Ajustes de performance de Gradle: habilita el daemon, caché, etc. |
| `settings.gradle.kts` | Punto de entrada de Gradle: registra el nombre del proyecto y el módulo `app` |

---

### 2.2 Módulo `app/`

```
app/
├── build.gradle.kts      → Dependencias y configuración del módulo Android
├── proguard-rules.pro     → Reglas de ofuscación de código para release
└── src/
    ├── androidTest/       → Tests de instrumentación (se ejecutan en un dispositivo/emulador)
    ├── test/              → Tests unitarios (se ejecutan en la JVM local)
    └── main/
        ├── AndroidManifest.xml  → Declaración de la app: actividades, permisos, tema
        ├── java/                → Código fuente Kotlin
        └── res/                 → Recursos: iconos, colores XML, strings, etc.
```

---

### 2.3 Paquete principal `com.example.hadescoin`

```
com.example.hadescoin/
├── HadesCoinApp.kt              → Clase Application (punto de entrada de la app)
├── MainActivity.kt              → Actividad principal; inicializa Compose y la navegación
├── domain/
│   └── model/
│       ├── AppUser.kt           → Modelo de datos del usuario
│       └── WalletTransaction.kt → Modelo de datos de una transacción
├── presentation/
│   ├── auth/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt      → Vista (UI) de la pantalla de Login
│   │   │   └── LoginViewModel.kt   → Lógica de negocio del Login
│   │   └── register/
│   │       ├── RegisterScreen.kt   → Vista (UI) de la pantalla de Registro
│   │       └── RegisterViewModel.kt → Lógica de negocio del Registro
│   ├── home/
│   │   ├── HomeScreen.kt        → Vista del Dashboard (implementación temporal)
│   │   └── HomeViewModel.kt     → ViewModel del Home (estructura base)
│   └── navigation/
│       ├── AppNavHost.kt        → Grafo de navegación de la app
│       └── Screen.kt            → Definición de las rutas de navegación
└── ui/
    └── theme/
        ├── Color.kt             → Colores base del tema
        ├── Theme.kt             → Configuración del tema Material 3
        └── Type.kt              → Tipografía del tema
```

---

## 3. Capa `domain/model` — Los Modelos de Datos

Los modelos representan la **estructura de los datos** que maneja la aplicación. Al ser `data class` de Kotlin, incluyen automáticamente `equals()`, `hashCode()`, `copy()` y `toString()`.

---

### 3.1 `AppUser.kt`

**¿Qué es?** El modelo que representa a un usuario registrado en la aplicación.

```kotlin
data class AppUser(
    val id: String = "",
    val documentNumber: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val pin: String = "",
    val balance: Double = 0.0,
    val createdAt: String = ""
)
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único generado por Firebase (key del nodo) |
| `documentNumber` | `String` | Número de cédula o documento del usuario |
| `phoneNumber` | `String` | Número de teléfono; funciona también como "número de cuenta" |
| `fullName` | `String` | Nombre completo del usuario |
| `pin` | `String` | PIN de 4 dígitos para autenticación |
| `balance` | `Double` | Saldo actual de la billetera en la moneda del sistema |
| `createdAt` | `String` | Fecha de creación de la cuenta en formato ISO 8601 |

**Valores por defecto:** todos los campos tienen un default (vacío o 0.0), lo que permite la deserialización automática desde Firebase sin errores de `NullPointerException`.

---

### 3.2 `WalletTransaction.kt`

**¿Qué es?** El modelo que representa una transacción dentro de la billetera.

```kotlin
data class WalletTransaction(
    val id: String,
    val amount: Double,
    val type: String,
    val createdAt: Long
)
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único de la transacción |
| `amount` | `Double` | Monto de la transacción (positivo = ingreso, negativo = egreso) |
| `type` | `String` | Tipo de transacción: `"credit"`, `"debit"`, `"transfer"`, etc. |
| `createdAt` | `Long` | Timestamp Unix en milisegundos del momento de la transacción |

> **Nota:** A diferencia de `AppUser`, `WalletTransaction` no tiene valores por defecto, por lo que todos sus campos son obligatorios al instanciar el objeto.

---

## 4. Capa `presentation` — ViewModels y Pantallas

Esta capa implementa el patrón **MVVM** de Android. Cada feature (auth, home) tiene su propia carpeta con dos archivos: el `ViewModel` (lógica) y el `Screen` (UI).

---

### 4.1 `LoginViewModel.kt`

**¿Qué es?** El ViewModel que contiene toda la lógica de negocio de la pantalla de inicio de sesión.

#### Importaciones

| Importación | Para qué sirve |
|---|---|
| `androidx.lifecycle.LiveData` | Tipo observable de solo lectura expuesto a la UI |
| `androidx.lifecycle.MutableLiveData` | Tipo observable mutable que el ViewModel puede modificar internamente |
| `androidx.lifecycle.ViewModel` | Clase base de todos los ViewModels; sobrevive a rotaciones de pantalla |
| `androidx.lifecycle.viewModelScope` | Scope de corrutinas ligado al ciclo de vida del ViewModel |
| `com.google.firebase.database.FirebaseDatabase` | Punto de entrada al SDK de Firebase Realtime Database |
| `kotlinx.coroutines.launch` | Lanza una corrutina en el scope dado |
| `kotlinx.coroutines.tasks.await` | Extiende las `Task` de Firebase para poder usarlas con `suspend` / `await` |

#### Estados que expone

```kotlin
val loginExitoso: LiveData<String>  // Emite el nombre del usuario al loguearse correctamente
val loginError: LiveData<String>    // Emite un mensaje de error si algo falla
val cargando: LiveData<Boolean>     // Emite true mientras espera respuesta de Firebase
```

#### Lógica del método `login()`

1. Valida que `phoneNumber` y `pin` no estén vacíos.
2. Lanza una corrutina con `viewModelScope.launch`.
3. Activa `_cargando = true`.
4. Consulta el nodo `"users"` en Firebase con `.get().await()`.
5. Itera los hijos buscando coincidencia de `phoneNumber` y `pin`.
6. Si encuentra al usuario → emite `_loginExitoso` con su nombre.
7. Si no lo encuentra → emite `_loginError`.
8. En el bloque `finally` → desactiva `_cargando = false`.

---

### 4.2 `LoginScreen.kt`

**¿Qué es?** La función `@Composable` que construye la interfaz visual de la pantalla de Login.

#### Importaciones

| Importación | Para qué sirve |
|---|---|
| `androidx.compose.foundation.*` | Layouts base: `background`, scroll, etc. |
| `androidx.compose.foundation.layout.*` | Composables de disposición: `Column`, `Row`, `Box`, `Spacer`, `fillMaxSize`, `padding`, etc. |
| `androidx.compose.foundation.text.KeyboardActions` | Acciones del teclado virtual al presionar "Next" o "Done" |
| `androidx.compose.foundation.text.KeyboardOptions` | Opciones del teclado: tipo numérico, tipo contraseña, acción IME |
| `androidx.compose.material3.*` | Componentes de Material Design 3: `Button`, `OutlinedTextField`, `Scaffold`, `SnackbarHost`, `Text`, `CircularProgressIndicator`, `IconButton`, etc. |
| `androidx.compose.runtime.*` | `remember`, `mutableStateOf`, `LaunchedEffect` para manejo de estado local |
| `androidx.compose.runtime.livedata.observeAsState` | Convierte un `LiveData` en un `State` de Compose para observarlo reactivamente |
| `androidx.compose.ui.Alignment` | Alineación de elementos: `Center`, `CenterHorizontally` |
| `androidx.compose.ui.Modifier` | Modificadores de UI: tamaño, padding, fondo, etc. |
| `androidx.compose.ui.focus.FocusDirection` | Mueve el foco entre campos de texto |
| `androidx.compose.ui.platform.LocalFocusManager` | Maneja el foco del teclado (ocultarlo o moverlo) |
| `androidx.compose.ui.text.font.FontWeight` | Peso de la fuente: `Bold`, `SemiBold`, etc. |
| `androidx.compose.ui.text.input.*` | `PasswordVisualTransformation`, `VisualTransformation`, `KeyboardType`, `ImeAction` |
| `androidx.compose.ui.tooling.preview.Preview` | Permite ver previews de la pantalla en Android Studio sin ejecutar la app |
| `androidx.compose.ui.unit.dp` | Unidad de medida para tamaños en Compose (density-independent pixels) |
| `androidx.lifecycle.viewmodel.compose.viewModel` | Obtiene o crea un ViewModel dentro de un Composable |
| `android.content.res.Configuration.UI_MODE_NIGHT_YES` | Constante para activar el modo oscuro en la preview |
| `com.example.hadescoin.ui.theme.HadesCoinTheme` | Aplica el tema visual de la app en los previews |

#### Parámetros de la función

```kotlin
fun LoginScreen(
    onLoginSuccess: () -> Unit,       // Lambda que la pantalla llama al loguearse bien → navega a Home
    onNavigateToRegister: () -> Unit, // Lambda que navega hacia la pantalla de Registro
    viewModel: LoginViewModel = viewModel() // ViewModel inyectado (con default para no romper el preview)
)
```

---

### 4.3 `RegisterViewModel.kt`

**¿Qué es?** El ViewModel que maneja la lógica de creación de una nueva cuenta de usuario.

#### Importaciones

| Importación | Para qué sirve |
|---|---|
| `androidx.lifecycle.LiveData` | Tipo observable de solo lectura |
| `androidx.lifecycle.MutableLiveData` | Tipo observable mutable interno |
| `androidx.lifecycle.ViewModel` | Clase base de ViewModels |
| `androidx.lifecycle.viewModelScope` | Scope de corrutinas del ViewModel |
| `com.google.firebase.database.FirebaseDatabase` | Acceso a Firebase Realtime Database |
| `kotlinx.coroutines.launch` | Lanza corrutinas asíncronas |
| `kotlinx.coroutines.tasks.await` | Convierte Tasks de Firebase en suspending functions |
| `java.text.SimpleDateFormat` | Formatea la fecha actual al crear el usuario |
| `java.util.Date` | Obtiene la fecha y hora actuales del sistema |
| `java.util.Locale` | Especifica el locale para el formateo de la fecha |

#### Estados que expone

```kotlin
val registroExitoso: LiveData<String>  // Emite mensaje de éxito al crear la cuenta
val registroError: LiveData<String>    // Emite mensaje de error si falla
val cargando: LiveData<Boolean>        // Controla el indicador de carga
```

#### Lógica del método `register()`

1. Valida que `documentNumber`, `phoneNumber` y `pin` no estén vacíos.
2. Lanza corrutina con `viewModelScope.launch`.
3. Genera la fecha actual con `SimpleDateFormat`.
4. Construye un `Map` con los datos del nuevo usuario (incluyendo `balance = 0.0`).
5. Llama a `database.getReference("users").push().setValue(nuevoUsuario).await()` para guardar en Firebase con una key auto-generada.
6. Emite `_registroExitoso` si tiene éxito, o `_registroError` si falla.

---

### 4.4 `RegisterScreen.kt`

**¿Qué es?** La función `@Composable` que construye la interfaz de registro de nuevos usuarios.

Comparte las mismas importaciones que `LoginScreen.kt`, con las siguientes adiciones:

| Importación adicional | Para qué sirve |
|---|---|
| `@OptIn(ExperimentalMaterial3Api::class)` | Habilita el uso de la API experimental `TopAppBar` de Material 3 |
| `TopAppBar` (vía `material3.*`) | Barra superior con título "Crear cuenta" y botón de regreso |

#### Parámetros de la función

```kotlin
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,  // Navega a Home tras registro exitoso
    onNavigateToLogin: () -> Unit,  // Regresa a la pantalla de Login
    viewModel: RegisterViewModel = viewModel()
)
```

---

### 4.5 `HomeScreen.kt` y `HomeViewModel.kt`

**¿Qué es?** La pantalla de inicio (Dashboard) de la billetera. Actualmente es una **implementación temporal** que sirve solo como destino de navegación tras un login/registro exitoso.

#### `HomeViewModel.kt` — Importaciones

| Importación | Para qué sirve |
|---|---|
| `androidx.lifecycle.ViewModel` | Clase base del ViewModel |
| `com.example.hadescoin.domain.model.AppUser` | Referencia al modelo de usuario para el estado de la UI |

Define también el `data class HomeUiState(val user: AppUser? = null)`, que representa el estado completo de la pantalla Home, preparado para cuando se implemente el dashboard completo.

#### `HomeScreen.kt` — Importaciones

| Importación | Para qué sirve |
|---|---|
| `androidx.compose.foundation.layout.*` | Layouts: `Column`, `fillMaxSize`, `padding`, `Arrangement`, `Spacer`, `height` |
| `androidx.compose.material3.*` | `Text`, `MaterialTheme` para estilos |
| `androidx.compose.runtime.Composable` | Anotación para marcar funciones como componentes UI |
| `androidx.compose.ui.Alignment` | Alineación centrada |
| `androidx.compose.ui.Modifier` | Modificadores de tamaño y espaciado |
| `androidx.compose.ui.text.font.FontWeight` | Negrita para el título |
| `androidx.compose.ui.unit.dp` | Medidas en dp |

---

### 4.6 `AppNavHost.kt`

**¿Qué es?** El componente que define el **grafo de navegación** de toda la aplicación. Conecta las rutas con las pantallas correspondientes.

#### Importaciones

| Importación | Para qué sirve |
|---|---|
| `androidx.compose.runtime.Composable` | Es un componente Composable |
| `androidx.navigation.NavHostController` | Controlador de navegación inyectado o creado |
| `androidx.navigation.compose.NavHost` | Contenedor del grafo de navegación |
| `androidx.navigation.compose.composable` | Registra una pantalla como destino de navegación |
| `androidx.navigation.compose.rememberNavController` | Crea y recuerda un `NavHostController` |
| `LoginScreen`, `RegisterScreen`, `HomeScreen` | Las pantallas que se registran como destinos |

#### Flujo de navegación

```
[Login] ──── onLoginSuccess ──────────────────► [Home]
   │                                              ▲
   └── onNavigateToRegister ──► [Register] ───────┘
                                    │
                              onNavigateToLogin
                                    │
                                    ▼
                                 [Login]
```

Al navegar a **Home** desde Login o Register, se limpia el backstack con `popUpTo(Login) { inclusive = true }`, evitando que el usuario pueda regresar con el botón Atrás.

---

### 4.7 `Screen.kt`

**¿Qué es?** Una `sealed class` que define las rutas de navegación como constantes tipadas, evitando el uso de strings literales dispersos por el código.

```kotlin
sealed class Screen(val route: String) {
    object Login    : Screen("login")
    object Register : Screen("register")
    object Home     : Screen("home")
}
```

> Usar `sealed class` en lugar de strings directos previene errores de typo y facilita el refactoring.

---

## 5. Capa `ui/theme` — El Tema Visual

Esta capa configura el aspecto visual global de la app usando **Material Design 3**.

### 5.1 `Color.kt`

Define los colores base (`Purple80`, `PurpleGrey80`, `Pink80`, `Purple40`, `PurpleGrey40`, `Pink40`) usados en el tema claro y oscuro. Son los colores generados por defecto por Android Studio al crear un nuevo proyecto con Material 3.

### 5.2 `Theme.kt`

**¿Qué es?** La función `@Composable` `HadesCoinTheme` que envuelve toda la app con el tema Material 3.

| Importación | Para qué sirve |
|---|---|
| `android.app.Activity` | Referencia a la Activity para Dynamic Color |
| `android.os.Build` | Verifica la versión de Android (necesario para Dynamic Color) |
| `androidx.compose.foundation.isSystemInDarkTheme` | Detecta si el sistema está en modo oscuro |
| `androidx.compose.material3.MaterialTheme` | Proveedor del tema Material 3 |
| `androidx.compose.material3.darkColorScheme` | Esquema de colores predefinido para modo oscuro |
| `androidx.compose.material3.dynamicDarkColorScheme` | Colores dinámicos del sistema en modo oscuro (Android 12+) |
| `androidx.compose.material3.dynamicLightColorScheme` | Colores dinámicos del sistema en modo claro (Android 12+) |
| `androidx.compose.material3.lightColorScheme` | Esquema de colores predefinido para modo claro |
| `androidx.compose.runtime.Composable` | Anotación de función Composable |
| `androidx.compose.ui.platform.LocalContext` | Accede al contexto Android dentro de un Composable |

**Dynamic Color:** En Android 12+ (`Build.VERSION_CODES.S`), el tema toma automáticamente los colores del fondo de pantalla del usuario. En versiones anteriores usa los colores definidos en `Color.kt`.

### 5.3 `Type.kt`

Define el objeto `Typography` que configura los estilos de texto del tema: `displayLarge`, `headlineLarge`, `bodyMedium`, `titleMedium`, etc., todos basados en la escala tipográfica de Material Design 3.

---

## 6. Archivos de Configuración del Módulo `app`

### 6.1 `AndroidManifest.xml`

Declara la aplicación ante el sistema Android: nombre del paquete, tema visual, permiso de internet (necesario para Firebase) y la `MainActivity` como punto de entrada con `intent-filter` de lanzamiento.

### 6.2 `app/build.gradle.kts`

#### Plugins utilizados

| Plugin | Para qué sirve |
|---|---|
| `com.android.application` | Indica que este módulo es una app Android (no una librería) |
| `org.jetbrains.kotlin.plugin.compose` | Habilita el compilador de Jetpack Compose para Kotlin |
| `com.google.gms.google-services` | Procesa el archivo `google-services.json` para conectar con Firebase |

#### Configuración Android

| Propiedad | Valor | Significado |
|---|---|---|
| `compileSdk` | 36 | Versión del SDK con la que se compila (Android 15) |
| `minSdk` | 26 | Versión mínima soportada: Android 8.0 Oreo |
| `targetSdk` | 36 | Versión objetivo de Android |
| `versionCode` | 1 | Número interno de versión (se incrementa en cada release) |
| `versionName` | "1.0" | Versión visible al usuario |

---

## 7. Dependencias del Proyecto

### 7.1 Jetpack Compose (UI)

| Dependencia | Para qué sirve |
|---|---|
| `androidx.compose.bom` | Bill of Materials: alinea automáticamente las versiones de todas las librerías de Compose |
| `androidx.activity.compose` | Integra Compose con `ComponentActivity` (`setContent {}`) |
| `androidx.compose.material3` | Componentes de Material Design 3: botones, campos de texto, tarjetas, etc. |
| `androidx.compose.ui` | Core de Compose: `Modifier`, `Composable`, layouts básicos |
| `androidx.compose.ui.graphics` | Colores, formas y gráficos en Compose |
| `androidx.compose.ui.tooling.preview` | Soporte para `@Preview` en Android Studio |

### 7.2 Lifecycle y ViewModel

| Dependencia | Para qué sirve |
|---|---|
| `androidx.lifecycle.runtime.ktx` | Extensiones Kotlin para el ciclo de vida: `lifecycleScope`, `repeatOnLifecycle` |
| `androidx.lifecycle.livedata.ktx` | Extensiones Kotlin para `LiveData`: `liveData {}`, `switchMap`, etc. |
| `lifecycle-viewmodel-compose:2.8.0` | Conecta ViewModels con Compose: función `viewModel()` en Composables |
| `lifecycle-runtime-compose:2.8.0` | `collectAsStateWithLifecycle()` para Flow → State en Compose |
| `runtime-livedata:1.7.6` | `observeAsState()` para usar `LiveData` dentro de Composables |

### 7.3 Navegación

| Dependencia | Para qué sirve |
|---|---|
| `navigation-compose:2.7.7` | Sistema de navegación de Jetpack integrado con Compose: `NavHost`, `composable()`, `NavController` |

### 7.4 Firebase

| Dependencia | Para qué sirve |
|---|---|
| `firebase-bom:34.12.0` | Bill of Materials de Firebase: sincroniza versiones de todos los SDKs de Firebase |
| `firebase-analytics` | Analytics automático de uso de la app (eventos, sesiones, etc.) |
| `firebase-database` | SDK de Firebase Realtime Database: lectura y escritura de datos en tiempo real |
| `kotlinx-coroutines-play-services` | Agrega la extensión `.await()` a las `Task` de Google Play Services, permitiendo usar Firebase con corrutinas |

### 7.5 Herramientas de Prueba

| Dependencia | Tipo | Para qué sirve |
|---|---|---|
| `junit` | `testImplementation` | Framework de pruebas unitarias en JVM |
| `androidx.espresso.core` | `androidTestImplementation` | Framework de pruebas de UI en dispositivo real/emulador |
| `androidx.compose.ui.test.junit4` | `androidTestImplementation` | Tests de Compose con JUnit 4 |
| `androidx.compose.ui.test.manifest` | `debugImplementation` | Manifiesto necesario para ejecutar tests de Compose |
| `androidx.compose.ui.tooling` | `debugImplementation` | Inspector de layouts de Compose en debug |

---

## 8. Archivos de Entrada de la Aplicación

### 8.1 `HadesCoinApp.kt`

Clase que extiende `Application`. Es el punto de arranque de Android antes de cualquier Activity. Actualmente tiene la estructura base; se utiliza para inicializar librerías globales en el futuro (por ejemplo: configurar Firebase, inyección de dependencias, logging, etc.).

### 8.2 `MainActivity.kt`

**¿Qué hace?**

1. Extiende `ComponentActivity` (la Activity base para Compose).
2. Llama a `enableEdgeToEdge()` para que la UI se extienda detrás de las barras del sistema (status bar + navigation bar).
3. Llama a `setContent {}` para montar el árbol de Compose.
4. Envuelve todo en `HadesCoinTheme {}` para aplicar el tema.
5. Lanza `AppNavHost()` como raíz de la navegación.

| Importación | Para qué sirve |
|---|---|
| `android.os.Bundle` | Datos del estado guardado de la Activity |
| `androidx.activity.ComponentActivity` | Clase base de Activity para Compose |
| `androidx.activity.compose.setContent` | Función de extensión para establecer un Composable como contenido de la Activity |
| `androidx.activity.enableEdgeToEdge` | Activa el diseño Edge-to-Edge (pantalla completa sin barras del sistema) |
| `AppNavHost` | Grafo de navegación de la app |
| `HadesCoinTheme` | Tema visual de la app |

---

## 9. Resumen del Patrón MVVM en HadesCoin

```
┌─────────────────────────────────────────────────────────────┐
│                        VISTA (View)                         │
│  LoginScreen.kt  │  RegisterScreen.kt  │  HomeScreen.kt     │
│         Composables @Composable — Solo UI, sin lógica       │
└──────────────────────────┬──────────────────────────────────┘
                           │ observa LiveData con observeAsState()
                           │ llama funciones del ViewModel
┌──────────────────────────▼──────────────────────────────────┐
│                       VIEWMODEL                             │
│  LoginViewModel  │  RegisterViewModel  │  HomeViewModel     │
│   Maneja estado (LiveData), lógica, llamadas a Firebase     │
└──────────────────────────┬──────────────────────────────────┘
                           │ lee / escribe datos
┌──────────────────────────▼──────────────────────────────────┐
│                  MODELO / DATOS (Model)                     │
│   AppUser.kt   │  WalletTransaction.kt  │  Firebase DB      │
│        Estructuras de datos y fuente de verdad              │
└─────────────────────────────────────────────────────────────┘
```

---

*Documento generado el 07/05/2026 — HadesCoin v1.0*
