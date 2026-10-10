# Diseño de lógica de negocio del servidor

## Component Design

### Responsabilidad

El módulo de negocio aplica las reglas de mediciones. Valida tipo y valor, guarda datos en la tabla `mediciones`, consulta registros y normaliza los valores para devolver el contrato del dominio. La persistencia se realiza a través de PDO y sentencias preparadas.

### Modelo y contexto de base de datos

La lógica opera sobre la tabla definida en `database_design.md`:

```text
TABLE mediciones
  id: INT, NOT NULL, AUTO_INCREMENT, PRIMARY KEY
  tipo: ENUM('CO2', 'TEMPERATURA'), NOT NULL
  valor: DOUBLE, NOT NULL
  fecha: DATETIME, NOT NULL, DEFAULT CURRENT_TIMESTAMP
```

`Medicion = (id: N, tipo: Text, valor: R, fecha: DateTime)` y `Mediciones = [ Medicion ]`. La entrada de alta es `(tipo, valor)`; MySQL asigna `id` y `fecha`.

Para describir persistencia sin introducir tipos PDO en el contrato lógico se usan estos alias abstractos: `BaseDatos` (acceso a persistencia), `Consulta = Text`, `Parametro = Text | Z | R | B` y `Fila = Medicion`.

### Diseño global del módulo

La implementación actual es un archivo de funciones PHP globales, no una clase. No mantiene estado de instancia. Depende de las funciones de conexión de `ConexionMediciones.php`.

```text
tipo: Text, valor: R --> guardarMediciones() -->

bd: BaseDatos, consultas: [Consulta], parametros: [Parametro] --> ejecutarPrimeraConsultaValida() --> [Fila]

                    -------- mediciones.php --------
                    │ sin estado propio
                    └─ normalizarFilaMedicion() --> Medicion

Mediciones <-- mostrarMediciones() <--
```

| Función | Diseño lógico | Responsabilidad |
|---|---|---|
| `guardarMediciones(tipo: Text, valor: R)` | `tipo: Text, valor: R --> guardarMediciones() -->` | Rechaza tipos distintos de CO2/TEMPERATURA y valores no finitos; inserta de forma parametrizada en `mediciones`. No devuelve dato. |
| `mostrarMediciones()` | `--> mostrarMediciones() --> [Medicion]` | Lee todas las filas, ordenadas de más reciente a más antigua, y las normaliza. |
| `ejecutarPrimeraConsultaValida(bd: BaseDatos, consultas: [Consulta], parametros: [Parametro])` | `bd: BaseDatos, consultas: [Consulta], parametros: [Parametro] --> ejecutarPrimeraConsultaValida() --> [Fila]` | Detalle interno de persistencia: ejecuta consultas candidatas en orden; devuelve filas para lectura o lista vacía en alta; comunica fallo para que la capa externa lo maneje. `PDO` y `PDOException` son detalles de implementación, no tipos del diseño lógico. |
| `normalizarFilaMedicion(fila: Fila)` | `fila: Fila --> normalizarFilaMedicion() --> Medicion` | Convierte columnas a los tipos estables del contrato (`id`, `tipo`, `valor`, `fecha`). |

### Reglas y fallos

- `tipo` se compara de forma estricta con `CO2` y `TEMPERATURA`.
- `valor` debe ser finito; el esquema no declara límites ambientales de rango.
- Las entradas no válidas lanzan `InvalidArgumentException`; los problemas de conexión/SQL se propagan como excepciones para que la capa llamadora los gestione.
- Las entradas y salidas se expresan mediante tipos lógicos del dominio; las operaciones de persistencia quedan delegadas a PDO.
- Las variantes SQL actuales toleran diferencias históricas de mayúsculas en nombres de tabla/columnas; se pueden reducir cuando todos los despliegues compartan un esquema único.

### Correspondencia de implementación

La implementación organizada por componentes está en `src/business_logic/mediciones.php`; la conexión está en `src/database/ConexionMediciones.php`. La aplicación web desplegable conserva sus copias en `EsqueletoWebAppEnPHPConSesion/src/logica/mediciones.php` y `EsqueletoWebAppEnPHPConSesion/src/BBDD/ConexionMediciones.php`. El diseño detallado de consultas también aparece en [`web_logic_design.md`](web_logic_design.md).

## Design Clarifications

- La lógica limita sus dependencias a la validación del dominio y a las operaciones de persistencia PDO.
- `guardarMediciones()` devuelve `void` porque el contrato solo requiere completar o comunicar un error mediante excepción.
- La estructura no crea una entidad de clase `Medicion`; el contrato lógico es una agregación.

## General Rules

- **Programming Language:** PHP.
- **Function/Method Headers:** Cada función debe tener un bloque con su diseño lógico entre líneas `--------------------`.
- **Code Readability:** Las funciones deben conservar la separación de validación, SQL y normalización.
- **Automated Testing:** Probar tipos válidos/no válidos, valores no finitos, inserción, consulta, orden y normalización contra una BD aislada.
