# Diseño de la lógica fake Android

## Component Design

### Modelo compartido

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`. No se crea clase `Medicion`. Se pasan `tipo` y `valor`; el servidor crea `id` y `fecha`.

### Lógica fake y cliente REST

```text
tipo: Text, valor: R --> LogicaFake.guardarMediciones() -->
                         validación de dominio (sin transporte)

tipo: Text, valor: R --> PeticionarioREST.enviarMedicion() --> POST /mediciones
GET /mediciones        --> PeticionarioREST.hacerPeticionREST() --> callback HTTP
```

`LogicaFake.java` contiene solo la validación de dominio. `PeticionarioREST.java` construye/ejecuta HTTP y ofrece `enviarMedicion(tipo, valor)`; `ServicioEscuharBeacons.java` coordina validación y envío. `ConfiguracionRest.java` centraliza la URL completa del endpoint para cambiar el servidor sin alterar la lógica.

### Interpretación de la separación

`LogicaFake` implementa validación independiente de transporte. `PeticionarioREST` es la infraestructura HTTP; sus callbacks HTTP pertenecen al cliente, no al contrato de negocio. El servicio BLE conserva su escucha y utiliza ambos componentes secuencialmente.

### Ubicación de implementación

`BTLEAlumnos2021hechoapp2/app/src/main/java/com/example/ldiamur/btlealumnos2021app/LogicaFake.java`, `PeticionarioREST.java` y `ServicioEscuharBeacons.java`.

### Diseño global de las clases Android

`LogicaFake` valida las mediciones, `PeticionarioREST` realiza la comunicación HTTP, `ServicioEscuharBeacons` controla el escaneo BLE y conecta el beacon con la lógica/REST, y `MainActivity` gestiona permisos, botones y la ejecución manual de pruebas. `ConfiguracionRest` centraliza la URL. No se mantiene una lista local de mediciones: `id` y `fecha` proceden del servidor.

Convención del diagrama de clase: los campos privados quedan completamente dentro del contorno. Las operaciones públicas se dibujan fuera y entran por una abertura del borde; el borde no se cierra atravesando la operación. Los diagramas siguientes muestran relaciones entre clases; las tablas identifican la visibilidad al describir cada firma.

```text
MainActivity --inicia/detiene--> ServicioEscuharBeacons
ServicioEscuharBeacons --valida--> LogicaFake.guardarMediciones()
ServicioEscuharBeacons --envía tras validar--> PeticionarioREST.enviarMedicion()
PeticionarioREST --lee URL de--> ConfiguracionRest.URL_MEDICIONES
MainActivity --activa manualmente--> PruebasRESTBoton
```

El siguiente es el diseño de clase de la lógica Android. La operación es pública y estática; por eso aparece en la pared de la clase y lleva `--x`. No hay campos de instancia privados.

```text
                           -------- LogicaFake --------
                           |
                           |
tipo: Text, valor: R --> | guardarMediciones() --x
                           |
                           --------------------------------
```

### Clases y funciones

### Flujo de lectura BLE a petición REST

```text
ScanResult
  → descartar si falta el registro o tiene menos de 30 bytes
  → TramaIBeacon(bytes)
  → major = bytes Major como entero
  → tipoMedida = major >> 8
  → contador = major & 0xff
  → valor = bytes Minor como entero con signo
  → tipoMedida 11 → "CO2"; tipoMedida 12 → "TEMPERATURA"
  → descartar si tipo + contador coincide con el último anuncio enviado
  → LogicaFake.guardarMediciones(tipo, valor)
  → PeticionarioREST.enviarMedicion(tipo, valor)
```

Este contrato binario debe coincidir con `Publicador` del firmware. El servicio descarta tipos de beacon no reconocidos. Una validación fallida se captura y se registra en Logcat en lugar de iniciar el POST.

#### `LogicaFake`

Clase estática de validación y delegación. No almacena mediciones.

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `guardarMediciones(String tipo, double valor): void` | `tipo: Text, valor: R --> guardarMediciones() -->` | Acepta únicamente CO2/TEMPERATURA y valores finitos; si no, lanza `IllegalArgumentException`. No devuelve nada ni persiste localmente. |

#### `PeticionarioREST`

Cliente HTTP asíncrono basado en `AsyncTask`. Guarda temporalmente método, destino, cuerpo y callback de una petición.

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `hacerPeticionREST(String metodo, String urlDestino, String cuerpo, RespuestaREST callback): void` | `metodo: Text, URL: Text, cuerpo: Text, callback: CallbackREST --> hacerPeticionREST() -->` | Configura la petición y la ejecuta en segundo plano. |
| `PeticionarioREST()` | `--> PeticionarioREST() --> ClienteREST` | Crea el cliente con `HttpURLConnection` real. |
| `PeticionarioREST(AbridorConexion abridor)` | `abridor: AbridorConexion --> PeticionarioREST() --> ClienteREST` | Inyecta el transporte, lo que permite conexiones falsas en pruebas. |
| `crearCuerpoMedicion(String tipo, double valor): String` | `tipo: Text, valor: R --> crearCuerpoMedicion() --> JSON` | Construye JSON con exactamente `tipo` y `valor`. |
| `enviarMedicion(String tipo, double valor): void` | `tipo: Text, valor: R --> enviarMedicion() -->` | Envía POST a la URL configurada y registra éxito para HTTP 201 o el error recibido. |
| `doInBackground(Void... params): Boolean` | `params: Void[] --> doInBackground() --> B` | Abre conexión, fija método/cuerpo, lee estado y respuesta fuera del hilo principal. |
| `onPostExecute(Boolean correcto): void` | `correcto: B --> onPostExecute() -->` | Entrega estado y cuerpo al callback en el hilo de interfaz. |

#### `MainActivity`

Controla la pantalla Android, permisos de Bluetooth, servicio BLE y botón que activa las pruebas.

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `botonBuscarDispositivosBTLEPulsado(View): void` | `vista: View --> botonBuscarDispositivosBTLEPulsado() -->` | Arranca el servicio persistente de escucha BLE. |
| `botonBuscarNuestroDispositivoBTLEPulsado(View): void` | `vista: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->` | Arranca el servicio que busca el beacon esperado. |
| `botonDetenerBusquedaDispositivosBTLEPulsado(View): void` | `vista: View --> botonDetenerBusquedaDispositivosBTLEPulsado() -->` | Detiene el servicio de escucha. |
| `botonBuscarBeaconPruebaPulsado(View): void` | `vista: View --> botonBuscarBeaconPruebaPulsado() -->` | Inicia el escaneo temporal del beacon centinela y bloquea el botón mientras espera. |
| `actualizarResultadoBeaconPrueba(boolean): void` | `encontrado: B --> actualizarResultadoBeaconPrueba() -->` | Muestra si se recibió el beacon o venció el tiempo de búsqueda. |
| `onStart(): void` | `--> onStart() -->` | Registra el receptor interno del resultado de búsqueda. |
| `onStop(): void` | `--> onStop() -->` | Libera el receptor interno de resultados. |
| `onCreate(Bundle): void` | `estado: Bundle --> onCreate() -->` | Crea la pantalla y solicita los permisos necesarios. |
| `onRequestPermissionsResult(int, String[], int[]): void` | `codigo: N, permisos: Texto[], resultados: Z[] --> onRequestPermissionsResult() -->` | Inicializa Bluetooth solo si se conceden todos los permisos. |
| `iniciarServicioEscucha(): void` | `--> iniciarServicioEscucha() -->` | Construye el intent de inicio y arranca el servicio en primer plano. |
| `inicializarBlueTooth(): void` | `--> inicializarBlueTooth() -->` | Comprueba permisos y adaptador y habilita Bluetooth. |
| `permisosNecesarios(): String[]` | `--> permisosNecesarios() --> Texto[]` | Selecciona permisos según la versión Android. |
| `tengoLosPermisosNecesarios(): boolean` | `--> tengoLosPermisosNecesarios() --> B` | Comprueba que todos los permisos requeridos están concedidos. |
| `pedirPermisosNecesarios(): void` | `--> pedirPermisosNecesarios() -->` | Solicita permisos pendientes o inicializa Bluetooth. |
| `comprobarLogicaFake(): void` | `--> comprobarLogicaFake() --> B` | Ejecuta ejemplos válidos e inválidos de la lógica fake y registra resultados. |
| `comprobarRechazoLogica(String, double, String): void` | `tipo: Text, valor: R, nombre: Text --> comprobarRechazoLogica() -->` | Falla si la lógica acepta una entrada inválida; confirma el rechazo esperado. |
| `botonEjecutarTestsPulsado(View): void` | `vista: View --> botonEjecutarTestsPulsado() -->` | Ejecuta pruebas solo al pulsar, evita dobles ejecuciones y actualiza estado/botón. |
| `comprobarRest(): void` | `--> comprobarRest() -->` | Llama a la batería REST simulada y muestra el resumen. |
| `onCreate(Bundle): void` | `estado: Bundle --> onCreate() -->` | Crea la pantalla y solicita los permisos necesarios. |
| `onRequestPermissionsResult(int, String[], int[]): void` | `codigo: N, permisos: Texto[], resultados: Z[] --> onRequestPermissionsResult() -->` | Inicializa Bluetooth solo si todos los permisos solicitados se conceden. |

#### `ServicioEscuharBeacons`

Servicio Android en primer plano. Prepara el escáner BLE, valida permisos, interpreta anuncios iBeacon y envía mediciones nuevas.

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `onCreate(): void` | `--> onCreate() -->` | Crea canal/notificación y prepara el servicio. |
| `onStartCommand(Intent, int, int): int` | `intent: Intent, flags: N, startId: N --> onStartCommand() --> N` | Procesa la acción de inicio y comienza el escaneo. |
| `prepararEscaner(): void` | `--> prepararEscaner() -->` | Obtiene el escáner Bluetooth LE y sus dependencias. |
| `tengoPermisoEscaneo(): boolean` | `--> tengoPermisoEscaneo() --> B` | Comprueba el permiso necesario para escanear. |
| `iniciarEscaneo(): void` | `--> iniciarEscaneo() -->` | Inicia el escaneo y define callbacks de resultado, lote y error. |
| `iniciarBusquedaBeaconPrueba(): void` | `--> iniciarBusquedaBeaconPrueba() -->` | Activa el escaneo de prueba y programa el fin de búsqueda a los diez segundos. |
| `finalizarBusquedaBeaconPrueba(): void` | `--> finalizarBusquedaBeaconPrueba() -->` | Detiene el escaneo y comunica a la actividad si encontró el centinela. |
| `procesarBeacon(ScanResult): void` | `resultado: ScanResult --> procesarBeacon() -->` | Comprueba el anuncio iBeacon, extrae campos y descarta tramas no compatibles. |
| `enviarMedicionNueva(String, int, int, int): void` | `tipo: Text, valor: Z, tipoMedida: N, contador: N --> enviarMedicionNueva() -->` | Valida/identifica una lectura y delega el envío a la capa correspondiente. |
| `detenerEscaneo(): void` | `--> detenerEscaneo() -->` | Detiene el escáner activo y libera su callback. |
| `crearCanalNotificacion(): void` | `--> crearCanalNotificacion() -->` | Registra el canal requerido por la notificación persistente. |
| `iniciarPrimerPlano(): void` | `--> iniciarPrimerPlano() -->` | Construye la notificación y promueve el servicio a primer plano. |
| `onDestroy(): void` | `--> onDestroy() -->` | Detiene el escaneo y libera recursos al destruirse el servicio. |
| `onBind(Intent): IBinder` | `intent: Intent --> onBind() --> IBinder` | Devuelve el enlace del servicio; este servicio no ofrece una interfaz enlazable. |

#### `PruebasRESTBoton`

Contiene conexiones simuladas y una batería asíncrona GET, POST y error HTTP. `ConfiguracionRest` define `URL_MEDICIONES`; su constructor privado impide instancias.

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `ejecutar(ResultadoFinal): void` | `resultado: CallbackFinal --> ejecutar() -->` | Inicia la batería y registra su ejecución. |
| `comprobar(String, Caso, Bateria): void` | `nombre: Text, prueba: Caso, bateria: Bateria --> comprobar() -->` | Ejecuta un caso, cuenta éxito/error y permite seguir con los demás. |
| `Bateria.probarGet(): void` | `--> probarGet() -->` | Comprueba ruta, método, código y JSON GET simulados. |
| `Bateria.probarPost(): void` | `--> probarPost() -->` | Comprueba POST, JSON con tipo/valor y HTTP 201. |
| `Bateria.probarErrorHttp(): void` | `--> probarErrorHttp() -->` | Comprueba que el cliente comunica HTTP 500 y su cuerpo. |
| `Bateria.terminar(): void` | `--> terminar() -->` | Registra el resumen y llama al callback final. |
| `exigir(boolean, String): void` | `condicion: B, mensaje: Text --> exigir() -->` | Lanza error de aserción cuando no se cumple la condición. |
| `URL_MEDICIONES(): URL` | `--> URL_MEDICIONES() --> URL` | Convierte la dirección configurada a URL para conexiones falsas. |

#### Otras clases de apoyo

`ConfiguracionRest.URL_MEDICIONES` contiene la URL completa del endpoint; el constructor privado impide instancias. `TramaIBeacon` expone getters de campos del paquete publicitario y su constructor interpreta el byte array recibido. `Utilidades` es una colección estática de conversiones entre bytes, texto, UUID, hexadecimal y enteros; no mantiene estado.

| Clase/función | Diseño lógico | Qué hace |
|---|---|---|
| `ConfiguracionRest()` privado | `--> ConfiguracionRest() -->` | Impide instanciar la clase de configuración. |
| `TramaIBeacon(byte[] bytes)` | `bytes: Byte[] --> TramaIBeacon() --> Trama` | Interpreta los bytes del anuncio iBeacon y separa sus campos. |
| `TramaIBeacon.getPrefijo(): byte[]` | `trama: Trama --> getPrefijo() --> Byte[]` | Devuelve el prefijo del anuncio. |
| `TramaIBeacon.getUUID(): byte[]` | `trama: Trama --> getUUID() --> Byte[]` | Devuelve el UUID del beacon. |
| `TramaIBeacon.getMajor(): byte[]` | `trama: Trama --> getMajor() --> Byte[]` | Devuelve el campo Major. |
| `TramaIBeacon.getMinor(): byte[]` | `trama: Trama --> getMinor() --> Byte[]` | Devuelve el campo Minor. |
| `TramaIBeacon.getTxPower(): byte` | `trama: Trama --> getTxPower() --> Byte` | Devuelve la potencia Tx anunciada. |
| `TramaIBeacon.getLosBytes(): byte[]` | `trama: Trama --> getLosBytes() --> Byte[]` | Devuelve los bytes de la trama. |
| `TramaIBeacon.getAdvFlags(): byte[]` | `trama: Trama --> getAdvFlags() --> Byte[]` | Devuelve los flags del anuncio. |
| `TramaIBeacon.getAdvHeader(): byte[]` | `trama: Trama --> getAdvHeader() --> Byte[]` | Devuelve la cabecera del anuncio. |
| `TramaIBeacon.getCompanyID(): byte[]` | `trama: Trama --> getCompanyID() --> Byte[]` | Devuelve el identificador del fabricante. |
| `TramaIBeacon.getiBeaconType(): byte` | `trama: Trama --> getiBeaconType() --> Byte` | Devuelve el tipo iBeacon. |
| `TramaIBeacon.getiBeaconLength(): byte` | `trama: Trama --> getiBeaconLength() --> Byte` | Devuelve la longitud del bloque iBeacon. |
| `Utilidades.stringToBytes(String): byte[]` | `texto: Text --> stringToBytes() --> Byte[]` | Convierte texto en bytes. |
| `Utilidades.stringToUUID(String): UUID` | `textoUUID: Text --> stringToUUID() --> UUID` | Analiza una cadena UUID. |
| `Utilidades.uuidToString(UUID): String` | `uuid: UUID --> uuidToString() --> Text` | Convierte UUID a texto con formato. |
| `Utilidades.uuidToHexString(UUID): String` | `uuid: UUID --> uuidToHexString() --> Text` | Convierte UUID a representación hexadecimal. |
| `Utilidades.bytesToString(byte[]): String` | `bytes: Byte[] --> bytesToString() --> Text` | Interpreta bytes como texto. |
| `Utilidades.dosLongToBytes(long, long): byte[]` | `alto: Z, bajo: Z --> dosLongToBytes() --> Byte[]` | Combina dos valores long en su representación byte. |
| `Utilidades.bytesToInt(byte[]): int` | `bytes: Byte[] --> bytesToInt() --> Z` | Convierte bytes a entero. |
| `Utilidades.bytesToLong(byte[]): long` | `bytes: Byte[] --> bytesToLong() --> Z` | Convierte bytes a long. |
| `Utilidades.bytesToIntOK(byte[]): int` | `bytes: Byte[] --> bytesToIntOK() --> Z` | Convierte el tramo de bytes según el orden esperado por el protocolo. |
| `Utilidades.bytesToHexString(byte[]): String` | `bytes: Byte[] --> bytesToHexString() --> Text` | Representa un arreglo de bytes en hexadecimal. |

- `guardarMediciones(tipo, valor)` devuelve `void`, como indica la firma del diseño. Las entradas inválidas producen `IllegalArgumentException`; el servicio BLE captura el rechazo antes de enviar por REST.
- El cliente REST admite GET para consumidores que lo necesiten, pero la lógica fake Android no expone ni recibe callbacks HTTP. La aplicación actual usa el cliente para POST; los datos persistidos y la fecha se generan en el servidor.
- Las pruebas de `MainActivity` se ejecutan al pulsar el botón de la pantalla, no al iniciar la app. La lógica fake se comprueba en el hilo principal y los casos REST simulados corren en segundo plano; el resumen aparece en LogCat.
- El botón ejecuta la batería REST con conexiones simuladas: GET `/mediciones`, POST con solo tipo y valor, respuesta 201 y error HTTP; así no escribe en la base de datos real.
- `PeticionarioRESTTest` es una prueba instrumentada separada que usa MockWebServer para ejercitar el `HttpURLConnection` real contra localhost. Comprueba GET/POST, ruta, JSON, respuesta y callback en el hilo principal; requiere SDK Android y un emulador/teléfono conectado.

## Design Clarifications

- `LogicaFake` valida antes de enviar y no conserva mediciones; la URL se centraliza en `ConfiguracionRest`.
- Las pruebas Android se ejecutan mediante el botón y la prueba instrumentada usa MockWebServer local.

## General Rules

- **Programming Language:** Java para Android.
- **Function/Method Headers:** Cada función debe tener inmediatamente encima el diseño lógico en comentario delimitado por líneas `--------------------`.
- **Code Readability:** Comentarios adicionales breves si explican una decisión necesaria.
- **Automated Testing:** El botón usa conexiones simuladas para los casos rápidos; `PeticionarioRESTTest` usa MockWebServer local para verificar el cliente HTTP real.
