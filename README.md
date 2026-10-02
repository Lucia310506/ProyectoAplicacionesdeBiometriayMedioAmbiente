# ProyectoAplicacionesdeBiometriayMedioAmbiente
El proyecto consta de diferentes partes de la arquitectura:
 -Un Microcontrolador que emite beacons, que esta escrito en c++
 -Una aplicacion en java
 -Una base de datos
 -Una página web que muestra las medidas

# Estructura de carpetas
El código de Arduino se encuentra en la carpeta "HolaMundoIBeacon" que consta de:
    -HolaMundoIBeacon.ino -> Programa funcional que ejecuta el hilo principal para enviar el beacon con las medidas de temperatura y CO2.
    -LED.h -> Controla el funcionamiento de la LED para indicar que el código funciona
    -Medidor.h -> Simula las medidas de CO2 y temperatura
    -EmisoraBLE.h -> Controla la emisora y la emisión del beacon
    -ServicioEnEmisora.h -> Controla el servicio atribuyendole características. La clase Característica está en su interior.
    -PuertoSerie.h -> Controla el ciclo de vida del puerto Serie. 
    -Publicador.h ->    Encapsula la emisora y envía las medidas (codifca major/minor).

El código de la App de Android esta códificado en java y se encuentra en la carpeta BTLEAlumnos2021hechoapp2 y tiene las siguientes clases:
    -MainActivity.java -> Clase que controla el funcionamiento de la app, pide permisos, filtra y detiene la captación de beacons.
    -TramaIBeacon.java -> Clase que permite obtener los datos del beacon.
    -Utilidades.java -> Clase de apoyo para convertir entre tipos.
    -PeticionarioREST.java -> Clase que ejerce el papel de cliente REST, que envia los datos para que lleguen a la base de datos.
    -ServicioEscucharBeacon.java -> Clase que hace que la app funcione en segundo plano para captar beacons. 
    -ConfiguracionRest.java -> Sirve para indicar la dirección a la que hay que buscar la bbdd.

El código del servidor REST está en EsqueletoWebAppEnPHPConSesion/src/rest:
    -mediciones.php -> Endpoint GET/POST /mediciones (también accesible como /rest/mediciones.php).

El código de la lógica de negocio está en EsqueletoWebAppEnPHPConSesion/src/logica:
    -mediciones.php -> guardarMediciones(tipo, valor) y mostrarMediciones().

La lógica fake web está en EsqueletoWebAppEnPHPConSesion/src/logicaFake:
    -PeticionarioREST.js -> pedirMediciones() llama a GET /rest/mediciones.php.

La UX del navegador está en EsqueletoWebAppEnPHPConSesion/src/ux:
    -Aplicacion.html y Aplicacion.js -> gráfico de mediciones almacenadas.

La app Android usa lógica fake y peticionario REST separados:
    -LogicaFake.java -> guardarMediciones(tipo, valor)
    -PeticionarioREST.java -> enviarMedicion(tipo, valor) con POST

La conexión Plesk está en EsqueletoWebAppEnPHPConSesion/src/BBDD:
    -ConexionMediciones.php
    -ConfiguracionProduccion.ejemplo.php (copiar a ConfiguracionProduccion.php en el servidor)

SQL en EsqueletoWebAppEnPHPConSesion/bbdd:
    -Estructura.sql, datos.sql, EstructuraPruebas.sql

Publica en Plesk el directorio src. Instrucciones en src/00-Leeme.txt.

# Flujo de funcionamiento
1. **Emisión BLE (Arduino):** El microcontrolador ejecuta el bucle en `HolaMundoIBeacon.ino`, obtiene las medidas con `Medidor.h` y las publica mediante `Publicador.h` y `EmisoraBLE.h` emitiendo tramas iBeacon periódicas (codificando el tipo y contador en el Major, y el valor en el Minor).
2. **Recepción y reenvío (App Android):** El servicio en segundo plano `ServicioEscuharBeacons` escanea y filtra los beacons "GTI-3A". Al recibir una medida, decodifica los datos con `TramaIBeacon.java` y realiza una petición HTTP POST en formato JSON usando `PeticionarioREST.java` hacia la URL del servidor Plesk definida en `ConfiguracionRest.java`.
3. **Servicio y almacenamiento (Servidor Plesk):** El endpoint en la carpeta `rest` recibe la petición, delega la validación en la capa `logica` y registra la medición en MySQL mediante la capa `BBDD`.
4. **Visualización (Página Web):** La página web consulta el endpoint REST mediante peticiones GET para obtener las últimas lecturas y mostrarlas de forma interactiva al usuario.
