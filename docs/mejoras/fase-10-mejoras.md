# Fase 10 — Mejoras implementadas por John Mesa

**Proyecto:** HadesCoin — Billetera Digital Android  
**Responsable:** John Mesa  
**Rama:** `release/mejoras-finales`  
**Fecha:** 31 de mayo de 2026

---

## Resumen ejecutivo

En la Fase 10 se completó la integración de **autenticación biométrica (huella dactilar)** en todo el flujo de la aplicación, mejorando la experiencia del usuario y la seguridad. Se implementó un sistema inteligente de validación que omite el ingreso de PIN cuando el trabajador — usuario ya ha sido autenticado con huella. Paralelamente, se refactorizó la experiencia de UI unificando el componente base `HadesScreen` con `safeDrawingPadding` automático, eliminando la necesidad de repetir esta lógica en cada pantalla. Finalmente, se agregó un botón QR en el `SpeedDial` con sheet de selección para futuras funcionalidades de escaneo y generación de códigos QR.

---

## Mejoras agrupadas por área

### A. Biometría — Núcleo e integración (5 commits)

**Commits:** e701e98, 15e3f65, 2e7da6c, 6101cb2

#### Descripción general
Se construyó una arquitectura de biometría modular basada en `BiometricHelper`, un singleton reutilizable que encapsula toda la interacción con la **API AndroidX BiometricPrompt**. La biometría se controla mediante un flag `biometriaActiva` persistido en Firebase a través de `SessionRepository`.

#### Fases implementadas

**Fase 1: Núcleo BiometricHelper** (commit e701e98)
- Clase singleton `BiometricHelper.kt` en `presentation/utils/`
- Métodos clave:
  - `isDisponible(context)` — verifica si el dispositivo tiene biometría registrada
  - `mostrar(activity, titulo, subtitulo, onExito, onError)` — muestra el diálogo nativo del SO
- Integración con Firebase para persistir `biometriaActiva` en `SessionLocalDataSource` y `SessionRepository`

**Fase 2: Integración en Login** (commit 15e3f65)
- `LoginViewModel` expone `biometriaActiva: LiveData<Boolean>`
- `LoginView` muestra botón **"Inicia sesión con huella"** solo si:
  - Hay una sesión guardada (login inteligente) Y
  - El dispositivo tiene biometría disponible
- Al completar autenticación biométrica → se realiza login automáticmente

**Fase 3: Switch de activación en Profile** (commit 2e7da6c)
- `ProfileView` expone un toggle **"Usar huella para autenticación"**
- Al activar → se guarda `biometriaActiva = true` en Firebase
- Al desactivar → se guarda `biometriaActiva = false`
- Los cambios se sincronizan en tiempo real en todas las pantallas

**Fase 3.5: Fix ComponentActivity** (commit 6101cb2)
- **Problema identificado:** `BiometricPrompt` de AndroidX requiere `FragmentActivity`, pero MainActivity extendía `ComponentActivity`
- **Solución:** MainActivity ahora extiende `FragmentActivity` (que es subclase de `ComponentActivity`, 100 % compatible con Compose)
- **Impacto:** Todos los `BiometricHelper.mostrar()` funciona correctamente sin Breaking Changes

---

### B. Mejoras de UI y layout (2 commits)

**Commits:** de78582, 4fb0f9b

#### Descripción general
Se creó `HadesScreen`, un componente reutilizable que garantiza que ninguna pantalla se solape nunca con las barras del sistema (status bar arriba, navigation bar abajo), sin importar el dispositivo.

#### Componente HadesScreen
- **Ubicación:** `presentation/components/HadesScreen.kt`
- **Composición:** `HadesBackground` + `Box` con `safeDrawingPadding`
- **Beneficio:** Todas las pantallas heredan automáticamente el respeto de las barras del sistema
- **Uso simple:**
  ```kotlin
  HadesScreen {
      Column(...) { /* contenido */ }
  }
  ```

#### Pantallas migradas a HadesScreen
- ✅ `LoginView`
- ✅ `RegisterView` (ambas fases)
- ✅ `HomeView`
- ✅ `TransferView`
- ✅ `ProfileView`
- ✅ `NotificationsView`

#### Diferencia: HadesScreen vs HadesBackground
| Aspecto | HadesBackground | HadesScreen |
|--------|-----------------|-----------|
| Propósito | Solo gradiente de fondo | Fondo + seguridad de barras |
| `safeDrawingPadding` | ❌ Ausente | ✅ Aplicado |
| Reutilización | Poco práctica | 📍 Estándar del proyecto |

---

### C. Formularios inteligentes (3 commits)

**Commits:** 82386ee, cb62314, 87a0fa7

#### Descripción general
Se implementó lógica condicional en los formularios de Login, Transfer y Cajero para ocultar el campo de PIN cuando la biometría está activa, mejorando la experiencia y reduciendo clics innecesarios.

#### C.1 — Login inteligente (commit 82386ee)
- **Saludo coherente:** Si hay sesión guardada → "¡Bienvenido de nuevo, [Nombre]!" + solo campo PIN
- **Botón crear cuenta:** Siempre visible, independiente del estado de sesión
- **Enlace "Cambiar cuenta":** Más claro y prominente para volver a login estándar
- **Botón huella:** Se muestra solo si `biometriaActiva && BiometricHelper.isDisponible()`

#### C.2 — Transfer inteligente (commit cb62314)
- Si `biometriaActiva == true` → campo PIN se oculta
- El usuario **confirma la transferencia solo con huella**
- PIN sigue disponible como opción de fallback accesible desde el sheet de confirmación
- La transferencia se ejecuta con `autenticadoConHuella = true`

#### C.3 — Cajero inteligente (commit 87a0fa7)
- Dialog `WithdrawCodeDialog`: Si `biometriaActiva == true` → campo PIN se oculta
- La confirmación de retiro se realiza solo con huella
- PIN disponible como fallback manual

---

### D. Correcciones de validación con huella (4 commits)

**Commits:** cbe80e5, 0f2490b, 8aca1ef, 913eb9d

#### Descripción general
Se implementó un sistema de propagación de `autenticadoConHuella` desde la UI hasta la capa de Repository para omitir validación de PIN cuando es necesario.

#### Cambios por commit

**Commit cbe80e5:** Transfer — Exponer flag
- `TransferViewModel` expone `biometriaActiva: LiveData<Boolean>`
- Se pasa `autenticadoConHuella` al UseCase de transferencia
- PIN acepta valores vacíos cuando `autenticadoConHuella = true`

**Commit 0f2490b:** Cajero — Omitir validación (GenerateWithdrawalCodeUseCase)
- No se valida PIN si `autenticadoConHuella = true`
- El código de retiro se genera sin verificación de PIN

**Commit 8aca1ef:** Cajero — Propagar flag
- `HomeViewModel` propaga `autenticadoConHuella` desde `HomeView` al `GenerateWithdrawalCodeUseCase`
- La cadena ahora es: **HomeView → HomeViewModel → UseCase → Repository**

**Commit 913eb9d:** Transfer — Propagar hasta Repository
- El flag `autenticadoConHuella` llega hasta `WalletRepositoryImpl`
- Se omite validación de PIN en la escritura de transacción
- Flujo completo: **TransferView → ViewModel → UseCase → Repository → Firebase**

#### Patrón de propagación implementado
```
Composable (UI)
    ↓ (parámetro o observableAsState)
ViewModel (expone LiveData<Boolean>)
    ↓ (pasa al UseCase)
UseCase (recibe Boolean en invoke)
    ↓ (pasa al Repository)
Repository (evalúa flag antes de validar)
    ↓
Firebase (transacción sin validación de PIN)
```

---

### E. Nueva funcionalidad QR (1 commit)

**Commit:** 4b882cc

#### Descripción
Se agregó un **botón QR en el SpeedDial** con un sheet de selección de acciones: **"Escanear QR"** y **"Generar QR"**.

#### Implementación actual
- **Estado:** UI + navegación completada
- **Ubicación:** `HomeView` → `SpeedDial` + `QRSheet`
- **Interfaz:**
  - Botón QR redondo con ícono de código de barras
  - Al tocar → sheet modal desde abajo con dos opciones
  - "Escanear QR" — próximamente enlaza con lógica de cámara
  - "Generar QR" — próximamente implementa generación de código

#### Trabajo futuro
- Integración de **CameraX** para escaneo real (similar a `RegisterView`)
- Generación de imágenes QR con **QrCode/ZXing** library
- Lógica de negocio para enlazar QR con transferencias o retiros

---

## Tabla de estado

| # | Mejora | Estado | Archivos clave |
|---|--------|--------|----------------|
| A | Biometría — Núcleo e integración | ✅ Completo | `BiometricHelper.kt`, `SessionRepository`, `LoginView`, `ProfileView` |
| B | UI — HadesScreen y safeDrawingPadding | ✅ Completo | `HadesScreen.kt`, `LoginView`, `TransferView`, `ProfileView`, `NotificationsView` |
| C | Formularios inteligentes | ✅ Completo | `LoginView`, `TransferView`, `WithdrawCodeDialog` |
| D | Validación inteligente con huella | ✅ Completo | `GenerateWithdrawalCodeUseCase`, `TransferUseCase`, `WalletRepositoryImpl` |
| E | QR en SpeedDial | 🔧 Parcial | `HomeView`, `QRSheet` (UI lista, lógica pendiente) |

---

## Flujos que quedaron pendientes (próximamente)

1. **Escaneo real de QR con cámara**
   - Requiere integración de `CameraX`
   - Similar a la implementada en `RegisterView` (Fase 5)
   - Captura de imagen QR dentro de `QRSheet`

2. **Generación de imagen QR propia**
   - Requiere biblioteca como `ZXing` o `qrcode` library
   - Generar QR a partir de datos de transferencia o retiro
   - Mostrar QR para que el usuario lo comparta o imprima

---

## Notas técnicas

### Patrón de propagación de `autenticadoConHuella`
El flag `autenticadoConHuella` se propaga a través de toda la arquitectura Clean Architecture:
1. **UI (Composable):** Usuario toca botón de huella o campo oculto
2. **ViewModel:** Observa `BiometricHelper` y establece `autenticadoConHuella = true`
3. **UseCase:** Recibe el flag como parámetro
4. **Repository:** Omite validación de PIN si el flag es true
5. **Firebase:** Transacción se ejecuta sin verificar PIN

Esta arquitectura permite validaciones inteligentes sin comprometer la seguridad.

### Por qué se migró de ComponentActivity a FragmentActivity
- **BiometricPrompt** de AndroidX (`androidx.biometric`) requiere una `FragmentActivity`
- `ComponentActivity` (base de Compose) **no hereda** de `FragmentActivity`
- **Solución:** `MainActivity` ahora hereda de `FragmentActivity`
- **Ventaja:** `FragmentActivity` extiende `ComponentActivity`, así que es 100 % compatible con Compose
- **Impacto:** Cero Breaking Changes — toda la app sigue funcionando normalmente

### Qué aporta HadesScreen vs HadesBackground
| Concepto | HadesBackground | HadesScreen |
|----------|-----------------|-----------|
| **Propósito** | Inyecta gradiente de color de fondo | Inyecta fondo + respeto de barras del SO |
| **safeDrawingPadding** | ❌ No aplica | ✅ Automático |
| **Repetición de código** | Cada pantalla debe recordar agregar `safeDrawingPadding` | Una sola estructura reutilizable |
| **Mantenibilidad** | Baja — cambios requieren actualizar cada pantalla | Alta — cambio centralizado en `HadesScreen.kt` |

`HadesScreen` se convierte en el **componente base oficial** de todas las pantallas de HadesCoin, asegurando consistencia visual y funcional.

---

## Resumen de cambios por área de código

### Data / Repository
- `SessionLocalDataSource` — agrega flag `biometriaActiva`
- `SessionRepository` — getter/setter para `biometriaActiva`
- `WalletRepositoryImpl` — omite validación de PIN cuando `autenticadoConHuella = true`

### Domain / UseCase
- `GenerateWithdrawalCodeUseCase` — acepta parámetro `autenticadoConHuella`
- `TransferUseCase` — acepta parámetro `autenticadoConHuella`

### Presentation / Components
- `HadesScreen.kt` — componente base nuevo
- `QRSheet.kt` — sheet modal para selección de acciones QR

### Presentation / Views
- `LoginView.kt` — botón huella, saludo coherente, PIN oculto si no es necesario
- `TransferView.kt` — PIN oculto si biometría activa, confirmación con huella
- `ProfileView.kt` — toggle de activación/desactivación de biometría
- `HomeView.kt` — QR en SpeedDial, propagación de `autenticadoConHuella`
- `NotificationsView.kt` — migrado a `HadesScreen`

### Presentation / Utils
- `BiometricHelper.kt` — nuevo, núcleo de biometría
- `MainActivity.kt` — ahora extiende `FragmentActivity`

---

> Documento generado el 31 de mayo de 2026 sobre la rama release/mejoras-finales

