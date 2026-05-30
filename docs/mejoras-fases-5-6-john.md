# 📋 Mejoras Implementadas — Fases 5 y 6
**Proyecto:** HadesCoin — Billetera Digital Android  
**Responsable:** John  
**Rama:** `release/mejoras-finales`  
**Fecha:** Mayo 2026

---

## Resumen de mejoras implementadas

| # | Mejora | Fase | Estado |
|---|--------|------|--------|
| #9 | Cámara — escaneo de cédula en registro | 5 | ✅ Completado |
| #5 | Persistencia local + login inteligente | 5 | ✅ Completado |
| #7 + #11 | Retiro con código temporal + seguridad en cajero | 6 | ✅ Completado |
| #6 | Depósito desde cajero | 6 | ✅ Completado |
| — | Mejoras de UI e íconos en HomeView | 6 | ✅ Completado |

---

## Mejora #9 — Cámara: escaneo de cédula en registro

**Ubicación:** `app/src/main/java/com/example/hadescoin/presentation/register/`

### Descripción
Se integró **CameraX** (Jetpack) en el flujo de registro para simular el onboarding real de billeteras digitales como Nequi y Daviplata. El usuario debe capturar su documento de identidad antes de completar el registro.

### Flujo implementado
1. El usuario llega al paso de verificación de identidad durante el registro
2. La app solicita permiso de cámara en tiempo de ejecución
3. Se activa la cámara con vista previa en tiempo real (`PreviewView`)
4. El usuario encuadra su cédula y toca el botón de captura
5. La app muestra confirmación: **"Documento capturado ✅"**
6. El registro continúa normalmente hacia el siguiente paso

### Archivos modificados / creados
- `CameraView.kt` — pantalla de captura con CameraX y botón de disparo
- `RegisterViewModel.kt` — manejo del estado de captura de documento
- `AndroidManifest.xml` — permiso `CAMERA` declarado
- `app/build.gradle.kts` — dependencias CameraX `1.5.0-alpha06`

### Dependencias agregadas
```kotlin
val cameraxVersion = "1.5.0-alpha06"
implementation("androidx.camera:camera-core:$cameraxVersion")
implementation("androidx.camera:camera-camera2:$cameraxVersion")
implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
implementation("androidx.camera:camera-view:$cameraxVersion")
implementation("androidx.camera:camera-extensions:$cameraxVersion")
```

### Notas técnicas
- No se implementó OCR real. La captura visual es suficiente para demostración académica.
- El permiso de cámara se solicita en tiempo de ejecución con `rememberLauncherForActivityResult`.
- Compatible con dispositivo físico y emulador (emulador requiere cámara virtual activada).

---

## Mejora #5 — Persistencia local + Login inteligente

**Ubicación:** `app/src/main/java/com/example/hadescoin/presentation/login/`

### Descripción
Se implementó **DataStore (Jetpack Preferences)** para recordar al último usuario autenticado. La pantalla de login adapta su comportamiento según si hay o no una sesión guardada localmente.

### Comportamiento sin sesión guardada
- Pantalla de login estándar: teléfono + PIN
- Al autenticarse exitosamente → se guardan nombre y teléfono localmente
- La próxima apertura muestra el login inteligente

### Comportamiento con sesión guardada (login inteligente)
- Saludo personalizado: **"¡Bienvenido de nuevo, [Nombre]!"**
- El número de teléfono viene precargado — el usuario no lo escribe
- Solo se ingresa el PIN para autenticar
- Botón secundario **"Iniciar sesión como otro usuario"** → vuelve al login estándar y limpia los datos locales

### Datos guardados localmente
| Dato | Guardado | Justificación |
|---|---|---|
| Nombre completo | ✅ Sí | Para el saludo personalizado |
| Número de teléfono | ✅ Sí | Para precargar el campo |
| PIN | ❌ Nunca | Seguridad — solo se valida contra Firebase |

### Archivos modificados / creados
- `UserPreferencesRepository.kt` — lectura y escritura en DataStore
- `LoginView.kt` — lógica de UI condicional (login estándar vs inteligente)
- `LoginViewModel.kt` — estado reactivo de sesión local

### Dependencias agregadas
```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

---

## Mejora #7 + #11 — Retiro con código temporal + Seguridad en cajero

**Ubicación (app principal):** `presentation/home/`, `presentation/components/`  
**Ubicación (cajero):** `HadesCoin-Cajero` — módulo separado

### Descripción
Se implementó el flujo completo de retiro en cajero usando el **Modo A (estilo Daviplata)**: el usuario genera un código temporal con un monto máximo desde la app, va al cajero físico e ingresa su teléfono + código + monto a retirar.

### Flujo — App principal (generación del código)
1. Usuario abre el SpeedDial en `HomeView` → toca **"Retirar en Cajero"**
2. Se abre `WithdrawCodeDialog` — modal con dos campos: **monto máximo** y **PIN**
3. El PIN se valida contra Firebase antes de generar el código
4. Se genera un código temporal de **6 dígitos alfanuméricos** aleatorios
5. El código se guarda en Firebase bajo el nodo del usuario con:
   - `code` — el código generado
   - `maxAmount` — monto máximo autorizado
   - `expiresAt` — timestamp de expiración (10 minutos)
   - `used` — booleano, `false` al crear
6. El código y el temporizador de cuenta regresiva se muestran en pantalla
7. Al expirar o ser usado → el código desaparece automáticamente

### Flujo — Cajero (validación y descuento)
1. Operador ingresa teléfono del usuario + código + monto a retirar
2. El cajero consulta Firebase para verificar:
   - El código existe y no ha expirado (`expiresAt > now`)
   - El código no ha sido usado (`used == false`)
   - El monto solicitado no supera `maxAmount`
   - El saldo del usuario es suficiente
3. Si todo es válido → Firebase descuenta el saldo y marca `used = true`
4. La transacción queda registrada en el historial con `type: "WITHDRAWAL_COMPLETED"` y `source: "ATM"`

### Seguridad implementada (#11)
- **PIN obligatorio** para generar el código desde la app
- **Expiración automática** de 10 minutos
- **Un solo uso** — el código se invalida tras ser utilizado
- **Tope de monto** — el cajero no puede retirar más del `maxAmount` autorizado
- **Bloqueo temporal** tras 3 intentos fallidos de código en el cajero

### Archivos modificados / creados
- `WithdrawCodeDialog.kt` — componente modal de generación de código
- `HomeViewModel.kt` — función `generarCodigoRetiro()` y `clearCodigoRetiro()`
- `WalletRepository.kt` — escritura y lectura del nodo de retiro en Firebase
- `HomeView.kt` — integración del diálogo desde el SpeedDial

### Estructura en Firebase Realtime Database
```
users/
  {phoneNumber}/
    withdrawCode/
      code: "A3F7K2"
      maxAmount: 150000
      expiresAt: 1748642400000
      used: false
```

---

## Mejora #6 — Depósito desde cajero

**Ubicación:** `HadesCoin-Cajero` — módulo separado

### Descripción
Se habilitó en la app del cajero el flujo para acreditar saldo a un usuario de HadesCoin desde el operador del cajero físico.

### Flujo implementado
1. Operador del cajero ingresa el número de teléfono del usuario
2. La app consulta Firebase para confirmar que el usuario existe
3. Operador ingresa el monto a depositar
4. Confirma la operación con su PIN de operador
5. Firebase acredita el saldo al usuario en tiempo real:
   - Se incrementa `balance` del usuario
   - Se registra la transacción con `type: "DEPOSIT"` y `source: "ATM"`
6. La app principal del usuario recibe el cambio en tiempo real (Firebase listener activo)

### Archivos modificados / creados (cajero)
- `DepositView.kt` — pantalla del operador para ingresar teléfono y monto
- `CajeroViewModel.kt` — lógica de depósito y validación de usuario
- `CajeroRepository.kt` — escritura en Firebase con transacción atómica

### Estructura de transacción en Firebase
```
transactions/
  {transactionId}/
    fromPhone: "CAJERO"
    toPhone: "3001234567"
    amount: 200000
    type: "DEPOSIT"
    source: "ATM"
    direction: "IN"
    timestamp: "2026-05-30T14:00:00Z"
```

---

## Mejoras de UI — Íconos personalizados en HomeView

**Ubicación:** `presentation/utils/HadesIcons.kt`, `presentation/home/HomeView.kt`

### Descripción
Se reemplazaron los íconos genéricos rellenos de Material Icons por íconos de trazo fino personalizados, embebidos directamente como `ImageVector` nativos de Compose. No se requiere ninguna dependencia externa adicional.

### Íconos creados (`HadesIcons.kt`)
| Objeto | Descripción visual | Uso en la app |
|---|---|---|
| `HadesIcons.ArrowDownToLine` | Flecha ↓ con línea base horizontal | Depósito en historial y SpeedDial |
| `HadesIcons.ArrowUpFromLine` | Flecha ↑ con línea base horizontal | Retiro en historial y SpeedDial |
| `HadesIcons.Landmark` | Edificio con columnas y techo triangular | Botón "Retirar en Cajero" en SpeedDial |

### Criterio de color aplicado
- **Cyan (`HadesCyan`)** — operaciones de entrada (depósito, transferencia recibida)
- **Naranja (`HadesOrange`)** — operaciones de salida (retiro, transferencia enviada)
- **Púrpura (`HadesPurple`)** — pago con tarjeta (deshabilitado, próximamente)

### Archivos modificados / creados
- `HadesIcons.kt` — nuevo archivo con los 3 vectores embebidos
- `HomeView.kt` — función `txIcon()` actualizada, SpeedDial actualizado

---

## Notas finales para el equipo

- El mecanismo de código temporal implementado en **#7** es **reutilizable** para la mejora **#12 (Recuperación de PIN)**. La lógica de generación, expiración y validación está encapsulada en `WalletRepository` y puede invocarse desde cualquier ViewModel.
- El campo `source: "ATM"` en las transacciones permite a la mejora **#8 (Notificaciones)** identificar exactamente qué tipo de evento disparar.
- Los íconos de `HadesIcons` están implementados con `by lazy` — se instancian una sola vez y se reutilizan sin costo de memoria.
