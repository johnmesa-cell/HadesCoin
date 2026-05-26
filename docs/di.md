# Paquete: di

## Responsabilidad
Contiene los módulos de Inyección de Dependencias utilizando Hilt/Dagger. Su rol es proveer las instancias necesarias (repositorios, casos de uso, instancias de Firebase) para que la aplicación funcione sin instanciarlas manualmente.

## Archivos

### AppModule.kt
- **Qué es:** Módulo global de Hilt.
- **Qué hace:** Provee las dependencias que tienen un ciclo de vida global (Singleton), como la instancia de Firebase Database o FirebaseAuth.
- **Interactúa con:** Frameworks externos y repositorios.

### RepositoryModule.kt
- **Qué es:** Módulo de binding.
- **Qué hace:** Le indica a Hilt qué implementación concreta (`data`) debe inyectar cuando se solicite una interfaz del (`domain`). (ej. une `UserRepository` con `UserRepositoryImpl`).
- **Interactúa con:** Capas `data` y `domain`.

## Diagrama de dependencias (opcional)
di -> Inyecta dependencias en data, domain, y presentation