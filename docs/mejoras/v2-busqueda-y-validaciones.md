# Mejora de Búsqueda y Validaciones (v2) — HadesCoin

Este documento detalla los cambios realizados para optimizar la búsqueda en el historial y reforzar las validaciones de entrada de datos.

---

## 1. Búsqueda por Nombre y Número
Se ha evolucionado el buscador del historial para permitir la localización de transacciones no solo por el número de teléfono, sino también por el nombre del remitente o destinatario.

### 1.1 Desnormalización de Datos
Para evitar consultas costosas entre nodos de Firebase, se modificó el proceso de registro de transacciones para incluir los nombres en el momento de la operación:
- **Modelo:** Se agregaron `senderName` y `receiverName` a `WalletTransaction`.
- **Capa de Datos:** `WalletRepositoryImpl` ahora consulta y guarda los nombres completos junto a los IDs durante transferencias y retiros.

---

## 2. Optimizaciones de UX y Filtros

### 2.1 Requisito de Caracteres Mínimos
Para prevenir filtrados accidentales y mejorar la claridad:
- La búsqueda ahora se activa solo cuando el usuario ingresa **3 o más caracteres**.
- Se añadió un mensaje de aviso dinámico ("Ingresa al menos 3 caracteres...") que guía al usuario durante la escritura.

### 2.2 Restricciones de Longitud
- El campo de búsqueda en el historial se ha limitado a **20 caracteres**.
- Se reforzó la validación de **10 dígitos exactamente** para todos los campos de número telefónico (Login, Registro y Transferencias), impidiendo el ingreso de caracteres extra.

---

## 3. Impacto Técnico

### 3.1 Lógica de Búsqueda (Actualizada)
```kotlin
val matchesSearch = if (searchQuery.length < 3) {
    true // No filtra hasta tener la longitud mínima
} else {
    val query = searchQuery.lowercase()
    tx.senderId.contains(query) || 
    tx.receiverId.contains(query) || 
    tx.senderName.lowercase().contains(query) || 
    tx.receiverName.lowercase().contains(query)
}
```

### 3.2 Almacenamiento
Las nuevas transacciones en Firebase ahora lucen así:
```json
{
  "senderId": "3001234567",
  "senderName": "Juan Pérez",
  "receiverId": "3119876543",
  "receiverName": "María López",
  "amount": 50000.0,
  "type": "TRANSFER",
  "timestamp": "..."
}
```

---

## 4. Beneficios
- **Mayor Velocidad:** El usuario localiza contactos por nombre, lo cual es más intuitivo que recordar números.
- **Reducción de Errores:** Las validaciones de 10 dígitos previenen fallos en la comunicación con la base de datos por formatos inválidos.
- **Limpieza Visual:** El historial no parpadea con resultados irrelevantes mientras se escriben los primeros caracteres.

