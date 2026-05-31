# Paquete: data

## Responsabilidad
Este paquete conforma la capa de datos de la aplicación. Es responsable de implementar las interfaces definidas en el dominio, manejar las fuentes de datos (Firebase Realtime Database) y mapear los modelos de datos de la red a las entidades del dominio.

## Archivos

### FirebaseUserDataSource.kt
- **Qué es:** La fuente de datos remota para usuarios.
- **Qué hace:** Ejecuta las consultas y operaciones directas contra Firebase para registro, login y búsqueda de usuarios.
- **Interactúa con:** La base de datos de Firebase y los repositorios.

### FirebaseTransactionDataSource.kt
- **Qué es:** La fuente de datos remota para transacciones.
- **Qué hace:** Ejecuta las consultas contra Firebase para registrar y consultar transferencias de dinero, depósitos en cajero, retiros y movimientos generales.
- **Interactúa con:** La base de datos de Firebase y `WalletRepositoryImpl`.

### FirebaseNotificationDataSource.kt
- **Qué es:** La fuente de datos remota para notificaciones.
- **Qué hace:** Gestiona la lectura y escritura de notificaciones asociadas a usuarios en Firebase. Permite obtener el historial de notificaciones y crear nuevas notificaciones transaccionales.
- **Interactúa con:** La base de datos de Firebase y `WalletRepositoryImpl` / repositorios de notificaciones.

### SessionLocalDataSource.kt (data/datasource/local/)
- **Qué es:** La fuente de datos local persistida en el dispositivo.
- **Qué hace:** Almacena datos de sesión local del usuario mediante DataStore (Jetpack Preferences), incluyendo el flag `biometriaActiva` que persiste la preferencia de autenticación biométrica entre sesiones. Permite recuperar la sesión anterior si el usuario cierra y reabre la app.
- **Interactúa con:** `SessionRepository` y permite la funcionalidad de login inteligente.

### FirebaseAuthRepositoryImpl.kt
- **Qué es:** Implementación concreta del repositorio de autenticación.
- **Qué hace:** Orquesta las operaciones de login y registro delegando a `FirebaseUserDataSource`.
- **Interactúa con:** `FirebaseUserDataSource` y la interfaz `AuthRepository` del dominio.

### SessionRepositoryImpl.kt
- **Qué es:** Implementación concreta del repositorio de sesión.
- **Qué hace:** Mantiene y persiste el estado de la sesión local mediante `SessionLocalDataSource`. Gestiona el flag `biometriaActiva` que indica si el usuario prefiere autenticarse con huella.
- **Interactúa con:** `SessionLocalDataSource`, capa de dominio `SessionRepository`, y es consumida por `ProfileViewModel`, `LoginViewModel`, `TransferViewModel`.

### WalletRepository.kt (Interfaz, dominio)
- **Qué es:** Interfaz que define el contrato del repositorio principal de la billetera.
- **Qué hace:** Establece los métodos necesarios para:
  - Obtener el resumen de la cuenta con historial de transacciones (`getWalletData`)
  - Buscar usuarios por teléfono (`getUserByPhone`)
  - Realizar transferencias de dinero con soporte para autenticación biométrica (`transferFunds` con parámetro `autenticadoConHuella`)
  - Actualizar PIN y apodo del usuario
  - Generar, validar y guardar códigos de verificación pertenecientes a usuarios
  - Generar códigos temporales de retiro en cajero (`saveWithdrawalCode`)
  - Marcar retiros fallidos (`markWithdrawalFailed`)
- **Interactúa con:** `AppUser`, `WalletTransaction`. Es consumida por todos los Casos de Uso transaccionales e implementada en `WalletRepositoryImpl`.

### WalletRepositoryImpl.kt
- **Qué es:** Implementación concreta del repositorio principal de la billetera.
- **Qué hace:** Orquesta:
  - La obtención del saldo e historial de transacciones a través de `FirebaseUserDataSource` y `FirebaseTransactionDataSource`
  - Transferencias de fondos que omiten validación de PIN cuando `autenticadoConHuella = true`
  - Generación y validación de códigos de verificación temporales
  - Generación de códigos de retiro en cajero con expiración de 10 minutos
  - Mapeo automático entre snapshots de Firebase y modelos de dominio
- **Interactúa con:** `FirebaseUserDataSource`, `FirebaseTransactionDataSource` y la interfaz `WalletRepository` del dominio.

## Diagrama de dependencias
Firebase (BBDD) → data (DataSource → RepositoryImpl) → domain
