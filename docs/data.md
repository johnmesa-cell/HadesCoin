# Paquete: data

## Responsabilidad
Este paquete conforma la capa de datos de la aplicación. Es responsable de implementar las interfaces definidas en el dominio, manejar las fuentes de datos (como Firebase Realtime Database o Firestore) y mapear los modelos de datos de la red a las entidades del dominio.

## Archivos

### FirebaseDataSource.kt
- **Qué es:** La fuente de datos remota.
- **Qué hace:** Ejecuta las consultas y operaciones directas contra Firebase (ej. leer saldo, registrar usuario).
- **Interactúa con:** La base de datos de Firebase y el Repositorio.

### UserRepositoryImpl.kt
- **Qué es:** Implementación concreta del repositorio de usuarios.
- **Qué hace:** Orquesta la obtención de datos de los usuarios, gestionando si la información viene del DataSource.
- **Interactúa con:** `FirebaseDataSource.kt` y la interfaz `UserRepository` del dominio.

### TransactionDto.kt
- **Qué es:** Data Transfer Object (Modelo de datos).
- **Qué hace:** Representa la estructura exacta de la transacción tal cual viene de Firebase.
- **Interactúa con:** Los mappers para convertirse en entidades de dominio.

## Diagrama de dependencias (opcional)
Firebase (BBDD) -> data (DataSource -> RepositoryImpl) -> domain