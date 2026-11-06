# Post-contenido — Refactorización Guiada por Pruebas: Code Smells, Paquetes y Acoplamiento en un Proyecto Java Real

**Asignatura:** Patrones de Diseño de Software  
**Unidad 11:** Refactorización Avanzada y Clean Code Profundo  

---

## Parte 1: Red de Seguridad y Refactorización Guiada por Evidencia

### 1. Diagnóstico de Code Smells Iniciales

#### Smell 1: Long Method (Bloater) en `InventarioService.actualizarInventario()`
- **Estado Inicial:** Método de 38 líneas que violaba el Principio de Responsabilidad Única (SRP) al concentrar cinco responsabilidades distintas:
  1. Validación de precondiciones y parámetros.
  2. Búsqueda y recuperación del producto.
  3. Validación de existencias y mutación del stock.
  4. Cálculo de la cantidad y necesidad de reorden.
  5. Registro de auditoría y emisión de alertas en consola.
- **Técnicas aplicadas:** *Extract Method* y *Guard Clauses*.
- **Estado Final:** El método `actualizarInventario()` se redujo a solo **8 líneas de código**, actuando como una tabla de contenido declarativa de alto nivel que delega en métodos privados intencionales y en una clase colaboradora especializada.

| Métrica | Antes | Después |
| :--- | :---: | :---: |
| Líneas de `actualizarInventario()` | 38 líneas | 8 líneas |
| Responsabilidades en el método | 5 mezcladas | 1 (Orquestación) |
| Métodos privados extraídos | 0 | 5 |
| Clases colaboradoras creadas | 0 | 1 (`CalculadoraReorden`) |

---

#### Smell 2: Switch Statements Duplicados (Object-Orientation Abuser)
- **Estado Inicial:** Las clases `ClasificadorProducto` y `EtiquetadoProducto` contenían dos sentencias `switch` idénticas evaluando las mismas cuatro ramas del enumerador `TipoProducto` (`LIBRO`, `ELECTRONICO`, `ROPA`, `ALIMENTO`). Cualquier nuevo tipo de producto exigía modificar ambas clases (*Shotgun Surgery*), violando el principio Open/Closed (OCP).
- **Técnica aplicada:** *Replace Conditional with Polymorphism* mediante métodos abstractos por constante en el enum.
- **Estado Final:** Se eliminaron por completo las estructuras condicionales (`switch` o `if-else`) de ambas clases. Cada constante de `TipoProducto` implementa su propia lógica de recargo y formateo de etiquetas. Si se agrega un nuevo tipo de producto, `ClasificadorProducto` y `EtiquetadoProducto` permanecen intactos.

| Métrica | Antes | Después |
| :--- | :---: | :---: |
| Sentencias `switch` en el proyecto | 2 (4 casos c/u) | 0 |
| Clases afectadas al agregar un nuevo tipo | 2 clases | 0 clases |

---

### 2. Resultados de las Pruebas de Caracterización

Las pruebas unitarias se ejecutaron con `mvn test` antes de cualquier refactorización y después de cada cambio atómico:

- **Pruebas totales:** 12 pruebas automatizadas (6 en `InventarioServiceTest`, 4 en `ClasificadorProductoTest`, 2 en `EtiquetadoProductoTest`).
- **Resultado antes de refactorizar:** `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` (Red de seguridad activa).
- **Resultado tras refactorizar Long Method:** `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` (Sin regresiones).
- **Resultado tras refactorizar Switch Statements:** `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` (Sin regresiones).

---

### 3. Decisiones de Diseño (Parte 1)

#### Decisión de Diseño 1: Extracción de `CalculadoraReorden` frente a método privado
- **Elección:** Se extrajo la clase colaboradora independiente `CalculadoraReorden` en lugar de un método privado dentro de `InventarioService`.
- **Justificación:** La política de reabastecimiento (`(stockMinimo * 3) - nuevoStock`) representa una regla de negocio del dominio de compras y logística que tiene una razón de cambio completamente distinta a la transacción de inventario. Mantenerla como método privado concentraría lógica contable/logística en el servicio. Al aislarla en su propia clase:
  1. Se respeta estrictamente el Principio de Responsabilidad Única (SRP).
  2. Se facilita su evolución futura (por ejemplo, añadir stock de seguridad variable, tiempos de entrega de proveedores o estacionalidad) sin modificar el servicio.
  3. Permite pruebas unitarias directas e independientes sobre las fórmulas de reorden.

#### Decisión de Diseño 2: Enum polimórfico frente a jerarquía de clases de estrategia
- **Elección:** Se transformó el enumerador `TipoProducto` en un enum con métodos abstractos implementados por cada constante (`LIBRO`, `ELECTRONICO`, etc.) en lugar de crear una jerarquía de clases con el patrón Strategy.
- **Justificación:** El catálogo de tipos de producto representa una taxonomía cerrada y estable en este modelo de inventario. Los cálculos de recargo y etiquetado dependen exclusivamente del estado intrínseco del producto (su precio y nombre), sin requerir inyección de dependencias de Spring, acceso a repositorios ni servicios externos. Crear una interfaz y cuatro o más clases adicionales habría generado sobreingeniería (*Speculative Generality*). La solución con enum proporciona polimorfismo nativo en tiempo de compilación con cero sobrecosto de mantenimiento.