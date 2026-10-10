# Lógica de negocio de interfaz

La fachada JavaScript que consume la UX está implementada en `LogicaFake.js`. Su contrato público es `mostrarMediciones()`; delega el transporte en `../communication/PeticionarioREST.js`, y la UX no llama directamente al cliente HTTP.

Diseño: `doc/frontend_business_logic_design.md`.
