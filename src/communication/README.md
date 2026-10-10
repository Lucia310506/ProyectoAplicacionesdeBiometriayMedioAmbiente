# Comunicación

Este componente contiene las fronteras de transporte. El endpoint PHP `mediciones.php` y el cliente web `PeticionarioREST.js` se encuentran en esta carpeta. El endpoint valida la petición y delega en `src/business_logic/mediciones.php`; el cliente web ofrece `pedirMediciones()` a la lógica frontend.

El cliente REST Android vive en `BTLEAlumnos2021hechoapp2/app/src/main/java/com/example/ldiamur/btlealumnos2021app/PeticionarioREST.java`.

Diseños: `doc/communication_design.md`, `doc/web_rest_design.md` y `doc/web_rest_client_design.md`.
