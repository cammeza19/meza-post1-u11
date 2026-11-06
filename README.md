
# Post-contenido — Refactorización Guiada por Pruebas: Code Smells, Paquetes y Acoplamiento en un Proyecto Java Real

**Asignatura:** Patrones de Diseño de Software  
**Unidad 11:** Refactorización Avanzada y Clean Code Profundo  
**Estudiante:** Camilo Meza  
**Repositorio:** [meza-post1-u11](https://github.com/cammeza19/meza-post1-u11)  

---

## Resumen Ejecutivo de la Actividad

El proyecto consistió en la aplicación práctica de técnicas de refactorización de Martin Fowler, Clean Code de Robert C. Martin, y principios SOLID (especialmente SRP, OCP y DIP) sobre un proyecto Java real con Spring Boot, validando cada etapa mediante pruebas de caracterización automatizadas (JUnit 5) y pruebas de arquitectura estática (ArchUnit).

---

## Parte 1: Red de Seguridad y Refactorización Guiada por Evidencia

### 1. Diagnóstico de Code Smells Iniciales

#### Smell 1: Long Method (Bloater) en `InventarioService.actualizarInventario()`
- **Estado Inicial:** Método de 38 líneas que violaba el Principio de Responsabilidad Única (SRP) al concentrar cinco responsabilidades:
  1. Validación de precondiciones y parámetros.
  2. Búsqueda del producto en la colección.
  3. Validación de existencias y mutación del stock.
  4. Cálculo de necesidad y cantidad de reorden.
  5. Registro de auditoría y emisión de alertas en consola.
- **Técnicas aplicadas:** *Extract Method* y *Guard Clauses*.
- **Estado Final:** `actualizarInventario()` se redujo a solo **8 líneas de código**, actuando como una tabla de contenido declarativa que delega en métodos privados intencionales y en una clase colaboradora especializada.

| Métrica | Antes | Después |
| :--- | :---: | :---: |
| Líneas de `actualizarInventario()` | 38 líneas | 8 líneas |
| Responsabilidades en el método | 5 mezcladas | 1 (Orquestación) |
| Métodos privados extraídos | 0 | 5 |
| Clases colaboradoras creadas | 0 | 1 (`CalculadoraReorden`) |

#### Smell 2: Switch Statements Duplicados (Object-Orientation Abuser)
- **Estado Inicial:** Las clases `ClasificadorProducto` y `EtiquetadoProducto` contenían dos sentencias `switch` idénticas evaluando las mismas cuatro ramas del enumerador `TipoProducto` (`LIBRO`, `ELECTRONICO`, `ROPA`, `ALIMENTO`). Cualquier nuevo tipo de producto exigía modificar ambas clases (*Shotgun Surgery*), violando el principio Open/Closed (OCP).
- **Técnica aplicada:** *Replace Conditional with Polymorphism* mediante métodos abstractos por constante en el enum.
- **Estado Final:** Se eliminaron por completo las estructuras condicionales (`switch` o `if-else`) de ambas clases. Cada constante de `TipoProducto` implementa su propia lógica de recargo y formateo de etiquetas. Al agregar un nuevo tipo, las clases de servicio no se modifican.

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

#### Evidencia fotográfica de la red de seguridad en verde (Paso 2):
![](docs/img/01-pruebas-caracterizacion-verde.jpeg)

---

### 3. Decisiones de Diseño (Parte 1)

#### Decisión de Diseño 1: Extracción de `CalculadoraReorden` frente a método privado
- **Elección:** Se extrajo la clase colaboradora independiente `CalculadoraReorden`.
- **Justificación:** La política de reabastecimiento (`(stockMinimo * 3) - nuevoStock`) representa una regla de negocio del dominio de compras y logística que tiene una razón de cambio completamente distinta a la transacción de inventario. Mantenerla como método privado concentraría lógica contable/logística en el servicio. Al aislarla en su propia clase:
  1. Se respeta estrictamente el Principio de Responsabilidad Única (SRP).
  2. Se facilita su evolución futura (stock de seguridad variable, tiempos de entrega de proveedores o estacionalidad) sin modificar el servicio.
  3. Permite pruebas unitarias directas e independientes sobre las fórmulas de reorden.

#### Decisión de Diseño 2: Enum polimórfico frente a jerarquía de clases de estrategia
- **Elección:** Se transformó el enumerador `TipoProducto` en un enum con métodos abstractos implementados por cada constante (`LIBRO`, `ELECTRONICO`, etc.) en lugar de crear una jerarquía de clases con el patrón Strategy.
- **Justificación:** El catálogo de tipos de producto representa una taxonomía cerrada y estable en este modelo de inventario. Los cálculos de recargo y etiquetado dependen exclusivamente del estado intrínseco del producto (su precio y nombre), sin requerir inyección de dependencias de Spring, acceso a repositorios ni servicios externos. Crear una interfaz y cuatro clases adicionales habría generado sobreingeniería (*Speculative Generality*). La solución con enum proporciona polimorfismo nativo en tiempo de compilación con cero sobrecosto de mantenimiento.

---

## Parte 2: Diagnóstico y Resolución de Acoplamiento entre Paquetes

### 1. Diagnóstico del Acoplamiento Cíclico con ArchUnit

Al integrar el módulo de `reportes`, se generó una dependencia circular entre paquetes:

#### Diagrama de paquetes con ciclo (Antes):
```text
┌───────────────────────┐
│  paquete: inventario  │
└───────────┬───────────┘
            │  ▲
            │  │ (Dependencia circular)
            ▼  │
┌───────────┴───────────┐
│   paquete: reportes   │
└───────────────────────┘

```

* `inventario.InventarioService` importaba `reportes.ReporteBajoStockService`.
* `reportes.ReporteBajoStockService` importaba `inventario.InventarioService`.

#### Evidencia del fallo de la prueba de arquitectura (`ArquitecturaPaquetesTest`):

```text
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'slices matching ... should be free of cycles' was violated (1 times):
Cycle detected:
  Slice inventario ->
  Slice reportes ->
  Slice inventario

```

#### Evidencia fotográfica del diagnóstico de ArchUnit en rojo:

![](docs/img/02-archunit-ciclo-rojo.jpeg)

---

### 2. Resolución mediante Inversión de Dependencias (DIP)

Se desacoplaron los paquetes introduciendo la interfaz `NotificadorBajoStock` dentro del paquete `inventario`.

#### Diagrama de paquetes sin ciclo (Después):

```text
┌─────────────────────────────────────────┐
│           paquete: inventario           │
│                                         │
│   InventarioService ──> [Notificador]   │
└──────────────────────────────▲──────────┘
                               │ (implementa)
┌──────────────────────────────┴──────────┐
│            paquete: reportes            │
│                                         │
│        ReporteBajoStockService          │
└─────────────────────────────────────────┘

```

* La dependencia es ahora estrictamente unidireccional: `reportes` depende de `inventario`.
* `inventario` no contiene ningún import hacia `reportes`.

#### Evidencia de éxito en ArchUnit tras la refactorización:

```text
[INFO] Running com.universidad.refactoringu11.ArquitecturaPaquetesTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS

```

---

### 3. Decisiones de Diseño (Parte 2)

#### Decisión de Diseño 3: Ubicación de `NotificadorBajoStock` en `inventario`

* **Elección:** La interfaz `NotificadorBajoStock` se ubicó en el paquete `inventario` y no en `reportes`.
* **Justificación:** Siguiendo la regla de la Guía de Contenido (sección 4.3): *"los paquetes de infraestructura dependen de dominio; dominio nunca depende de infraestructura"*. En este sistema:
* `inventario` es el paquete de **mayor nivel de abstracción** (representa el núcleo del negocio y las reglas de dominio).
* `reportes` es un paquete de **menor nivel de abstracción** (actúa como servicio auxiliar/soporte).
El Principio de Inversión de Dependencias (DIP) y la arquitectura limpia establecen que los contratos (interfaces/puertos) deben pertenecer al módulo de alto nivel que los necesita. `InventarioService` define el contrato sobre cómo desea ser notificado, y cualquier módulo externo actúa como adaptador implementando esa interfaz.



#### Decisión de Diseño 4: Granularidad del método de notificación (Evento individual vs Por lote)

* **Elección:** Notificación por evento individual: `void notificar(Producto producto, int cantidadReorden)`.
* **Justificación:**
* **Ventaja de la notificación por evento individual:** Permite una reacción inmediata en tiempo real en el momento exacto en que una transacción cruza el umbral crítico de stock. No consume memoria almacenando lotes en búfer ni requiere procesos de sincronización adicionales, lo que resulta ideal para disparar alertas inmediatas o eventos de mensajería (Kafka, RabbitMQ, Webhooks).
* **Escenario para notificación por lote (`List<Producto>`):** Se preferiría una alternativa por lote en procesos por lotes (*batch processing*) o trabajos programados (e.g. un cron nocturno que consolide todas las necesidades de compra del día en un único informe PDF o Excel), o en operaciones de importación masiva de inventario donde notificar ítem por ítem causaría saturación de I/O o red.



---

### 4. Resultado Final de la Suite de Pruebas

Se ejecutó la suite completa sin modificar una sola línea de las pruebas de caracterización originales:

```text
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS

```

* **6/6** `InventarioServiceTest` (en verde)
* **4/4** `ClasificadorProductoTest` (en verde)
* **2/2** `EtiquetadoProductoTest` (en verde)
* **1/1** `ArquitecturaPaquetesTest` (en verde)

#### Evidencia fotográfica de la suite completa y arquitectura en verde:

![](docs/img/03-suite-completa-verde.jpeg)


---
