# Diseño de conexión a base de datos

## Component Design

### Responsabilidad

Crear y configurar conexiones PDO para los entornos permitidos, manteniendo credenciales fuera de los archivos versionados y usando consultas parametrizadas.

### Tipos lógicos

- `Entorno = { PRODUCCION, PRUEBAS }`
- `Conexion = PDO`

### Interfaces lógicas

- `entorno: Entorno --> obtenerConfiguracion() --> Dict`
- `entorno: Entorno --> conectarBaseDatos() --> Conexion`

### Diseño global del módulo `ConexionMediciones.php`

Este archivo contiene funciones globales, no una clase instanciable. Resuelve las credenciales del entorno y crea una conexión PDO con excepciones y filas asociativas.

```text
┌────────────────────────── ConexionMediciones.php ──────────────────────────┐
│ conectarBaseDatos(entorno): PDO                                            │
│ obtenerConfiguracion(entorno): Dict                                        │
│ credencialesProduccionIncompletas(host, base, usuario, password): Boolean  │
│ obtenerEntornoBaseDatos(): Text                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `conectarBaseDatos(string entorno = 'produccion'): PDO` | `entorno: Text --> conectarBaseDatos() --> PDO` | Resuelve la configuración y abre MySQL con PDO, `utf8mb4`, excepciones y filas asociativas. |
| `obtenerConfiguracion(string entorno = 'produccion'): array` | `entorno: Text --> obtenerConfiguracion() --> Dict` | Acepta producción o pruebas; carga la configuración local/variables de entorno y devuelve host, base, usuario y password. |
| `credencialesProduccionIncompletas(host, base, usuario, password): bool` | `host: Text, base: Text, usuario: Text, password: Text --> credencialesProduccionIncompletas() --> B` | Detecta datos ausentes o marcadores `ESCRIBE_` antes de abrir una conexión de producción. |
| `obtenerEntornoBaseDatos(): string` | `--> obtenerEntornoBaseDatos() --> Text` | Devuelve `MEDICIONES_ENTORNO` o, si no existe, `produccion`. |

### Resolución de configuración

1. `obtenerEntornoBaseDatos()` lee `MEDICIONES_ENTORNO`; el valor predeterminado es `produccion`.
2. `obtenerConfiguracion()` solo acepta `produccion` o `pruebas`. Un entorno distinto produce `InvalidArgumentException`.
3. En producción, el archivo local `ConfiguracionProduccion.php` puede proporcionar `host`, `base`, `usuario` y `password`. También se admiten las variables `MEDICIONES_DB_HOST_PROD`, `MEDICIONES_DB_NAME_PROD`, `MEDICIONES_DB_USER_PROD` y `MEDICIONES_DB_PASSWORD_PROD`.
4. En pruebas se utilizan las variables con sufijo `_TEST`. Si se omiten, el código incluye valores de desarrollo por defecto; en una instalación real de pruebas se deben configurar explícitamente.
5. La función revisa que la configuración de producción no esté vacía ni conserve marcadores `ESCRIBE_`; si falta, lanza `RuntimeException` antes de conectar.
6. `conectarBaseDatos()` abre PDO MySQL con `utf8mb4`, modo de error por excepción y fetch asociativo.

### Contrato de configuración

| Entorno | Archivo local | Variables | Uso previsto |
|---|---|---|---|
| Producción | `EsqueletoWebAppEnPHPConSesion/src/BBDD/ConfiguracionProduccion.php` | `MEDICIONES_DB_*_PROD` | Peticiones normales de la API y la web. |
| Pruebas | Ninguno | `MEDICIONES_DB_*_TEST` y `MEDICIONES_ENTORNO=pruebas` | Batería que elimina filas en su tabla. Debe ser una base separada. |

El archivo `.txt` de configuración es una guía para la persona que despliega; PHP no lo carga. Las credenciales reales deben quedar fuera del control de versiones.

## Design Clarifications

- La configuración de producción se entrega mediante un archivo local que no debe publicarse.

## General Rules

- **Programming Language:** PHP para la conexión a base de datos.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Verificar conexiones válidas/invalidas, selección de entorno y consultas parametrizadas.
