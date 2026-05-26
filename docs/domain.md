# Paquete: domain

## Responsabilidad
Es la capa central de la Clean Architecture y contiene el núcleo de la aplicación. Aquí residen las reglas de negocio, las entidades puras y las abstracciones (interfaces) de los repositorios. Es totalmente independiente de Android, Firebase o cualquier otro framework externo.

## Archivos

### AppUser.kt
- **Qué es:** Entidad de negocio que representa a un usuario del sistema.
- **Qué hace:** Define la estructura de datos pura de un usuario (ID, documento, teléfono, nombre, PIN, saldo y fecha).
- **Interactúa con:** Las interfaces de los repositorios y los Casos de Uso.

### WalletTransaction.kt
- **Qué es:** Entidad de negocio que representa un movimiento financiero.
- **Qué hace:** Modela los datos fundamentales de una transferencia (emisor, receptor, monto, tipo y dirección).
- **Interactúa con:** La interfaz `WalletRepository` y el Caso de Uso de obtención de datos.

### AuthRepository.kt
- **Qué es:** Interfaz que define el contrato abstracto del repositorio de autenticación.
- **Qué hace:** Establece los métodos obligatorios para iniciar sesión (`login`) y registrar un nuevo usuario (`register`).
- **Interactúa con:** La entidad `AppUser` y es consumida por `LoginUseCase` y `RegisterUseCase`.

### WalletRepository.kt
- **Qué es:** Interfaz que define el contrato del repositorio principal de la billetera.
- **Qué hace:** Establece los métodos necesarios para obtener el resumen de la cuenta (`getWalletData`), buscar usuarios y realizar transferencias (`transferFunds`).
- **Interactúa con:** Las entidades `AppUser` y `WalletTransaction`, y es consumida por `GetWalletDataUseCase` y `TransferUseCase`.

### GetWalletDataUseCase.kt
- **Qué es:** Caso de uso encargado de obtener la información principal de la billetera.
- **Qué hace:** Ejecuta la acción de recuperar los datos del usuario y su historial de transacciones a partir de su número de teléfono.
- **Interactúa con:** La interfaz `WalletRepository`.

### LoginUseCase.kt
- **Qué es:** Caso de uso que gestiona el inicio de sesión.
- **Qué hace:** Valida las credenciales del usuario (teléfono y PIN) delegando la acción al repositorio.
- **Interactúa con:** La interfaz `AuthRepository`.

### RegisterUseCase.kt
- **Qué es:** Caso de uso responsable del registro de nuevos usuarios.
- **Qué hace:** Toma los datos de un usuario nuevo y ejecuta la operación de guardado en el sistema.
- **Interactúa con:** La entidad `AppUser` y la interfaz `AuthRepository`.

### TransferUseCase.kt
- **Qué es:** Caso de uso que maneja la lógica de las transferencias de dinero.
- **Qué hace:** Orquesta el envío de fondos entre dos usuarios, validando el monto y el PIN de seguridad.
- **Interactúa con:** La interfaz `WalletRepository`.