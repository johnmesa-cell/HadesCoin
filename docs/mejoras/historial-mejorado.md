# Historial de Transacciones Mejorado — HadesCoin

Este documento detalla la evolución y las capacidades técnicas del nuevo módulo de historial de transacciones en HadesCoin. El objetivo principal es ofrecer al usuario una herramienta robusta de auditoría y búsqueda de sus movimientos financieros.

---

## 1. Objetivo
Transformar el historial estático en una herramienta dinámica que permita localizar rápidamente cualquier movimiento mediante filtros avanzados y visualización detallada, mejorando la experiencia de usuario (UX) y la transparencia operacional.

---

## 2. Funcionalidades Clave

### 2.1 Búsqueda por Teléfono
- **Componente**: `HadesTextField` con icono de búsqueda.
- **Acción**: Filtrado en tiempo real mientras el usuario escribe.
- **Lógica**: Busca coincidencias en los campos `senderId` o `receiverId` de la transacción.

### 2.2 Filtrado Multi-Criterio (Acumulativo)
El historial permite aplicar múltiples filtros de forma simultánea:
1.  **Por Dirección**: Ingresos (`IN`) o Egresos (`OUT`).
2.  **Por Tipo de Operación**: Transferencias, Depósitos o Retiros.
3.  **Por Fecha**: Filtro específico por día mediante un calendario interactivo.

### 2.3 Resumen de Recibo (Modal de Detalle)
Al tocar cualquier transacción, se despliega un `TransactionDetailDialog` con diseño de "recibo digital" que muestra:
- Iconografía según el tipo de movimiento.
- Monto con formato de moneda y color distintivo (Cian para ingresos, Naranja para egresos).
- Datos de origen y destino (en caso de transferencia).
- Código de verificación (si aplica).
- ID único de la transacción.

---

## 3. Implementación Técnica

### 3.1 Lógica de Filtrado (HomeView.kt)
Se utiliza un bloque de filtrado funcional en Compose que procesa la lista original en tiempo real basándose en 4 predicados:

```kotlin
val transaccionesFiltradas = transactions.filter { tx ->
    // 1. Filtro por tipo (TRANSFER, DEPOSIT, WITHDRAW)
    val matchesType = ...
    
    // 2. Filtro por dirección (IN, OUT)
    val matchesDirection = ...
    
    // 3. Filtro por búsqueda de teléfono
    val matchesSearch = ...
    
    // 4. Filtro por fecha (YYYY-MM-DD)
    val matchesDate = ...

    matchesType && matchesDirection && matchesSearch && matchesDate
}
```

### 3.2 Selección de Fecha
Se integra el `DatePicker` de **Material3** mediante `HadesDatePickerDialog`. Este componente maneja el estado del calendario y devuelve la fecha seleccionada en formato `yyyy-MM-dd` para facilitar la comparación de cadenas con el `timestamp` de Firebase.

### 3.3 Componentes UI Reutilizados
- **`HadesFilterChipRow`**: Fila de chips estilizados con temática neón que gestionan el estado de selección.
- **`TransactionRow`**: Componente de fila optimizado que muestra dirección y estado visual mediante iconos (`txIcon`).

---

## 4. Visualización de Datos

### 4.1 Formateo de Fecha y Hora
Se utiliza el helper `formatTimestamp` para convertir los sellos de tiempo ISO-8601 de la base de datos en formatos legibles (ej: "24 Abr 2026, 10:00 AM").

### 4.2 Traducción Dinámica
El sistema mapea los tipos técnicos de base de datos (`WITHDRAWAL_COMPLETED`, `DEPOSIT`) a etiquetas amigables en español mediante la función `translateTransactionType`.

---

## 5. Casos de Borde Corregidos
- **Búsqueda vacía**: Si la caja de búsqueda está en blanco, se omiten el predicado de búsqueda para mostrar todos los registros.
- **Limpieza de Filtros**: Se añadió un botón "X" que aparece solo cuando hay una fecha seleccionada para permitir al usuario restablecer la vista rápidamente.
- **Estado Cero**: Se incluyó una ilustración visual decorativa para cuando no hay resultados que coincidan con los filtros aplicados.

