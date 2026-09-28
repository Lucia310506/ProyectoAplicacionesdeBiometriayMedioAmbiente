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

El código del cliente REST se encuentra en la carpeta rest:
    -mediciones.php -> Endpoint de la API REST que atiende peticiones HTTP (GET y POST) para consultar o registrar nuevas mediciones.
    -reglasREST.php -> Controla el enrutamiento de las peticiones REST y genera las respuestas en formato JSON.

El código de la lógica de negocio se encuentra en la carpeta logica:
    -Logica.php -> Implementa las reglas de negocio reales, validando las lecturas recibidas y gestionando el almacenamiento y consulta a través de la base de datos.

El código de la lógica fake se encuentra en la carpeta logicaFake: 
    -LogicaFake.php -> Simula la lógica de negocio devolviendo datos predefinidos de prueba sin requerir conexión a la base de datos real.

El código de la conexión con la BBDD se encuentra en la carpeta BBDD: 
    -Conexion.php -> Establece y gestiona la conexión con la base de datos MySQL/MariaDB en el servidor Plesk.
    -ConsultasBBDD.php -> Contiene las funciones con consultas preparadas (INSERT y SELECT) para interactuar con la tabla de mediciones.
    -esquema.sql -> Script SQL con la definición de la base de datos y la tabla donde se almacenan las medidas (tipo, valor, fecha/hora).

El código de la página web se encuentra en la carpeta web:
    -index.html -> Interfaz de usuario para visualizar las medidas de temperatura y CO2 desde el navegador.
    -estilos.css -> Estilos visuales para la presentación gráfica de los datos y tarjetas de medición.
    -app.js -> Script JavaScript que realiza peticiones periódicas (GET) al servicio REST para actualizar las mediciones en tiempo real.

# Flujo de funcionamiento
1. **Emisión BLE (Arduino):** El microcontrolador ejecuta el bucle en `HolaMundoIBeacon.ino`, obtiene las medidas con `Medidor.h` y las publica mediante `Publicador.h` y `EmisoraBLE.h` emitiendo tramas iBeacon periódicas (codificando el tipo y contador en el Major, y el valor en el Minor).
2. **Recepción y reenvío (App Android):** El servicio en segundo plano `ServicioEscuharBeacons` escanea y filtra los beacons "GTI-3A". Al recibir una medida, decodifica los datos con `TramaIBeacon.java` y realiza una petición HTTP POST en formato JSON usando `PeticionarioREST.java` hacia la URL del servidor Plesk definida en `ConfiguracionRest.java`.
3. **Servicio y almacenamiento (Servidor Plesk):** El endpoint en la carpeta `rest` recibe la petición, delega la validación en la capa `logica` y registra la medición en MySQL mediante la capa `BBDD`.
4. **Visualización (Página Web):** La página web consulta el endpoint REST mediante peticiones GET para obtener las últimas lecturas y mostrarlas de forma interactiva al usuario.
