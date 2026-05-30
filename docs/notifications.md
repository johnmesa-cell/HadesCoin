# Módulo de Notificaciones — HadesCoin

Este documento describe la implementación del sistema de notificaciones locales, avisos flotantes y cola de correos electrónicos en HadesCoin.

## Contenido
1. [Arquitectura](#arquitectura)
2. [Esquema de Base de Datos (Firebase)](#esquema-de-base-de-datos-firebase)
3. [Flujos que generan notificaciones](#flujos-que-generan-notificaciones)
4. [Componentes de UI](#componentes-de-ui)
5. [Notificaciones por Correo](#notificaciones-por-correo)

---

## Arquitectura

Siguiendo **Clean Architecture**, el módulo se divide en:

### Capa de Dominio (`domain/`)
- **Modelo:** `AppNotification` (Entidad pura).
- **Repositorios:** `NotificationRepository` e `NotificationEmailRepository` (Interfaces).
- **Casos de Uso:**
    - `CreateNotificationUseCase`: Crea una notificación en BD.
    - `GetNotificationsUseCase`: Lista las notificaciones del usuario.
    - `MarkNotificationAsReadUseCase`: Cambia el estado a leída.
    - `GetUnreadNotificationsCountUseCase`: Retorna el conteo para el badge.
    - `QueueNotificationEmailUseCase`: Encola un correo para envío externo.

### Capa de Datos (`data/`)
- **DataSource:** `FirebaseNotificationDataSource` (Acceso directo a nodos `notifications` y `emailQueue`).
- **Implementación:** `NotificationRepositoryImpl` y `NotificationEmailRepositoryImpl`.

### Capa de Presentación (`presentation/`)
- **ViewModel:** `NotificationsViewModel` (Usa `LiveData` individuales, sin `UiState`).
- **View:** `NotificationsView` (Pantalla de listado con Jetpack Compose).
- **Integración:** Actualización de `HomeViewModel`, `TransferViewModel` y `ProfileViewModel` para disparar eventos.

---

## Esquema de Base de Datos (Firebase)

### Nodo `notifications`
Organizado por número de teléfono para consultas rápidas.
```json
{
  "notifications": {
    "3001234567": {
      "-Ntxabc123": {
        "title": "Transferencia recibida",
        "message": "Recibiste $50,000.00 del numero 3110000000.",
        "type": "TRANSFER",
        "createdAt": "2026-05-30T10:00:00Z",
        "read": false
      }
    }
  }
}
```

### Nodo `emailQueue`
Cola desacoplada para que un Worker externo o Cloud Function procese el envío.
```json
{
  "emailQueue": {
    "-Mzyx987": {
      "phoneNumber": "3001234567",
      "toEmail": "usuario@ejemplo.com",
      "subject": "HadesCoin - Transferencia enviada",
      "body": "...",
      "status": "PENDING",
      "createdAt": "2026-05-30T10:05:00Z"
    }
  }
}
```

---

## Flujos que generan notificaciones

1. **Transferencias:**
    - Al emisor: Confirma el envío y el destinatario.
    - Al receptor: Notifica el abono de dinero.
2. **Retiro en Cajero:**
    - Notifica la generación del código de 6 dígitos y el monto autorizado.
3. **Seguridad (Perfil):**
    - Notifica cambios de PIN (normal o por recuperación).
    - Notifica cambios en el apodo (Nickname).

---

## Componentes de UI

### 1. Pantalla de Notificaciones (`NotificationsView`)
- Acceso desde la campanita del Home o desde el Perfil.
- Lista ordenada cronológicamente (más reciente primero).
- Indicador visual (punto naranja) para mensajes no leídos.
- Al tocar una notificación, se marca automáticamente como leída en Firebase.

### 2. Campanita e Indicador (Badge)
- Implementado en `HomeHeader` y en la vista de `Profile`.
- Muestra el número de notificaciones pendientes en tiempo real.

### 3. Notificación Flotante (Snackbar)
- Cuando ocurre un evento de éxito (ej. transferencia), se muestra un aviso flotante inmediato informando que la notificación ha sido guardada para revisión posterior.

---

## Notificaciones por Correo

El sistema verifica si el usuario tiene un campo `email` en su perfil de Firebase.
- Si existe, además de la notificación interna, se inserta un objeto en `emailQueue`.
- El objeto incluye el asunto y el cuerpo del mensaje relacionado con la transacción o cambio de seguridad.
- Este proceso es asíncrono y no bloquea la experiencia del usuario en la app.

