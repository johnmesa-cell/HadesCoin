# Paquete: presentation

## Responsabilidad
Contiene la lógica de presentación de la aplicación. Su rol es conectar la interfaz de usuario con la lógica de negocio (Clean Architecture), utilizando ViewModels y LiveData para manejar el estado de las pantallas de manera reactiva y sobrevivir a los cambios de configuración del ciclo de vida de Android.

## Archivos

### HomeViewModel.kt
- **Qué es:** ViewModel que gestiona el estado de la pantalla principal (Dashboard) de la billetera.
- **Qué hace:** Expone el estado reactivo (`LiveData`) del usuario y su historial de transacciones. Ejecuta la carga de datos a través del caso de uso correspondiente y permite refrescar la información, manejando de forma segura los estados de carga (`cargando`) y posibles errores de conexión.
- **Interactúa con:** `GetWalletDataUseCase`, las entidades `AppUser` y `WalletTransaction`, y el inyector de dependencias manual `ServiceLocator`.

### TransferViewModel.kt
- **Qué es:** ViewModel encargado de gestionar la lógica y el estado de la pantalla de transferencias.
- **Qué hace:** Valida los datos de entrada antes de procesar el envío (verifica que el monto sea mayor a cero, que el teléfono tenga 10 dígitos y el PIN 4, y evita auto-transferencias). Además, precarga el saldo actual del emisor y ejecuta la transferencia de fondos, emitiendo los estados de éxito, error o carga hacia la UI.
- **Interactúa con:** `TransferUseCase`, `GetWalletDataUseCase` y `ServiceLocator`.