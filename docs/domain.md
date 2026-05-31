# Paquete: domain

## Responsabilidad
Es la capa central (el núcleo) de la Clean Architecture. Aquí residen las reglas de negocio de la billetera, las entidades puras y las abstracciones (interfaces) de los repositorios. Este paquete es totalmente independiente de Android, de Firebase o de cualquier otro framework externo.

## Archivos

### AppUser.kt
- **Qué es:** Entidad de negocio (modelo de datos puro).
- **Qué hace:** Define la estructura fundamental de un usuario en el sistema (ID, documento, teléfono, nombre, PIN, saldo y fecha de creación).
- **Interactúa con:** Las interfaces de los repositorios y los Casos de Uso.

### WalletTransaction.kt
- **Qué es:** Entidad de negocio.
- **Qué hace:** Modela los datos de una transferencia o movimiento financiero (emisor, receptor, monto, tipo y dirección).
- **Interactúa con:** La interfaz `WalletRepository` y los Casos de Uso transaccionales.

### AuthRepository.kt
- **Qué es:** Interfaz que define el contrato del repositorio de autenticación.
- **Qué hace:** Establece los métodos obligatorios para iniciar sesión (`login`) y registrar usuarios (`register`), sin importar qué base de datos se use por debajo.
- **Interactúa con:** La entidad `AppUser`. Es consumida por los Casos de Uso de autenticación e implementada en la capa `data`.

### WalletRepository.kt
- **Qué es:** Interfaz que define el contrato del repositorio principal de la billetera.
- **Qué hace:** Establece los métodos necesarios para obtener el resumen de la cuenta (`getWalletData`), buscar usuarios por teléfono y realizar transferencias de dinero (`transferFunds`).
- **Interactúa con:** `AppUser` y `WalletTransaction`. Es consumida por los Casos de Uso transaccionales e implementada en la capa `data`.

### RegisterUseCase.kt
- **Qué es:** Caso de uso responsable del registro de usuarios.
- **Qué hace:** Recibe los datos de un usuario nuevo a través de la entidad `AppUser` y orquesta la operación de guardado en el sistema delegándola al repositorio.
- **Interactúa con:** La entidad `AppUser` y la interfaz `AuthRepository`.

### GetWalletDataUseCase.kt
- **Qué es:** Caso de uso específico de lectura de datos.
- **Qué hace:** Ejecuta la acción de recuperar la información principal de la billetera del usuario y su historial de transacciones utilizando su número de teléfono.
- **Interactúa con:** La interfaz `WalletRepository`.

### LoginUseCase.kt
- **Qué es:** Caso de uso para el inicio de sesión.
- **Qué hace:** Valida las credenciales del usuario (teléfono y PIN) delegando la verificación al repositorio de autenticación.
- **Interactúa con:** La interfaz `AuthRepository`.

### TransferUseCase.kt
- **Qué es:** Caso de uso que maneja las transferencias de fondos.
- **Qué hace:** Ejecuta la lógica para enviar dinero entre usuarios. Acepta un parámetro nuevo `autenticadoConHuella: Boolean` que permite omitir la validación de PIN cuando el usuario se autenticó con biometría. Valida el monto a transferir y determina si se requiere PIN basándose en el flag de autenticación biométrica.
- **Interactúa con:** La interfaz `WalletRepository`.

### GetUserProfileUseCase.kt
- **Qué es:** Caso de uso para obtener los datos completos del perfil de un usuario.
- **Qué hace:** Recupera la información del usuario (nombre, teléfono, email, apodo, etc.) y está disponible en la pantalla de perfil para visualización y edición.
- **Interactúa con:** La interfaz `WalletRepository`.

### UpdateUserPinUseCase.kt
- **Qué es:** Caso de uso para cambiar el PIN del usuario.
- **Qué hace:** Valida un PIN anterior y permite actualizar a uno nuevo mediante el flujo de recuperación de PIN con código de verificación.
- **Interactúa con:** La interfaz `WalletRepository`.

### UpdateUserNicknameUseCase.kt
- **Qué es:** Caso de uso para actualizar el apodo del usuario.
- **Qué hace:** Guarda un apodo personalizado (alias visual) para el usuario en la base de datos.
- **Interactúa con:** La interfaz `WalletRepository`.

### GenerateVerificationCodeUseCase.kt
- **Qué es:** Caso de uso para generar códigos de verificación.
- **Qué hace:** Genera un código de 6 dígitos aleatorio y lo guarda de forma temporal en Firebase asociado al teléfono del usuario. Utilizado en flujos de recuperación de PIN y validación de identidad.
- **Interactúa con:** La interfaz `WalletRepository`.

### ValidateVerificationCodeUseCase.kt
- **Qué es:** Caso de uso para validar códigos de verificación.
- **Qué hace:** Verifica que el código ingresado por el usuario coincida con el generado y guardado en Firebase. Si es correcto, marca el código como validado.
- **Interactúa con:** La interfaz `WalletRepository`.

### GenerateWithdrawalCodeUseCase.kt
- **Qué es:** Caso de uso para generar códigos temporales de retiro en cajero.
- **Qué hace:** Genera un código alfanumérico de 6 caracteres con validez de 10 minutos. Acepta parámetro `autenticadoConHuella` para omitir validación de PIN cuando el usuario se autenticó biométricamente. El código se guarda con un monto máximo autorizado.
- **Interactúa con:** La interfaz `WalletRepository`.

### CreateNotificationUseCase.kt
- **Qué es:** Caso de uso para crear notificaciones.
- **Qué hace:** Genera notificaciones transaccionales y las guarda asociadas al usuario para que las visualice en el panel de notificaciones.
- **Interactúa con:** La interfaz `WalletRepository`.

### GetUnreadNotificationsCountUseCase.kt
- **Qué es:** Caso de uso para obtener el contador de notificaciones no leídas.
- **Qué hace:** Consulta cuántas notificaciones no marcadas como leídas tiene el usuario, útil para la UI del badge en el botón de acceso a notificaciones.
- **Interactúa con:** La interfaz `WalletRepository`.