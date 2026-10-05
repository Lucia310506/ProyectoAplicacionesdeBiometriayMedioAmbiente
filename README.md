# Proyecto de biometría y medio ambiente

## De qué va el proyecto

El proyecto mide CO2 y temperatura. El Arduino manda las mediciones por Bluetooth, la aplicación Android las recibe y las guarda en una base de datos MySQL usando el servidor PHP. La página web muestra las mediciones guardadas.

## Carpetas del proyecto

- HolaMundoIBeacon: programa del Arduino y pruebas de inicio.
- BTLEAlumnos2021hechoapp2: aplicación Android.
- EsqueletoWebAppEnPHPConSesion: página web, servidor PHP y base de datos.
- doc: diseños y documentación de las partes del proyecto.

Dentro de la carpeta del servidor:

- src/rest: rutas REST para guardar y consultar mediciones.
- src/logica: reglas para validar y guardar las mediciones.
- src/BBDD: conexión y configuración de la base de datos.
- src/logicaFake: cliente REST que usa la web.
- src/ux: página web.
- src/tests: pruebas del proyecto.
- bbdd: archivos para crear y rellenar las tablas de MySQL.

## Cómo funciona

1. El Arduino prepara una medición y la manda por Bluetooth.
2. Android recibe el beacon y comprueba sus datos.
3. Android manda el tipo y el valor al servidor.
4. PHP comprueba los datos y los guarda en MySQL.
5. La web pide las mediciones al servidor y las muestra.

## Arduino

1. Abre HolaMundoIBeacon.ino con Arduino IDE.
2. Selecciona la placa del proyecto y comprueba que tienes instalada la librería Bluefruit.
3. Compila y carga el programa.
4. Abre el monitor serie a 115200 baudios. Al iniciar, se ejecutan una vez las pruebas de test.h. Verás EJECUTAR TESTS ARDUINO y el resultado.
5. Cuando terminan las pruebas, el Arduino empieza a enviar los beacons.

## Android

1. Abre BTLEAlumnos2021hechoapp2 en Android Studio y espera a que termine de preparar el proyecto.
2. Si usas otro servidor, cambia la dirección en ConfiguracionRest.java.
3. Ejecuta la aplicación en un móvil compatible y acepta los permisos que pide.

Al abrirse, la aplicación ejecuta las pruebas de la lógica y comprueba la conexión REST. Los resultados aparecen en Logcat. Puedes buscar TESTS_APP, TEST_LOGICAFake o TEST_PETICIONARIO_REST.

También hay pruebas en app/src/androidTest. PeticionarioRESTTest necesita MockWebServer, que ahora mismo no está añadido en app/build.gradle.kts.

## Web y base de datos

### Crear la base de datos

1. Crea una base de datos en MySQL.
2. Ejecuta EsqueletoWebAppEnPHPConSesion/bbdd/Estructura.sql para crear la tabla.
3. Si quieres añadir mediciones de ejemplo, ejecuta también bbdd/datos.sql.

### Configurar la conexión

En src/BBDD está el archivo ConfiguracionProduccion.txt. Ahí tienes una plantilla con los datos que necesita la conexión. Rellénala con los datos de tu servidor MySQL.

El servidor PHP no lee el archivo de texto. Tienes que crear en esa misma carpeta un archivo llamado ConfiguracionProduccion.php con esos datos. La estructura que debe tener es:

    <?php
    return [
        'host' => 'ESCRIBE_EL_HOST',
        'base' => 'ESCRIBE_EL_NOMBRE_DE_LA_BASE',
        'usuario' => 'ESCRIBE_EL_USUARIO',
        'password' => 'ESCRIBE_LA_CONTRASEÑA'
    ];

ConfiguracionProduccion.php está ignorado para que no se publiquen tus claves. Por eso no aparecerá al subir el proyecto y tendrás que crearlo también en el servidor. No pongas contraseñas reales en el archivo .txt ni lo subas a un sitio público.

### Subir la web

1. Copia el contenido de EsqueletoWebAppEnPHPConSesion al servidor.
2. Crea ConfiguracionProduccion.php con los datos de la base de datos del servidor.
3. Comprueba que el servidor tiene PHP, PDO MySQL y Apache con mod_rewrite.
4. Abre src/ux/Aplicacion.html. La ruta para consultar y guardar mediciones es /mediciones.
5. En Android, pon la dirección completa del servidor en ConfiguracionRest.java.

La web consulta con GET. Android guarda una medición con POST. Si se guarda bien, el servidor responde con el código 201.

## Pruebas

### Al abrir la web

Cuando abres Aplicacion.html, la web ejecuta las pruebas y luego empieza a consultar las mediciones. Abre la consola del navegador para ver EJECUTAR TESTS, el resultado de cada prueba y el resumen.

Las pruebas comprueban el cliente REST de la web, la lógica PHP y la conexión con la base de datos. Las pruebas de base de datos guardan dos mediciones temporales y luego borran solo esas dos. No borran las mediciones normales.

### Pruebas PHP

Hay dos pruebas para ejecutar aparte: probar_base_datos.php y probar_rest.php. Estas pruebas borran todas las filas de la tabla mediciones antes y después. Úsalas solo con una base de datos de pruebas vacía. No las ejecutes con la base de datos que usa la web o que tiene mediciones que quieras conservar.

### Pruebas JavaScript

Los archivos probar_peticionario_rest.js y probar_ux.js comprueban partes de la web con respuestas de ejemplo. No necesitan conectarse a MySQL.

## Diseños

En doc están los diseños de las partes del proyecto. component_map.md muestra cómo se relacionan los diseños con las carpetas y archivos.