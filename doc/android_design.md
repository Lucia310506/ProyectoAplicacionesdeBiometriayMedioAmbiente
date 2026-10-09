# Diseño de la lógica fake Android

## Component Design

### Modelo compartido

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`. No se crea clase `Medicion`. Se pasan `tipo` y `valor`; el servidor crea `id` y `fecha`.

### Lógica fake y cliente REST

```text
tipo: Text, valor: R --> guardarMediciones() -->

                                                             -------- LogicaNegocio --------
                                                             |
                                                             | mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
                                                             |
                       tipo: Text, valor: R --> | guardarMediciones() -->
                                                             |
                                                             |
 mediciones: [ (id: N, tipo: Text,                    |
                valor: R, fecha: DateTime) ] <-- | mostrarMediciones() <--
                                                             --------------------------------

POST /mediciones
(tipo: Text, valor: R) --> guardarMediciones()

tipo: Text, valor: R --> PeticionarioREST.enviarMedicion() --x
```

El diseño de `LogicaFake.java` combina el contrato de la lógica (`guardarMediciones`/`mostrarMediciones`) con el contrato de rutas REST y el envío Android. `PeticionarioREST.java` sigue siendo un archivo y componente separado, con la operación estática `enviarMedicion(tipo, valor)`. La lógica fake debe integrarse con el servicio BLE actual sin romperlo, de modo que se pueda probar el envío de la medición recibida por beacon. La URL base REST se configura en un único archivo o constante, intercambiable entre XAMPP y Plesk.

### Interpretación de la separación

`LogicaFake` implementa el punto de entrada de lógica solicitado por el diseño. `PeticionarioREST` es la operación de infraestructura REST con interfaz estática `enviarMedicion(tipo, valor)`. El servicio BLE conserva su escucha y utiliza la lógica fake y el cliente REST conforme a sus respectivos diseños; son archivos/clases separados.

### Ubicación de implementación

`BTLEAlumnos2021hechoapp2/app/src/main/java/com/example/ldiamur/btlealumnos2021app/LogicaFake.java`, `PeticionarioREST.java` y `ServicioEscuharBeacons.java`.

## Design Clarifications

- `guardarMediciones(tipo, valor)` devuelve `void`, como indica la firma del diseño. Las entradas inválidas producen `IllegalArgumentException`; el servicio BLE captura el rechazo antes de enviar por REST.
- `mostrarMediciones(callback)` delega GET `/mediciones` en `PeticionarioREST`; Android recibe la lista JSON en su callback porque la petición corre en segundo plano. El servidor conserva la lista y genera `id` y `fecha`.
- Las pruebas de `MainActivity` se ejecutan al pulsar el botón de la pantalla, no al iniciar la app. La lógica fake se comprueba en el hilo principal y los casos REST simulados corren en segundo plano; el resumen aparece en LogCat.
- El botón ejecuta la batería REST con conexiones simuladas: GET `/mediciones`, POST con solo tipo y valor, respuesta 201 y error HTTP; así no escribe en la base de datos real.

## General Rules

- **Programming Language:** Java para Android.
- **Function/Method Headers:** Cada función debe tener inmediatamente encima el diseño lógico en comentario delimitado por líneas `--------------------`.
- **Code Readability:** Comentarios adicionales breves si explican una decisión necesaria.
- **Automated Testing:** Servidor HTTP simulado para ruta, método, cuerpo JSON, respuesta 201 y errores HTTP.
