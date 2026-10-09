# Lógica de negocio de interfaz

La fachada JavaScript que usa la UX web está implementada en `EsqueletoWebAppEnPHPConSesion/src/logicaFake/LogicaFake.js`. Su contrato público es `mostrarMediciones()`; delega el transporte en `PeticionarioREST.js` y la UX no llama directamente al cliente HTTP.

Diseño: `doc/frontend_business_logic_design.md`.
