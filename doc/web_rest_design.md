# Diseño del servidor REST

## Component Design

### Modelo compartido

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`. No se crea una clase `Medicion`. El cliente envía `tipo` y `valor`; el servidor crea `id` y `fecha`.

### Rutas REST

```text
POST /mediciones
(tipo: Text, valor: R) --> guardarMediciones() -->
JSON de entrada: { "tipo": "CO2", "valor": 500 }
El POST Android devuelve una respuesta HTTP 201 cuando se guarda correctamente; el cliente debe informar errores HTTP.

GET /mediciones
mostrarMediciones() --> [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
JSON de salida: [
  { "id": 1, "tipo": "CO2", "valor": 500, "fecha": "2026-09-25T10:30:00" },
  { "id": 2, "tipo": "TEMPERATURA", "valor": -19, "fecha": "2026-09-25T10:31:00" }
]
```

### Dependencias y límites

El endpoint delega toda persistencia a la lógica de negocio. La lógica utiliza PDO y consultas preparadas. La configuración separa desarrollo XAMPP, producción Plesk y pruebas; las credenciales reales no forman parte del repositorio.

### Ubicación de implementación

`src/communication/mediciones.php`; la aplicación desplegable conserva su copia en `EsqueletoWebAppEnPHPConSesion/src/rest/mediciones.php`.

### Diseño global del módulo `rest/mediciones.php`

Es un módulo PHP de endpoint, no una clase. Recibe el método HTTP y el cuerpo, transforma el resultado de la lógica en un código HTTP y una respuesta JSON. En ejecución web añade cabeceras CORS y serializa la respuesta.

```text
┌────────────────────────────── rest/mediciones.php ──────────────────────────┐
│ atenderMediciones(metodo, cuerpo): [codigo HTTP, respuesta]                 │
│ Adaptador HTTP activo salvo cuando PRUEBA_REST está definido                 │
└─────────────────────────────────────────────────────────────────────────────┘
             │ delega
             ▼
       lógica/mediciones.php
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `atenderMediciones(string metodo, string cuerpo): array` | `metodo: Text, cuerpo: Text --> atenderMediciones() --> (codigo: N, respuesta: Dict|Nulo)` | OPTIONS devuelve 204; POST valida JSON, delega el alta y devuelve 201; GET devuelve la lista con 200; métodos no admitidos dan 405; errores de validación dan 422 y fallos inesperados 500. |

En la entrada HTTP, el módulo toma `REQUEST_METHOD` y el cuerpo de `php://input`, llama a `atenderMediciones`, define el estado y el tipo JSON y escribe la respuesta si no es nula.

### Tabla de respuestas HTTP

| Método o condición | Código | Respuesta | Efecto |
|---|---:|---|---|
| `OPTIONS` | 204 | Sin cuerpo | Preflight CORS. |
| `POST` correcto | 201 | `{ "resultado": "medición guardada" }` | Valida y guarda una fila. |
| `POST` con JSON/campos/valor inválidos | 400 | `{ "error": ... }` | No llama a la lógica de alta. |
| `POST` con tipo no aceptado o valor no finito | 422 | `{ "error": ... }` | La validación de negocio rechaza el alta. |
| `GET` | 200 | Arreglo de mediciones | Consulta la lógica y la base. |
| Método diferente | 405 | `{ "error": "Método no permitido" }` | Incluye cabecera `Allow`. |
| Error inesperado | 500 | `{ "error": ... }` | Registra el error en el log del servidor. |

### Adaptación entre endpoint y función

`atenderMediciones()` recibe método/cuerpo y devuelve el par `[código, respuesta]`. La envoltura HTTP define cabeceras CORS para GET, POST y OPTIONS, toma la entrada real y serializa JSON. Cuando `PRUEBA_REST` está definido, se omite esa envoltura para permitir probar la función con entradas explícitas sin emitir encabezados ni leer la petición global.

### Seguridad y límites

- CORS está habilitado con origen `*` en el endpoint actual; la autenticación/autorización no forma parte de este diseño.
- Las consultas SQL están en la capa de lógica, no en el controlador REST.
- La capa no acepta una ruta distinta de `/mediciones`; la reescritura de URL corresponde a la configuración del servidor.
- El endpoint no sirve credenciales en la respuesta; el detalle de error se devuelve con 500 según la implementación actual y también se registra en el log.

## Design Clarifications

- El contrato es `/mediciones`; `.htaccess` puede reescribir la URL hacia el archivo PHP del endpoint.
- Los tests de integración de GET y POST usan solo la base de datos de pruebas y comprueban el ciclo de limpieza.

## General Rules

- **Programming Language:** PHP para el servidor y JSON para el contrato HTTP.
- **Function/Method Headers:** Cada función debe llevar inmediatamente encima su diseño lógico, delimitado por líneas `--------------------`.
- **Code Readability:** Comentarios adicionales breves solo para decisiones importantes.
- **Automated Testing:** Integración de POST y GET; vaciar, preparar datos, comprobar, borrar y verificar tabla vacía.

