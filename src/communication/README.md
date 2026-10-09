# Comunicación

Este componente contiene la frontera HTTP del proyecto. Su implementación de servidor se conserva en `EsqueletoWebAppEnPHPConSesion/src/rest/mediciones.php`, porque Apache publica esa aplicación de forma independiente. El cliente REST Android vive en `BTLEAlumnos2021hechoapp2/app/src/main/java/com/example/ldiamur/btlealumnos2021app/PeticionarioREST.java` y el cliente REST web en `EsqueletoWebAppEnPHPConSesion/src/logicaFake/PeticionarioREST.js`.

Los contratos están descritos en `doc/communication_design.md`, `doc/web_rest_design.md` y `doc/web_rest_client_design.md`.
