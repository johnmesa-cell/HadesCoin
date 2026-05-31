git# Resumen Financiero Gráfico — HadesCoin

Este documento detalla la implementación técnica, lógica de negocio y diseño visual del componente de **Resumen Financiero Gráfico** integrado en la pantalla principal de HadesCoin.

---

## 1. Objetivo
Proporcionar al usuario una visualización inmediata y profesional de su salud financiera, comparando el flujo de ingresos frente al de egresos mediante una interfaz gráfica dinámica.

---

## 2. Componentes Técnicos

### 2.1 Archivo Principal
- **Ruta**: `app/src/main/java/com/example/hadescoin/presentation/components/HadesFinancialChart.kt`
- **Tecnología**: Jetpack Compose con `Canvas` API.

### 2.2 Dependencias de Color
El componente utiliza el sistema de diseño de HadesCoin:
- `HadesCyan`: Representa los **Ingresos**.
- `HadesOrange`: Representa los **Egresos**.
- `HadesNavyDark`: Fondo del contenedor con transparencia.
- `HadesPurple`: Cabecera del componente.

---

## 3. Lógica de Cálculos (Precisión Financiera)

Para evitar errores de redondeo comunes (donde las sumas dan 99% o 101%), se implementó la siguiente lógica aritmética:

1. **Total de Movimiento**: Se suma el monto absoluto de ingresos y egresos.
   ```kotlin
   val totalMovimiento = ingresos + egresos
   ```
2. **Porcentaje de Ingresos**: Se utiliza `kotlin.math.round` para obtener el entero más cercano.
   ```kotlin
   val porcentajeIngresos = round((ingresos / totalMovimiento) * 100).toInt()
   ```
3. **Porcentaje de Egresos**: Se calcula por diferencia para garantizar que la suma sea siempre **exactamente 100%**.
   ```kotlin
   val porcentajeEgresos = 100 - porcentajeIngresos
   ```

---

## 4. Diseño de Interfaz

### 4.1 Gráfico de Dona (Donut Chart)
Se dibuja mediante la API `Canvas` de Compose:
- **Pista de Fondo**: Un círculo con opacidad mínima (5%) para definir la estructura.
- **Arco de Ingresos**: Comienza en la posición superior (-90°) y se extiende proporcionalmente al porcentaje calculado.
- **Arco de Egresos**: Comienza donde termina el de ingresos, completando el círculo.
- **Estilo**: Se utiliza `StrokeCap.Round` para dar un acabado suavizado y moderno a las puntas de los arcos.

### 4.2 Centro Dinámico (ÉXITO)
El centro del gráfico funciona como un indicador de salud financiera:
- Muestra el **Porcentaje de Ingresos** (dominancia del capital sobre el gasto).
- El texto es completamente dinámico y se actualiza en tiempo real al detectar nuevos movimientos.

### 4.3 Leyenda Informativa
A la derecha del gráfico se presenta un desglose detallado que incluye:
- Indicador de color (punto).
- Etiqueta de categoría (Ingresos/Egresos).
- Porcentaje relativo.
- Monto total formateado en moneda local.

---

## 5. Integración en el Flujo
El componente se integra en `HomeView.kt` dentro del `LazyColumn` principal. Recibe los datos procesados desde el `HomeViewModel`, el cual filtra las transacciones por dirección (`IN`/`OUT`) antes de pasar los totales al componente.

---

## 6. Estado Cero
Si el usuario no posee transacciones, el componente muestra una "pista vacía" elegante y porcentajes en 0%, manteniendo la integridad visual de la aplicación sin generar errores de división por cero.

