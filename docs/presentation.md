# Paquete: presentation

## Responsabilidad
Este paquete contiene toda la capa de presentación de la aplicación. Combina la gestión del estado y las validaciones de la interfaz (ViewModels), la renderización declarativa de las pantallas (Views en Jetpack Compose), los componentes visuales reutilizables y el enrutamiento de la aplicación (Navegación).

## Archivos por funcionalidad

### 1. Autenticación (auth)

#### LoginViewModel.kt
- **Qué es:** ViewModel que gestiona el estado y la lógica del flujo de inicio de sesión.
- **Qué hace:** Valida que el teléfono tenga 10 dígitos y empiece por 3, y que el PIN posea exactamente 4 dígitos. Ejecuta el inicio de sesión y expone estados reactivos de carga, éxito o error mediante `LiveData`. Integra autenticación biométrica con `BiometricHelper` y expone el flag `biometriaActiva` para consultar si el dispositivo tiene biometría habilitada.
- **Interactúa con:** `LoginUseCase`, `BiometricHelper`, `SessionRepository`, `ServiceLocator` y alimenta visualmente a `LoginView.kt`.

#### LoginView.kt
- **Qué es:** Pantalla de inicio de sesión construida en Jetpack Compose.
- **Qué hace:** Renderiza el formulario de acceso separando el contenido puro en `LoginContent` para facilitar la previsualización. Reacciona a los estados de carga y muestra diálogos emergentes si fallan las credenciales. Implementa:
  - **Login inteligente:** Si hay sesión guardada, muestra saludo personalizado y pre-carga el teléfono
  - **Botón huella:** Disponible si el dispositivo tiene biometría y la sesión anterior la tenía activa
  - Validación de formulario y manejo de errores de conexión
- **Interactúa con:** `LoginViewModel`, `AppNavigation`, `BiometricHelper`, `HadesScreen` y componentes UI.

#### RegisterViewModel.kt
- **Qué es:** ViewModel para el flujo de registro de nuevos usuarios.
- **Qué hace:** Valida todos los campos del formulario (nombres, longitud de documento, consistencia telefónica y PIN). Coordina la creación de la entidad `AppUser` y gestiona las respuestas de éxito o cuenta duplicada.
- **Interactúa con:** `RegisterUseCase`, `ServiceLocator` y alimenta a `RegisterView.kt`.

#### RegisterView.kt
- **Qué es:** Pantalla de registro de usuarios.
- **Qué hace:** Dibuja el formulario de alta y observa de forma reactiva el estado del ViewModel para desplegar animaciones de carga, bloqueos de pantalla o diálogos de éxito/error.
- **Interactúa con:** `RegisterViewModel`, `AppNavigation` y el paquete de componentes.

### 2. Tablero Principal (home)

#### HomeViewModel.kt
- **Qué es:** ViewModel del Dashboard principal de la billetera.
- **Qué hace:** Carga y almacena el balance del usuario, su historial de movimientos y propaga el flag `autenticadoConHuella` desde la operación anterior de login. Ordena las transacciones cronológicamente de forma descendente. Expone métodos para:
  - Generar códigos de retiro en cajero
  - Limpiar códigos de retiro expirados
  - Cargar saldo e historial del usuario
- **Interactúa con:** `GetWalletDataUseCase`, `GenerateWithdrawalCodeUseCase`, `ServiceLocator` y alimenta a `HomeView.kt`.

#### HomeView.kt
- **Qué es:** Vista principal del cuadro de mando.
- **Qué hace:** Ensambla las tarjetas de saldo interactivo, el resumen financiero (gráfico donut de ingresos/egresos con `HadesFinancialChart`) y la lista filtrable de transacciones (`LazyColumn`). Implementa:
  - **HadesSpeedDial:** Menú flotante con acciones rápidas (Transferir, Retirar en Cajero, QR)
  - **Filtros:** Chips de filtro (`HadesFilterChipRow`) para ver todas las transacciones o filtrar por tipo
  - **Acceso a perfil:** Botón de usuario que navega a `ProfileView`
  - **Sheet QR:** Bottom sheet (`QrSheet`) con opciones Escanear / Generar QR (próximamente)
- **Interactúa con:** `HomeViewModel`, `AppNavigation`, `BiometricHelper`, `WithdrawCodeDialog`, `HadesScreen`, componentes visuales.

### 3. Transferencias (transfer)

#### TransferViewModel.kt
- **Qué es:** ViewModel encargado de gobernar las transferencias de dinero.
- **Qué hace:** Pre-carga el balance del emisor y valida estrictamente las reglas antes de enviar fondos (monto positivo, teléfono destino válido de 10 dígitos, PIN de 4 dígitos exactos, prohibición de auto-transferencia). Expone el flag `biometriaActiva` y `autenticadoConHuella` para controlar la validación inteligente de PIN. Actualiza el balance local tras un envío exitoso.
- **Interactúa con:** `TransferUseCase`, `GetWalletDataUseCase`, `ServiceLocator` y alimenta a `TransferView.kt`.

#### TransferView.kt
- **Qué es:** Pantalla del formulario de envíos de dinero.
- **Qué hace:** Permite ingresar los datos del destinatario y el monto a transferir. Implementa formulario inteligente:
  - Si `biometriaActiva = true` → el campo PIN se oculta automáticamente
  - Si biometría está activa → botón de huella confirma la transferencia
  - Si biometría no está activa → botón de PIN normal confirma
  - Advertencias visuales en naranja si el monto supera el saldo disponible
  - Sheet de confirmación con validación final
- **Interactúa con:** `TransferViewModel`, `AppNavigation`, `BiometricHelper`, `HadesScreen` y componentes visuales.

### 4. Navegación (navigation)

#### AppNavigation.kt
- **Qué es:** Enrutador central estático de la aplicación.
- **Qué hace:** Define el grafo de navegación utilizando `NavHost`. Establece `login` como el destino inicial y gestiona de forma segura la extracción y paso de parámetros hacia las rutas dinámicas:
  - `login` — inicio de sesión inicial
  - `register` — registro de nuevos usuarios
  - `home/{phoneNumber}` — dashboard principal con historial y saldo
  - `transfer/{senderPhone}` — formulario de transferencia
  - `profile/{phoneNumber}` — perfil de usuario (nuevo)
  - `notifications/{phoneNumber}` — notificaciones (nuevo)
- **Interactúa con:** `LoginView`, `RegisterView`, `HomeView`, `TransferView`, `ProfileView` y `NotificationsView`.

### 5. Componentes UI (components)

#### Componentes atómicos y contenedores
- **HadesButton, HadesTextField, HadesBackground, HadesCardBox, etc.**
  - **Qué es:** Biblioteca de elementos visuales reutilizables con estilo propio.
  - **Qué hace:** Encapsulan elementos base de Jetpack Compose para inyectarles de forma centralizada el sistema de diseño del proyecto (fondos con gradientes, bordes iluminados, botones con estado de carga integrado y campos de texto personalizados).
  - **Interactúa con:** Todas las pantallas del paquete `presentation`.

#### Componentes contenedor y layout
- **HadesScreen.kt**
  - **Qué es:** Contenedor base reutilizable para todas las pantallas.
  - **Qué hace:** Combina `HadesBackground` con `safeDrawingPadding` para garantizar que ninguna pantalla se solape con las barras del sistema (status bar arriba, navigation bar abajo). Proporciona un `Box` que actúa como contenedor con respeto automático de insets.
  - **Interactúa con:** Todas las vistas principales: `LoginView`, `RegisterView`, `HomeView`, `TransferView`, `ProfileView`, `NotificationsView`.

#### Componentes de visualización de datos financieros
- **HadesBalanceText, HadesSummaryRow**
  - **Qué es:** Componentes especializados en la presentación del saldo e información resumida.
  - **Qué hace:** Exponen balance con ocultamiento dinámico, resumen de ingresos/egresos y tarjetas de información visual.
  - **Interactúa con:** Principalmente `HomeView`.

- **HadesFinancialChart.kt**
  - **Qué es:** Componente de gráfico donut animado.
  - **Qué hace:** Visualiza la distribución de ingresos vs egresos en un gráfico circular con proporción en porcentajes. Utiliza `Canvas` para dibujar arcos y etiquetas numéricas con formato de moneda.
  - **Interactúa con:** `HomeView` para mostrar el resumen financiero.

#### Componentes de entrada y filtrado
- **HadesPinInput.kt**
  - **Qué es:** Campo visual de PIN con 4 círculos al estilo aplicaciones bancarias.
  - **Qué hace:** Renderiza 4 indicadores visuales vacíos que se llenan a medida que el usuario ingresa dígitos. Cualquier toque en el componente abre el teclado numerico. Soporta hasta 4 dígitos únicamente.
  - **Interactúa con:** `LoginView`, `TransferView`, `WithdrawCodeDialog`, `PinRecoveryComponents`.

- **HadesFilterChipRow.kt**
  - **Qué es:** Fila reutilizable de chips de filtro.
  - **Qué hace:** Muestra opciones de categoría (todas, depósito, retiro, transferencia) como chips interactivos. El chip seleccionado se resalta y filtra el historial de transacciones en tiempo real.
  - **Interactúa con:** `HomeView` para filtrar el historial.

#### Componentes interactivos y diálogos
- **HadesSpeedDial.kt**
  - **Qué es:** Menú flotante con múltiples acciones.
  - **Qué hace:** Renderiza un botón FAB con animación rotatoria que expande/contrae un menú con opciones (Transferir, Retirar en Cajero, QR). Cada opción es clickeable y ejecuta un callback. Soporta estados de habilitación por item.
  - **Interactúa con:** `HomeView` como menú de acciones rápidas.

- **WithdrawCodeDialog.kt**
  - **Qué es:** Diálogo de dos pasos para generar códigos de retiro en cajero.
  - **Qué hace:** 
    - Paso 1: Ingresa monto máximo de retiro
    - Si `biometriaActiva = true` → oculta campo PIN y solo requiere autenticación biométrica
    - Si `biometriaActiva = false` → solicita PIN de 4 dígitos para validar
    - Paso 2: Muestra el código generado con contador de expiración y botón copiar
  - **Interactúa con:** `HomeViewModel`, `BiometricHelper`.

- **QrSheet.kt**
  - **Qué es:** Bottom sheet de selección de acciones QR.
  - **Qué hace:** Muestra dos opciones decorativas (Escanear QR, Generar QR) con distintivo "Próximamente" indicando que la lógica está pendiente. La UI está completa pero las funcionalidades aún no integran CameraX ni generación de códigos.
  - **Interactúa con:** `HomeView` cuando el usuario selecciona el botón QR del SpeedDial.

#### Componente de flujo de recuperación de PIN
- **PinRecoveryComponents.kt**
  - **Qué es:** Flujo reutilizable de verificación de identidad con 4 pasos encadenados.
  - **Qué hace:**
    1. **VerifyIdentityDialog:** Ingresa teléfono + cédula, genera código en Firebase
    2. **CodeRevealDialog:** Muestra código de 6 dígitos con botón copiar
    3. **ConfirmCodeDialog:** Usuario ingresa el código para validar
    4. **ResetPinStepDialog:** Opción para ingresar nuevo PIN (solo si `showResetStep = true`)
  - Puede usarse para recuperación de PIN (flujo completo) o verificación de retiro (sin paso 4).
  - **Interactúa con:** `ProfileViewModel` y potencialmente `HomeViewModel` para confirmaciones de retiro.

#### Componentes de utilidades visuales
- **AlertDialogs.kt**
  - **Qué es:** Familia de diálogos reutilizables.
  - **Qué hace:** Proporciona `ShowLoadingAlertDialog`, diálogos de confirmación y alertas de error con animaciones y acciones clickeables.
  - **Interactúa con:** Múltiples ViewModels para feedback síncrono y asíncrono.

### 6. Perfil de usuario (profile)

#### ProfileViewModel.kt
- **Qué es:** ViewModel que gestiona la pantalla de perfil del usuario.
- **Qué hace:** Carga los datos del perfil, maneja cambios de PIN y apodo, controla el flujo de recuperación de PIN con códigos de verificación, expone el contador de notificaciones no leídas, y gestiona el flag `biometriaActiva` para mostrar/ocultar el toggle de autenticación biométrica.
- **Interactúa con:** `GetUserProfileUseCase`, `UpdateUserPinUseCase`, `UpdateUserNicknameUseCase`, `GenerateVerificationCodeUseCase`, `ValidateVerificationCodeUseCase`, `SessionRepository` y alimenta a `ProfileView.kt`.

#### ProfileView.kt
- **Qué es:** Pantalla de gestión del perfil y configuración del usuario.
- **Qué hace:** Muestra la información del usuario (nombre, teléfono, apodo), botones para cambiar PIN y apodo, un toggle para activar/desactivar biometría (si el dispositivo la soporta), acceso a notificaciones y un flujo de recuperación de PIN de 4 pasos. Integra `BiometricHelper` para solicitar confirmación biométrica antes de activar/desactivar la función.
- **Interactúa con:** `ProfileViewModel`, `AppNavigation`, `BiometricHelper`, `PinRecoveryComponents`, `HadesScreen` y componentes visuales.

### 7. Notificaciones (notifications)

#### NotificationsViewModel.kt
- **Qué es:** ViewModel que gestiona el listado de notificaciones del usuario.
- **Qué hace:** Carga todas las notificaciones asociadas al teléfono del usuario, mantiene un contador de no leídas y maneja la marcación de notificaciones como leídas.
- **Interactúa con:** Datasources de notificaciones y alimenta a `NotificationsView.kt`.

#### NotificationsView.kt
- **Qué es:** Pantalla de visualización del historial de notificaciones.
- **Qué hace:** Renderiza un `LazyColumn` con listado de notificaciones, cada una mostrando ícono, tipo, monto (si aplica), fecha y estado (leída/no leída). Implementa pull-refresh para actualizar manualmente. Con interfaz limpia y contador de no leídas en el encabezado.
- **Interactúa con:** `NotificationsViewModel`, `AppNavigation`, `HadesScreen` y componentes visuales.

### 8. Utilidades (utils)

#### TextUtils.kt
- **Qué es:** Archivo de utilidades para el formateo de datos.
- **Qué hace:** Provee funciones puras de apoyo visual: extrae iniciales de nombres para avatares (`getInitials`), traduce identificadores de transacciones del servidor al español (`translateTransactionType`), formatea fechas ISO a texto amigable (`formatTimestamp`), y formatea montos con símbolo local (`formatCurrency`).
- **Interactúa con:** `HomeView.kt`, `TransferView.kt`, `NotificationsView.kt` y `ProfileView.kt`.

#### BiometricHelper.kt
- **Qué es:** Núcleo centralizado de autenticación biométrica.
- **Qué hace:** 
  - `isDisponible(context)` — verifica si el dispositivo tiene biometría registrada y disponible
  - `mostrar(activity, titulo, subtitulo, onExito, onError)` — muestra el diálogo nativo del SO con animación de escaneo
  - Encapsula toda la complejidad de `androidx.biometric.BiometricPrompt`
- **Requiere:** `FragmentActivity` (la `MainActivity` extiende `FragmentActivity` para compatibilidad)
- **Interactúa con:** `LoginView`, `TransferView`, `ProfileView`, `WithdrawCodeDialog` y cualquier ViewModel que necesite autenticación biométrica.

---
