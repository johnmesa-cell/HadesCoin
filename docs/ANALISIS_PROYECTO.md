# 📊 REPORTE COMPLETO DE ANÁLISIS DEL PROYECTO MyBank

**Fecha de Análisis**: Mayo 25, 2026  
**Proyecto**: MyBank - Aplicación Android de Autenticación Bancaria  
**Ubicación**: `C:\Users\ao184\OneDrive\Documentos\GitHub\MyBank`

---

## SECCIÓN 1 — TECNOLOGÍAS Y DEPENDENCIAS

### 1.1 Dependencias del build.gradle.kts

| Biblioteca | Versión | Propósito |
|-----------|---------|----------|
| **androidx.core:core-ktx** | 1.18.0 | Extensiones de Kotlin para Android Core |
| **androidx.lifecycle:lifecycle-runtime-ktx** | 2.10.0 | Gestión del ciclo de vida de Android |
| **androidx.lifecycle:lifecycle-viewmodel-compose** | 2.8.7 | ViewModel integrado con Compose |
| **androidx.activity:activity-compose** | 1.13.0 | Integración de Activity con Compose |
| **androidx.compose.ui:ui** | 2024.09.00 | Framework de UI Compose (BOM) |
| **androidx.compose.ui:ui-graphics** | 2024.09.00 | APIs de gráficos de Compose |
| **androidx.compose.ui:ui-tooling-preview** | 2024.09.00 | Preview de diseño en desarrollo |
| **androidx.compose.material3:material3** | 2024.09.00 | Design System Material 3 |
| **androidx.compose.ui:ui-tooling** | 2024.09.00 | Herramientas de desarrollo de UI |
| **androidx.compose.ui:ui-test-junit4** | 2024.09.00 | Testing de UI en Compose |
| **androidx.navigation:navigation-compose** | 2.9.8 | Sistema de navegación en Compose |
| **com.google.firebase:firebase-database** | 22.0.1 | Firebase Realtime Database |
| **com.google.firebase:firebase-bom** | 34.12.0 | Bill of Materials para Firebase |
| **com.google.firebase:firebase-analytics** | (via BOM) | Analytics de Firebase |
| **junit:junit** | 4.13.2 | Framework de testing unitario |
| **androidx.test.ext:junit** | 1.3.0 | Testing extensions para JUnit |
| **androidx.test.espresso:espresso-core** | 3.7.0 | Testing de UI con Espresso |
| **com.android.application** | 9.0.1 (AGP) | Plugin de aplicación Android |
| **org.jetbrains.kotlin.plugin.compose** | 2.0.21 | Plugin de Kotlin Compose |
| **com.google.gms.google-services** | 4.4.4 | Plugin de servicios de Google |

### 1.2 Base de Datos
- **Tipo**: Firebase Realtime Database (NoSQL)
- **Configuración**: Acceso directo a través de `FirebaseDatabase.getInstance().getReference("users")`
- **Estructura raíz**: Nodo `"users"` que contiene documentos de usuarios
- **Autenticación**: Sin autenticación nativa de Firebase (credenciales almacenadas localmente)

### 1.3 Librerías de UI
- **Framework principal**: Jetpack Compose
- **Librería de componentes**: Material Design 3 (`androidx.compose.material3`)
- **Componentes utilizados**:
  - `Button`, `TextButton`: Botones de acción
  - `OutlinedTextField`: Campos de entrada de texto
  - `AlertDialog`: Diálogos de alerta
  - `CircularProgressIndicator`: Indicador de carga
  - `Text`, `Column`, `Row`: Layouts básicos
  - `MaterialTheme`: Tematización centralizada

### 1.4 Sistema de Navegación
- **Librería**: `androidx.navigation:navigation-compose` (versión 2.9.8)
- **Tipo**: Navegación basada en rutas (string)
- **Rutas definidas**:
  - `"login"` → Pantalla de inicio de sesión (start destination)
  - `"register"` → Pantalla de registro
  - `"home"` → Pantalla principal después de autenticarse

### 1.5 Manejo de Estado
- **Patrón**: Callback-based (callbacks con lambda functions)
- **Herramienta**: `remember` y `mutableStateOf` de Compose para estado local
- **NO implementa**: State management centralizado (Redux, MVI, MVVM completo)
- **Limitaciones**: Estado local en cada Composable, sin flujos reactivos (Flow/StateFlow)

### 1.6 Sistema de Inyección de Dependencias
- **Tipo**: Manual (sin framework de DI)
- **Implementación**: Instanciación directa en ViewModels
  ```kotlin
  // Ejemplo en LoginViewModel
  private val loginUseCase: LoginUseCase = LoginUseCase(FirebaseAuthRepositoryImpl())
  ```
- **Característica**: Default parameters en constructores para proporcionar implementaciones

---

## SECCIÓN 2 — ARQUITECTURA

### 2.1 Patrón Arquitectónico
**Clean Architecture + MVVM híbrido**

Estructura de capas:
```
presentation (UI/Composables/ViewModels)
    ↓
domain (Modelos de negocio, interfaces de repo, use cases)
    ↓
data (Implementaciones de repo, datasources)
```

### 2.2 Listado Completo de Paquetes

| Paquete | Contiene | Responsabilidad |
|---------|----------|----------------|
| `com.utp.mybank` | MainActivity | Punto de entrada de la app, inicializa contenido Compose |
| `com.utp.mybank.presentation` | - | Capa de presentación |
| `com.utp.mybank.presentation.login` | LoginView, LoginViewModel | Pantalla y lógica de login |
| `com.utp.mybank.presentation.register` | RegisterView, RegisterViewModel | Pantalla y lógica de registro |
| `com.utp.mybank.presentation.home` | HomeView | Pantalla principal (stub) |
| `com.utp.mybank.presentation.navigation` | AppNavigation | Grafo de navegación |
| `com.utp.mybank.presentation.components` | AlertDialogs | Componentes reutilizables |
| `com.utp.mybank.ui.theme` | Color, Theme, Type | Sistema de diseño centralizado |
| `com.utp.mybank.domain` | - | Capa de dominio |
| `com.utp.mybank.domain.model` | User | Entidad de usuario |
| `com.utp.mybank.domain.repository` | AuthRepository | Interfaz de repositorio |
| `com.utp.mybank.domain.usecase` | LoginUseCase, RegisterUseCase | Casos de uso |
| `com.utp.mybank.data` | - | Capa de datos |
| `com.utp.mybank.data.repository` | FirebaseAuthRepositoryImpl | Implementación de repositorio |
| `com.utp.mybank.data.datasource` | FirebaseUserDataSource | Acceso directo a Firebase |

### 2.3 Listado Completo de Archivos

#### Archivos de Configuración
| Archivo | Paquete | Responsabilidad |
|---------|---------|----------------|
| **MainActivity.kt** | com.utp.mybank | Activity principal, establece contenido Compose y tema |
| **AndroidManifest.xml** | - | Metadatos de la app, declaración de activities |
| **build.gradle.kts** | - | Configuración de compilación del módulo app |
| **gradle.properties** | - | Propiedades globales del proyecto |

#### Archivos de Dominio
| Archivo | Paquete | Responsabilidad |
|---------|---------|----------------|
| **User.kt** | com.utp.mybank.domain.model | Data class del usuario con fullName, documentNumber, password |
| **AuthRepository.kt** | com.utp.mybank.domain.repository | Interfaz que define login() y register() |
| **LoginUseCase.kt** | com.utp.mybank.domain.usecase | Encapsula lógica de login, delega a repositorio |
| **RegisterUseCase.kt** | com.utp.mybank.domain.usecase | Encapsula lógica de registro, delega a repositorio |

#### Archivos de Datos
| Archivo | Paquete | Responsabilidad |
|---------|---------|----------------|
| **FirebaseUserDataSource.kt** | com.utp.mybank.data.datasource | Acceso a Firebase: getUser(), saveUser() |
| **FirebaseAuthRepositoryImpl.kt** | com.utp.mybank.data.repository | Implementa AuthRepository con lógica de login/registro |

#### Archivos de Presentación
| Archivo | Paquete | Responsabilidad |
|---------|---------|----------------|
| **LoginView.kt** | com.utp.mybank.presentation.login | Composable con formulario de login |
| **LoginViewModel.kt** | com.utp.mybank.presentation.login | ViewModel de login con validaciones |
| **RegisterView.kt** | com.utp.mybank.presentation.register | Composable con formulario de registro |
| **RegisterViewModel.kt** | com.utp.mybank.presentation.register | ViewModel de registro con validaciones |
| **HomeView.kt** | com.utp.mybank.presentation.home | Composable stub de pantalla principal |
| **AppNavigation.kt** | com.utp.mybank.presentation.navigation | Define grafo de navegación con NavHost |
| **AlertDialogs.kt** | com.utp.mybank.presentation.components | Componentes ShowLoadingAlertDialog y ShowMessageAlertDialog |

#### Archivos de Diseño (UI)
| Archivo | Paquete | Responsabilidad |
|---------|---------|----------------|
| **Color.kt** | com.utp.mybank.ui.theme | Paleta de colores (Purple80, Purple40, Pink40, etc.) |
| **Theme.kt** | com.utp.mybank.ui.theme | Definición de MyBankTheme con soporte a tema oscuro y dinámico |
| **Type.kt** | com.utp.mybank.ui.theme | Tipografía Material 3 |

#### Archivos de Recursos
| Archivo | Ubicación | Responsabilidad |
|---------|-----------|----------------|
| **strings.xml** | res/values/ | Strings en español para UI (login, register, errores, etc.) |
| **colors.xml** | res/values/ | Colores básicos XML |

---

## SECCIÓN 3 — ESTRUCTURA DE DATOS

### 3.1 Modelos de Datos

#### **User.kt** (Data Class)
```kotlin
data class User(
    val fullName: String,        // Nombre completo del usuario
    val documentNumber: String,  // Número de documento (único identificador)
    val password: String         // Contraseña en texto plano
)
```

**Campos**: 3  
**Tipos**: Todos String

### 3.2 Estructura de Base de Datos (Firebase Realtime Database)

**Nodo raíz**: `users`

**Esquema JSON**:
```json
{
  "users": {
    "12345678": {
      "fullName": "Juan Pérez",
      "password": "encrypted_or_plain"
    },
    "87654321": {
      "fullName": "María García",
      "password": "encrypted_or_plain"
    }
  }
}
```

**Características**:
- **Colección raíz**: "users"
- **Documentos**: Identificados por `documentNumber`
- **Campos por documento**:
  - `fullName`: String (nombre del usuario)
  - `password`: String (contraseña)
- **NO almacena**: saldos, transacciones, o información de cuentas

### 3.3 Identificador Principal del Usuario

**Campo identificador**: `documentNumber` (Número de Documento)

**Uso**:
- Clave primaria en Firebase
- Parámetro para login
- Identificador único en el sistema

**Tipo**: String  
**Validación**: Requiere ser numérico (KeyboardType.Number en UI)

### 3.4 Estructura de Transacciones

⚠️ **NO EXISTE**: El proyecto actual **no implementa transacciones**

**Limitaciones identificadas**:
- No hay tabla/colección de transacciones
- No hay campos para saldo o balance
- HomeView es solo un stub sin funcionalidad
- No hay flujo de transferencias implementado

---

## SECCIÓN 4 — FLUJO DE AUTENTICACIÓN

### 4.1 Proceso de Login Paso a Paso

**Ruta de datos**: UI → ViewModel → UseCase → Repository → DataSource → Firebase

```
1. Usuario ingresa credenciales en LoginView
   ↓
2. Usuario presiona botón "Iniciar Sesión"
   ↓
3. Evento onClick en LoginViewModel.login(documentNumber, password)
   ↓
4. Validación en ViewModel: verificar campos no vacíos
   ↓
5. Si válido → LoginUseCase(documentNumber, password, callback)
   ↓
6. LoginUseCase delega a FirebaseAuthRepositoryImpl.login()
   ↓
7. Repository obtiene usuario: FirebaseUserDataSource.getUser(documentNumber)
   ↓
8. Firebase retorna Task<DataSnapshot>
   ↓
9. En onSuccessListener:
     a) Extrae password: dataUser.child("password").value.toString()
     b) Compara con password ingresado
     c) Si coinciden: callback(true, 0) → navega a "home"
     d) Si no coinciden: callback(false, R.string.error_login_failed) → muestra error
   ↓
10. En onFailureListener: callback(false, R.string.error_login_failed)
```

**Código real del flujo**:

**LoginView.kt - Inicio del flujo**:
```kotlin
Button(
    onClick = {
        showLoadingAlert = true
        viewModel.login(documentNumber, password) { success, message ->
            showLoadingAlert = false
            if (success) {
                navController.navigate("home")
            } else {
                titleDialog = R.string.dialog_error_title
                messageDialog = message
                showMessageAlert = true
            }
        }
    }
) { Text(text = stringResource(id = R.string.btn_login)) }
```

**LoginViewModel.kt - Lógica de validación**:
```kotlin
fun login(documentNumber: String, password: String, onResult: (Boolean, Int) -> Unit) {
    if (documentNumber.isBlank() || password.isBlank()) {
        onResult(false, com.utp.mybank.R.string.error_login_failed)
        return
    }
    loginUseCase(documentNumber, password, onResult)
}
```

**FirebaseAuthRepositoryImpl.kt - Consulta a Firebase**:
```kotlin
override fun login(documentNumber: String, password: String, onResult: (Boolean, Int) -> Unit) {
    dataSource.getUser(documentNumber)
        .addOnSuccessListener { dataUser ->
            val dbPassword = dataUser.child("password").value.toString()
            if (dbPassword == password) {
                onResult(true, 0)
            } else {
                onResult(false, R.string.error_login_failed)
            }
        }
        .addOnFailureListener {
            onResult(false, R.string.error_login_failed)
        }
}
```

**FirebaseUserDataSource.kt - Acceso a Firebase**:
```kotlin
fun getUser(documentNumber: String): Task<DataSnapshot> {
    return database.child(documentNumber).get()
}
```

### 4.2 Proceso de Registro Paso a Paso

**Ruta de datos**: UI → ViewModel → UseCase → Repository → DataSource → Firebase

```
1. Usuario ingresa datos en RegisterView
   ↓
2. Usuario presiona botón "Registrarse"
   ↓
3. Evento onClick en RegisterViewModel.register()
   ↓
4. Validaciones en ViewModel:
     a) Campos no vacíos (fullName, documentNumber, password)
     b) password == confirmPassword
   ↓
5. Si todas válidas → Crea User(fullName, documentNumber, password)
   ↓
6. RegisterUseCase(user, callback)
   ↓
7. RegisterUseCase delega a FirebaseAuthRepositoryImpl.register()
   ↓
8. Repository crea map: {fullName: x, password: y}
   ↓
9. Guarda: FirebaseUserDataSource.saveUser(documentNumber, userData)
   ↓
10. Firebase ejecuta setValue()
    ↓
11. En onSuccessListener: callback(true, R.string.register_success_message)
    ↓
12. En onFailureListener: callback(false, R.string.error_register_failed)
    ↓
13. Muestra AlertDialog con resultado
```

**Código real del flujo**:

**RegisterViewModel.kt - Validaciones y creación del usuario**:
```kotlin
fun register(
    fullName: String,
    documentNumber: String,
    password: String,
    confirmPassword: String,
    onResult: (Boolean, Int) -> Unit
) {
    if (fullName.isBlank() || documentNumber.isBlank() || password.isBlank()) {
        onResult(false, R.string.error_register_failed)
        return
    }

    if (password != confirmPassword) {
        onResult(false, R.string.error_passwords_match)
        return
    }

    val user = User(
        fullName = fullName,
        documentNumber = documentNumber,
        password = password
    )

    registerUseCase(user, onResult)
}
```

**FirebaseAuthRepositoryImpl.kt - Guardado en Firebase**:
```kotlin
override fun register(user: User, onResult: (Boolean, Int) -> Unit) {
    val userData = mapOf(
        "fullName" to user.fullName,
        "password" to user.password
    )

    dataSource.saveUser(user.documentNumber, userData)
        .addOnSuccessListener {
            onResult(true, R.string.register_success_message)
        }
        .addOnFailureListener {
            onResult(false, R.string.error_register_failed)
        }
}
```

**FirebaseUserDataSource.kt - Guardado en Firebase**:
```kotlin
fun saveUser(documentNumber: String, userData: Map<String, String>): Task<Void> {
    return database.child(documentNumber).setValue(userData)
}
```

### 4.3 Validaciones en Cada Campo

#### **Campo: documentNumber (Número de Documento)**
| Validación | Nivel | Implementación |
|-----------|-------|----------------|
| No vacío | ViewModel | `documentNumber.isBlank()` |
| Numérico | UI | `KeyboardOptions(keyboardType = KeyboardType.Number)` |
| Único | DB | NO VALIDADO (posible duplicado) |

#### **Campo: password (Contraseña)**
| Validación | Nivel | Implementación |
|-----------|-------|----------------|
| No vacío | ViewModel | `password.isBlank()` |
| Longitud mínima | NO | No implementada |
| Complejidad | NO | No implementada |
| Coincide confirmPassword | ViewModel | `password != confirmPassword` (solo en registro) |

#### **Campo: fullName (Nombre Completo)**
| Validación | Nivel | Implementación |
|-----------|-------|----------------|
| No vacío | ViewModel | `fullName.isBlank()` |
| Formato | NO | No validado |

### 4.4 Dato Identificador para Login

**Campo utilizado**: `documentNumber` (Número de Documento)

**Proceso**:
1. Usuario proporciona documentNumber + password
2. Se busca nodo `users/{documentNumber}` en Firebase
3. Se compara contraseña almacenada

**Limitación crítica**: La contraseña se almacena en texto plano (sin encriptar)

---

## SECCIÓN 5 — FLUJO DE TRANSFERENCIA

### ⚠️ NO IMPLEMENTADO

**Estado actual**: El proyecto **NO tiene funcionalidad de transferencias**

**Evidencias**:
1. **HomeView.kt** es un stub vacío:
   ```kotlin
   @Composable
   fun HomeView(navController: NavController) {
       Column(...) {
           Text(text = stringResource(id = R.string.login_title))
       }
   }
   ```

2. **No existen**:
   - Modelos de Transferencia
   - Casos de uso de transferencia
   - Validaciones de saldo
   - APIs de transferencia en repositorio
   - Pantallas de transferencia

3. **Base de datos NO contiene**:
   - Colección de transacciones
   - Campos de saldo
   - Información de cuentas bancarias

**Conclusión**: Las funcionalidades de transferencia y transacciones no están implementadas en este proyecto.

---

## SECCIÓN 6 — PANTALLAS Y NAVEGACIÓN

### 6.1 Listado de Pantallas

| Pantalla | Ruta | Propósito | Estado |
|----------|------|----------|--------|
| **LoginView** | "login" | Autenticación de usuarios | ✅ Completa |
| **RegisterView** | "register" | Registro de nuevos usuarios | ✅ Completa |
| **HomeView** | "home" | Pantalla principal post-auth | ⚠️ Stub/Incompleta |

### 6.2 Diagrama de Navegación

```
Inicio (startDestination)
    ↓
┌─────────────────┐
│   LoginView     │
│   "login"       │
└────┬────────┬───┘
     │        │
     │ register navController.navigate("register")
     │        │
     ↓        ↓
     ┌─────────────────────┐
     │  RegisterView       │
     │  "register"         │
     └────┬────────────┬───┘
          │            │
   Success│  popBackStack() / Back button
          │            │
          ↓            ↓
     ┌─────────────────────┐
     │   HomeView          │
     │   "home"            │
     └─────────────────────┘
```

**Código real (AppNavigation.kt)**:
```kotlin
@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login"){
            LoginView(navController = navController)
        }

        composable("register"){
            RegisterView(navController = navController)
        }

        composable("home"){
            HomeView(navController = navController)
        }
    }
}
```

### 6.3 Paso de Datos Entre Pantallas

| Origen | Destino | Datos | Método |
|--------|---------|-------|--------|
| LoginView | HomeView | Ninguno | `navController.navigate("home")` |
| LoginView | RegisterView | Ninguno | `navController.navigate("register")` |
| RegisterView | LoginView | Ninguno | `navController.popBackStack()` |

**Limitación**: No pasa información del usuario logueado a través de rutas

### 6.4 Contenido de Cada Pantalla

#### **LoginView**
```
┌─────────────────────────────────────┐
│          MyBank (Título)            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Número de Documento         │   │
│  │ [__________________]        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Contraseña                  │   │
│  │ [••••••••]                  │   │
│  └─────────────────────────────┘   │
│                                     │
│         [Iniciar Sesión]            │
│                                     │
│          ¿Olvidaste tu contraseña?  │
│                                     │
│    ¿No tienes una cuenta?           │
│         Regístrate aquí             │
└─────────────────────────────────────┘
```

**Elementos**:
- Título usando `stringResource(R.string.login_title)`
- Campo documentNumber (tipo Number)
- Campo password (tipo Password)
- Botón "Iniciar Sesión"
- Botón "¿Olvidaste tu contraseña?" (sin implementar)
- Link "Regístrate aquí"

#### **RegisterView**
```
┌─────────────────────────────────────┐
│      Crear Cuenta (Título)          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Nombre Completo             │   │
│  │ [__________________]        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Número de Documento         │   │
│  │ [__________________]        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Contraseña                  │   │
│  │ [••••••••]                  │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Confirmar Contraseña        │   │
│  │ [••••••••]                  │   │
│  └─────────────────────────────┘   │
│                                     │
│         [Registrarse]               │
│                                     │
│    ¿Ya tienes una cuenta?           │
│      Inicia sesión aquí             │
└─────────────────────────────────────┘
```

**Elementos**:
- Título usando `stringResource(R.string.register_title)`
- Campo fullName
- Campo documentNumber (tipo Number)
- Campo password (tipo Password)
- Campo confirmPassword (tipo Password)
- Botón "Registrarse"
- Link "Inicia sesión aquí"

#### **HomeView**
```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│          MyBank (Título)            │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

**Estado**: Stub sin funcionalidad real

---

## SECCIÓN 7 — PATRONES DE CÓDIGO

### 7.1 Exposición de Estado en ViewModel

#### **Patrón Actual: Callback-based (NO Observable)**

```kotlin
// LoginViewModel.kt
class LoginViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(FirebaseAuthRepositoryImpl())
) : ViewModel() {

    fun login(documentNumber: String, password: String, onResult: (Boolean, Int) -> Unit) {
        if (documentNumber.isBlank() || password.isBlank()) {
            onResult(false, com.utp.mybank.R.string.error_login_failed)
            return
        }
        loginUseCase(documentNumber, password, onResult)
    }
}
```

**Características del patrón**:
- ✅ No expone estado mutable
- ✅ Utiliza lambdas (callbacks) para retornar resultados
- ❌ No es reactivo (no hay Flow/StateFlow)
- ❌ No es observable en tiempo real
- ❌ Manejo imperativo de resultados

### 7.2 Observación de Estado en la Pantalla

#### **Patrón Actual: Callbacks + remember + mutableStateOf**

```kotlin
// LoginView.kt
@Composable
fun LoginView(
    viewModel: LoginViewModel = viewModel(),
    navController: NavController
) {
    // Estado local en el Composable
    var documentNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showLoadingAlert by remember { mutableStateOf(false) }
    var showMessageAlert by remember { mutableStateOf(false) }
    var titleDialog by remember { mutableIntStateOf(0) }
    var messageDialog by remember { mutableIntStateOf(0) }

    // Mostrar/ocultar diálogos basado en estado local
    if (showLoadingAlert) {
        ShowLoadingAlertDialog()
    }

    if (showMessageAlert) {
        ShowMessageAlertDialog(
            onConfirmation = { showMessageAlert = false },
            dialogTitle = titleDialog,
            dialogText = messageDialog
        )
    }

    Column(...) {
        // Observación del ViewModel mediante callback
        Button(
            onClick = {
                showLoadingAlert = true
                viewModel.login(documentNumber, password) { success, message ->
                    showLoadingAlert = false
                    if (success) {
                        navController.navigate("home")
                    } else {
                        titleDialog = R.string.dialog_error_title
                        messageDialog = message
                        showMessageAlert = true
                    }
                }
            }
        ) { Text(...) }
    }
}
```

**Flujo de datos**:
```
Usuario escribe → remember/mutableStateOf actualiza
             ↓
Usuario presiona botón → lambda de onClick
             ↓
ViewModel.login(params, callback) se ejecuta
             ↓
Callback retorna resultado (success, message)
             ↓
remember/mutableStateOf actualiza UI
```

### 7.3 Manejo de Errores

#### **Patrón Actual: Callbacks con flags booleanos**

```kotlin
// En ViewModel
fun login(..., onResult: (Boolean, Int) -> Unit) {
    if (documentNumber.isBlank() || password.isBlank()) {
        onResult(false, R.string.error_login_failed)  // ← Error
        return
    }
    loginUseCase(documentNumber, password, onResult)
}

// En Firebase Repository
override fun login(..., onResult: (Boolean, Int) -> Unit) {
    dataSource.getUser(documentNumber)
        .addOnSuccessListener { dataUser ->
            val dbPassword = dataUser.child("password").value.toString()
            if (dbPassword == password) {
                onResult(true, 0)  // ← Éxito
            } else {
                onResult(false, R.string.error_login_failed)  // ← Error
            }
        }
        .addOnFailureListener {
            onResult(false, R.string.error_login_failed)  // ← Error
        }
}
```

#### **Cómo se muestran al usuario**:

```kotlin
// En LoginView
viewModel.login(documentNumber, password) { success, message ->
    showLoadingAlert = false
    if (success) {
        navController.navigate("home")
    } else {
        // Mostrar error en AlertDialog
        titleDialog = R.string.dialog_error_title
        messageDialog = message  // ← ID de string del error
        showMessageAlert = true
    }
}
```

**Tipos de errores controlados**:
1. **Credenciales inválidas**: `R.string.error_login_failed`
2. **Campos vacíos**: `R.string.error_login_failed`
3. **Contraseñas no coinciden (registro)**: `R.string.error_passwords_match`
4. **Error en registro**: `R.string.error_register_failed`

**Limitaciones**:
- ❌ No diferencia tipos de error (credenciales vs. conexión)
- ❌ Todo error retorna el mismo mensaje
- ❌ No hay stack trace o logging

### 7.4 Instanciación de ViewModels

#### **Patrón: Predeterminado de Compose**

```kotlin
// En LoginView.kt
@Composable
fun LoginView(
    viewModel: LoginViewModel = viewModel(),  // ← Default parameter
    navController: NavController
) {
    // ...
}

// En RegisterView.kt
@Composable
fun RegisterView(
    viewModel: RegisterViewModel = viewModel(),  // ← Default parameter
    navController: NavController
) {
    // ...
}
```

**Cómo funciona**:
- `viewModel()` usa el Compose ViewModel Provider
- Instancia automáticamente el ViewModel si no existe
- Lo mantiene en memoria durante el ciclo de vida del Composable
- Es inyectable (se puede pasar un ViewModel distinto para testing)

### 7.5 State Hoisting

#### **Implementación**: Parcial/Limitada

**Ejemplo de State Hoisting en LoginView**:
```kotlin
// ❌ NO aplicado: Estado en Composable
var documentNumber by remember { mutableStateOf("") }
var password by remember { mutableStateOf("") }
var showLoadingAlert by remember { mutableStateOf(false) }
var showMessageAlert by remember { mutableStateOf(false) }
var titleDialog by remember { mutableIntStateOf(0) }
var messageDialog by remember { mutableIntStateOf(0) }

// Estado controlado en el Composable (no elevado)
OutlinedTextField(
    value = documentNumber,
    onValueChange = { documentNumber = it }  // ← Cambio local
)
```

**¿Por qué no se applica State Hoisting?**
- El estado del formulario no es compartido con otros Composables
- No hay componentes reutilizables que necesiten estado compartido
- El patrón callback es más simple que elevar estado

**¿Dónde SÍ se aplica (implícitamente)?**
- `AlertDialogs` reciben callbacks: `onConfirmation`
- No manejan su propio estado visible (es controlado externamente)

---

## SECCIÓN ADICIONAL — RECURSOS Y CONFIGURACIÓN

### Strings Disponibles (strings.xml)

```xml
<!-- LOGIN -->
app_name = "MyBank"
login_title = "MyBank"
label_document_number = "Número de Documento"
label_password = "Contraseña"
btn_login = "Iniciar Sesión"
btn_forgot_password = "¿Olvidaste tu contraseña?"
text_no_account = "¿No tienes una cuenta?"
text_register = "Regístrate aquí"
error_login_failed = "Credenciales incorrectas"
text_loading = "Cargando"

<!-- REGISTER -->
register_title = "Crear Cuenta"
label_full_name = "Nombre Completo"
label_confirm_password = "Confirmar Contraseña"
btn_register = "Registrarse"
text_already_have_account = "¿Ya tienes una cuenta?"
text_login_here = "Inicia sesión aquí"
register_loading_title = "Registrando"
dialog_success_title = "Éxito"
dialog_error_title = "Error"
register_success_message = "Cuenta creada correctamente"
btn_accept = "Aceptar"
error_passwords_match = "Las contraseñas no coinciden"
error_register_failed = "Hubo un error al crear la cuenta"
```

### Colores (Theme)

```kotlin
// Light Theme
Purple40 = Color(0xFF6650a4)    // Primary
PurpleGrey40 = Color(0xFF625b71) // Secondary
Pink40 = Color(0xFF7D5260)       // Tertiary

// Dark Theme
Purple80 = Color(0xFFD0BCFF)     // Primary
PurpleGrey80 = Color(0xFFCCC2DC) // Secondary
Pink80 = Color(0xFFEFB8C8)       // Tertiary
```

### Android Configuration

- **Namespace**: com.utp.mybank
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 15)
- **Compile SDK**: 36
- **Java Compatibility**: 11
- **Kotlin Version**: 2.0.21
- **Compose BOM**: 2024.09.00

---

## RESUMEN EJECUTIVO

### ✅ Lo que está implementado:
1. **Arquitectura Clean Architecture** con separación de capas (presentation, domain, data)
2. **Sistema de autenticación completo** (login y registro)
3. **Firebase Realtime Database integration**
4. **UI en Jetpack Compose** con Material Design 3
5. **Navegación basada en rutas**
6. **Validaciones básicas** en campos de entrada
7. **Manejo de alertas** (carga y mensajes)

### ❌ Lo que NO está implementado:
1. **Funcionalidad de transferencias**
2. **Gestión de saldos**
3. **Historial de transacciones**
4. **State management reactivo** (Flow/StateFlow)
5. **Inyección de dependencias centralizada** (Dagger/Hilt)
6. **Encriptación de contraseñas**
7. **Autenticación nativa de Firebase**
8. **Testing (aunque hay archivos de test vacíos)**

### 🎯 Conclusión:
El proyecto es una **aplicación de autenticación básica** sin funcionalidad bancaria real. Está estructurado correctamente según Clean Architecture pero usa patrones antiguos (callbacks) en lugar de flujos reactivos modernos. Es adecuado como **punto de partida** o **proof of concept**, pero requiere expansión significativa para ser una aplicación bancaria completa.

---

**Fin del Análisis**

