do# Documentación de Cambios — HadesCoin (Perfil y Seguridad)

## 1. Módulo de Perfil de Usuario
Se implementó una nueva sección de perfil que permite la gestión de información personal y seguridad.

*   **Modelo de Datos (`AppUser.kt`)**: 
    *   Se agregaron los campos `nickname` (apodo) y `email` (correo electrónico) para enriquecer el perfil del usuario sin comprometer su identidad legal.
*   **Identidad Visual**:
    *   Se implementó una lógica de saludo dinámico: la app ahora saluda al usuario por su **Apodo** si está registrado; de lo contrario, utiliza su primer nombre.
*   **Visualización de Datos**: 
    *   La pantalla de perfil ahora muestra: Nombre Completo, Apodo, Documento, Teléfono, Correo y Fecha de Creación.
    *   **Mejora de UX**: Se añadieron etiquetas visuales de "PENDIENTE" en color naranja para campos no registrados (como apodo o documento), incentivando al usuario a completar su información.

## 2. Seguridad y Gestión de PIN
Se fortaleció la lógica de protección de acceso y recuperación de cuenta.

*   **Cambio de PIN (Perfil)**:
    *   Flujo de validación triple: Requiere PIN actual, PIN nuevo y confirmación.
    *   **Restricciones de Seguridad**: Se implementó una "Lista Negra" de PINs obvios (ej. `1234`, `0000`, `1111`, `4321`) que son rechazados automáticamente.
    *   Se valida que el PIN nuevo sea diferente al anterior.
*   **Recuperación de PIN (Login)**:
    *   Se implementó un nuevo flujo en la pantalla de inicio para usuarios que olvidaron su clave.
    *   **Validación Cruzada**: El sistema permite recuperar el PIN validando el Número de Teléfono + Número de Documento contra Firebase.
    *   **Reset post-recuperación**: Tras recuperar el PIN, se ofrece al usuario la opción de cambiarlo inmediatamente por uno nuevo sin necesidad de iniciar sesión, aplicando las mismas reglas de seguridad (4 dígitos, no obvios).

## 3. Arquitectura y Componentes
Los cambios se realizaron bajo los estrictos estándares de **Clean Architecture** definidos para el proyecto:

*   **Capas Independientes**: Se crearon casos de uso específicos (`RecoverPinUseCase`, `GetUserProfileUseCase`, `UpdateUserPinUseCase`, `UpdateUserNicknameUseCase`) para separar la lógica de negocio de la UI.
*   **LiveData por Concepto**: Los ViewModels (`ProfileViewModel` y `LoginViewModel`) exponen estados individuales (`cargando`, `mensajeExito`, `mensajeError`) evitando el uso de `UiState` o `StateFlow`.
*   **Componentes Hades**: Se crearon diálogos personalizados (`ResetPinDialog`, `RecoverPinDialog`, `ChangePinDialog`, `ChangeNicknameDialog`) que reutilizan la estética de la app (`HadesBackground`, `HadesTextField`, `HadesCardBox`, `HadesNavyDark`).
*   **Inyección manual**: Se actualizaron los proveedores en `ServiceLocator.kt`.

---
**Nota:** Todas las contraseñas y PINs se mantienen en **texto plano** en la base de datos de Firebase Realtime Database, cumpliendo con el requerimiento técnico específico del proyecto.

