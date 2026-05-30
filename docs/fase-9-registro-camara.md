# Fase 9 — Flujo de Registro con Verificación de Cédula por Cámara

## Resumen

Esta fase implementó el flujo completo de registro de usuario en 4 pasos, integrando captura de cámara (CameraX) para verificar la identidad del usuario mediante fotografía de su cédula por ambos lados. También se resolvió la incompatibilidad de dependencias entre CameraX y AGP 9.x, y se estandarizó el diseño visual en todos los pasos.

---

## Contexto del stack

| Herramienta | Versión |
|---|---|
| Android Gradle Plugin (AGP) | 9.2.0 |
| Kotlin | 2.2.10 |
| CameraX (antes) | 1.4.2 ❌ incompatible |
| CameraX (después) | 1.5.0-alpha06 ✅ |
| Jetpack Compose BOM | última estable |
| Firebase Realtime Database | BOM 34.12.0 |

---

## Problema 1: Incompatibilidad CameraX con AGP 9.x

### Síntomas
Todos los símbolos de CameraX aparecían como `Unresolved reference` en Android Studio:
- `Unresolved reference 'camera'` (líneas 4–9)
- `Unresolved reference 'ImageCapture'`, `'PreviewView'`, `'ProcessCameraProvider'`, `'Preview'`
- `Unresolved reference 'surfaceProvider'`, `'unbindAll'`, `'bindToLifecycle'`, `'CameraSelector'`

### Causa
CameraX `1.4.2` no declara compatibilidad con AGP `9.x`. El compilador no resolvía ningún símbolo del namespace `androidx.camera.*`.

### Solución
Actualizar todas las dependencias de CameraX en `app/build.gradle.kts`:

```kotlin
// ❌ Antes
val cameraxVersion = "1.4.2"

// ✅ Después
val cameraxVersion = "1.5.0-alpha06"
```

Dependencias afectadas:
- `camera-core`
- `camera-camera2`
- `camera-lifecycle`
- `camera-view`
- `camera-extensions`

---

## Problema 2: Error de inferencia de tipos en `mutableStateOf`

### Síntoma
```
Null cannot be a value of a non-null type 'uninferred T (of fun <T> mutableStateOf)'. :49
Cannot infer type for type parameter 'T'. Specify it explicitly. :107
```

### Causa
El compilador no podía inferir el tipo de `ImageCapture?` en `mutableStateOf(null)`.

### Solución
Declarar el tipo explícitamente:
```kotlin
// ❌ Antes
var imageCapture by remember { mutableStateOf(null) }

// ✅ Después
var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
```

---

## Problema 3: `goToLogin` inferido como `() -> Boolean`

### Síntoma
```
Argument type mismatch: actual type is 'Function0<Boolean>', but 'Function0<Unit>' was expected. :81, :93, :105, :125
```

### Causa
`navController.popBackStack()` retorna `Boolean`. Al escribir `val goToLogin = { navController.popBackStack() }`, Kotlin infería el tipo como `() -> Boolean`, pero todos los parámetros de callback esperaban `() -> Unit`.

### Solución
Declarar el tipo explícitamente y agregar `Unit` al final del lambda:
```kotlin
// ❌ Antes
val goToLogin = { navController.popBackStack() }

// ✅ Después
val goToLogin: () -> Unit = { navController.popBackStack(); Unit }
```

---

## Problema 4: Advertencias `Assigned value is never read`

### Causa
Los lambdas de `onChange` usaban `it` anidado dentro de otro `it`, generando _variable shadowing_. El compilador marcaba la variable externa como nunca leída.

### Solución
Nombrar explícitamente el parámetro externo:
```kotlin
// ❌ Antes
onFullNameChange = { if (it.all { c -> c.isLetter() ... }) { fullName = it } }

// ✅ Después
onFullNameChange = { value -> if (value.all { c -> c.isLetter() ... }) { fullName = value } }
```

---

## Cambios de diseño y flujo

### Flujo de 4 pasos implementado

```
Paso 1 → Formulario de datos
Paso 2 → Cámara: parte frontal de la cédula
Paso 3 → Cámara: parte trasera de la cédula
Paso 4 → Confirmación + botón "Crear cuenta"
```

Antes de esta fase existían solo 3 pasos (`FORMULARIO`, `CAMARA_FRONTAL`, `CAMARA_TRASERA`), sin pantalla de confirmación, y el botón "Registrarse" estaba dentro del formulario.

### Paso 1 — Formulario

- Se **eliminó** el botón "Registrarse" del formulario (redundante).
- Se **eliminó** el botón de escaneo con emoji (`📷`).
- Se **agregó** un único botón `VERIFICAR CÉDULA` con el ícono `Icons.Filled.CameraAlt` de Material Icons.
- El botón queda **deshabilitado** hasta que todos los campos sean válidos (nombre, documento 5–10 dígitos, teléfono 10 dígitos iniciando en 3, PIN de 4 dígitos coincidentes).
- El parámetro `cargando` se eliminó de `RegisterViewContent` al no tener uso tras quitar el botón de registro.

### Paso 2 y 3 — Cámara

- `StepIndicator` corregido de `totalSteps = 3` a `totalSteps = 4` para estandarizar.
- Se agregó el parámetro `onBackToLogin` a `CameraCaptureView`.
- Se agregó enlace `LoginRow` al pie de cada pantalla de cámara.

### Paso 4 — Confirmación (`RegisterConfirmacionView`)

Nueva pantalla con:
- `StepIndicator(currentStep = 4, totalSteps = 4)`.
- Ícono `CheckCircle` en cyan.
- Card de resumen con: nombre, número de documento, teléfono y estado de cédula.
- Botón `CREAR CUENTA` usando el componente `HadesButton` existente (aquí se llama a `viewModel.register(...)`).
- Enlace `← Volver a tomar fotos` para regresar al paso 3.
- Enlace `LoginRow` para ir a inicio de sesión.

---

## Estandarización del enlace "Iniciar sesión"

Se extrajo un composable reutilizable `LoginRow` para garantizar el mismo estilo en los 4 pasos:

```kotlin
@Composable
fun LoginRow(onBackToLoginClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text  = "¿Ya tienes cuenta? ",
            color = HadesOnDark.copy(alpha = 0.5f)  // gris tenue
        )
        TextButton(onClick = onBackToLoginClick) {
            Text(
                text       = "Iniciar sesión",
                color      = HadesOrange,            // naranja
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

**Antes:** los pasos 2, 3 y 4 usaban texto gris plano sin el formato del formulario.  
**Después:** los 4 pasos usan `LoginRow` con texto gris + enlace naranja en negrita.

---

## Archivos modificados

| Archivo | Tipo de cambio |
|---|---|
| `app/build.gradle.kts` | CameraX `1.4.2` → `1.5.0-alpha06` |
| `presentation/auth/register/CameraCaptureView.kt` | Corrección de tipos, `totalSteps = 4`, `onBackToLogin`, `LoginRow` |
| `presentation/auth/register/RegisterView.kt` | Flujo de 4 pasos, nuevo `RegisterConfirmacionView`, `LoginRow`, corrección de tipos lambda, limpieza de parámetros |

---

## Commits de esta fase

| SHA | Descripción |
|---|---|
| `1bd220f` | Actualizar CameraX a 1.5.0-alpha06 y corregir tipos en mutableStateOf |
| `c23daad` | Flujo de 4 pasos, paso 4 confirmación con crear cuenta, ícono material en botón cámara |
| `35d3358` | Estandarizar totalSteps=4 en cámara y agregar botón volver a login en todos los pasos |
| `6d74343` | Corregir tipo `() -> Unit` en goToLogin y limpiar advertencias |
| `2feb0f7` | Estandarizar botón iniciar sesión al mismo estilo en todos los pasos |
