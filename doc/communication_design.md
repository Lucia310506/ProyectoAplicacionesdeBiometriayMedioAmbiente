# Diseño de comunicación del servidor

## Component Design

### Responsabilidad

El endpoint REST adapta solicitudes HTTP al contrato lógico de mediciones. Lee método y cuerpo de la petición, valida el JSON estructural, llama a la lógica de negocio y convierte el resultado en código HTTP más cuerpo JSON. No contiene consultas SQL.

### Interfaces externas

```text
POST /mediciones
Entrada JSON: { "tipo": Text, "valor": R }
Salida correcta: HTTP 201, { "resultado": "medición guardada" }

GET /mediciones
Salida correcta: HTTP 200, [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]

OPTIONS /mediciones
Salida: HTTP 204
```

### Diseño global del módulo

`mediciones.php` es un módulo PHP sin estado, basado en funciones globales. `atenderMediciones()` permite evaluar el adaptador con método/cuerpo explícitos; la envoltura al final del archivo conecta la función con las variables globales HTTP y serializa su respuesta.

```text
Cliente Android / navegador
          │ HTTP
          ▼
┌──────────────── communication: /mediciones ────────────────┐
│ atenderMediciones(metodo, cuerpo)                           │
│ valida transporte y traduce resultados a códigos HTTP       │
└──────────────────────────────┬─────────────────────────────┘
                               │ invoca funciones de dominio
                               ▼
                    business_logic/mediciones
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `atenderMediciones(string metodo, string cuerpo): array` | `metodo: Text, cuerpo: Text --> atenderMediciones() --> (codigo: N, respuesta: Dato)` | OPTIONS da 204; POST valida JSON/campos, invoca `guardarMediciones` y da 201; GET invoca `mostrarMediciones` y da 200; método no permitido da 405; entrada de negocio no válida da 422; error inesperado da 500. |
| Adaptador HTTP global | `solicitud HTTP --> endpoint --> respuesta JSON` | Añade cabeceras CORS, toma `REQUEST_METHOD` y `php://input`, fija código y `Content-Type`, y codifica el cuerpo JSON cuando existe. |

### Traducción de fallos

| Fallo | Respuesta de comunicación |
|---|---|
| JSON inválido, faltan campos o `valor` no es numérico | HTTP 400 |
| Tipo desconocido o valor que viola regla de dominio | HTTP 422 |
| Método no implementado | HTTP 405 y cabecera `Allow: GET, POST, OPTIONS` |
| Error inesperado de negocio o PDO | HTTP 500 y registro en el log PHP |

`PRUEBA_REST` desactiva la envoltura que lee/scribe datos HTTP reales, de modo que las pruebas pueden llamar a `atenderMediciones()` directamente. El endpoint declara actualmente `Access-Control-Allow-Origin: *`; no incorpora autenticación.

### Correspondencia de implementación

El endpoint ejecutable está en `EsqueletoWebAppEnPHPConSesion/src/rest/mediciones.php`. El diseño REST específico y la tabla ampliada de códigos están en [`web_rest_design.md`](web_rest_design.md).

## Design Clarifications

- La capa de comunicación valida la forma HTTP; las reglas de tipo/valor pertenecen a la lógica de negocio.
- El contrato público es `/mediciones`; `.htaccess` o la configuración del servidor puede reescribirlo al archivo PHP.
- La respuesta de alta correcta usa HTTP 201.

## General Rules

- **Programming Language:** PHP; JSON sobre HTTP.
- **Function/Method Headers:** Cada función debe tener un bloque con su diseño lógico entre líneas `--------------------`.
- **Code Readability:** El endpoint solo traduce comunicación y no debe contener SQL.
- **Automated Testing:** Cubrir GET, POST, OPTIONS, entradas inválidas, método no permitido y error interno con BD de pruebas.
