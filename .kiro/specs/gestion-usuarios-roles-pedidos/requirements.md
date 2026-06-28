# Requirements Document

## Introduction

Este documento cubre los módulos faltantes del Sistema de Inventario en Línea para Pequeños Negocios (backend Spring Boot 3). Específicamente aborda tres áreas:

1. **Gestión de Usuarios/Empleados**: CRUD de usuarios dentro de un negocio, administrado por el ADMIN.
2. **Roles y Permisos Granulares**: Aplicación efectiva del enum `Rol` (ADMIN / EMPLEADO) mediante `@PreAuthorize` en todos los endpoints existentes y nuevos.
3. **Pedidos a Proveedores**: Ciclo de vida de pedidos pendientes a proveedores (creación, seguimiento, recepción y cancelación).

El sistema ya cuenta con autenticación JWT, entidad `Usuario` con campo `rol` y `activo`, entidad `Negocio` y el directorio de `Proveedor`. Estos requisitos extienden lo existente sin reemplazarlo.

---

## Glossary

- **Sistema**: El backend Spring Boot del Sistema de Inventario en Línea.
- **ADMIN**: Usuario con rol `ADMIN`. Dueño o administrador de un negocio. Tiene acceso completo a la gestión de su negocio.
- **EMPLEADO**: Usuario con rol `EMPLEADO`. Pertenece a un negocio y tiene acceso restringido a consultar productos y registrar movimientos de stock.
- **Usuario_Autenticado**: Cualquier usuario que ha iniciado sesión y porta un JWT válido.
- **Negocio**: Entidad que agrupa productos, categorías, proveedores, usuarios y pedidos bajo un negocio específico.
- **Pedido**: Solicitud formal de reabastecimiento enviada a un `Proveedor` que contiene uno o más `ItemPedido`.
- **ItemPedido**: Línea dentro de un `Pedido` que especifica un `Producto`, la cantidad solicitada y el precio unitario acordado.
- **GestorUsuarios**: Componente del Sistema responsable del CRUD de usuarios dentro de un negocio.
- **GestorPermisos**: Componente del Sistema responsable de evaluar el rol del `Usuario_Autenticado` antes de ejecutar cada operación protegida.
- **GestorPedidos**: Componente del Sistema responsable del ciclo de vida de los `Pedido`s a proveedores.
- **EstadoPedido**: Enumeración con los valores `PENDIENTE`, `RECIBIDO` y `CANCELADO`.

---

## Requirements

### Requirement 1: Creación de empleados por el ADMIN

**User Story:** As an ADMIN, I want to create employee accounts within my business, so that my staff can access the system with restricted permissions.

#### Acceptance Criteria

1. WHEN an ADMIN sends a valid employee creation request with `username`, `password`, `email` and `nombre` for a `Negocio` that the ADMIN owns, THE GestorUsuarios SHALL create a `Usuario` with `rol = EMPLEADO`, `activo = true`, and assign it to that `Negocio`.
2. IF the `username` provided in a creation request already exists in the system, THEN THE GestorUsuarios SHALL return HTTP 409 with a descriptive error message.
3. IF the ADMIN sends an employee creation request for a `Negocio` that the ADMIN does not own, THEN THE GestorUsuarios SHALL return HTTP 403.
4. THE GestorUsuarios SHALL encode the `password` of every new `Usuario` using BCrypt before persisting it.
5. WHEN an EMPLEADO sends a request to create a `Usuario`, THE GestorPermisos SHALL reject the request and return HTTP 403.

---

### Requirement 2: Consulta y listado de usuarios por negocio

**User Story:** As an ADMIN, I want to list all users belonging to my business, so that I can monitor who has access to the system.

#### Acceptance Criteria

1. WHEN an ADMIN requests the list of users for a `Negocio` that the ADMIN owns, THE GestorUsuarios SHALL return the list of all `Usuario` records associated with that `Negocio`, including `id`, `username`, `email`, `nombre`, `rol` and `activo`.
2. IF an ADMIN requests the list of users for a `Negocio` that the ADMIN does not own, THEN THE GestorUsuarios SHALL return HTTP 403.
3. WHEN an ADMIN requests the details of a single `Usuario` by ID, and that `Usuario` belongs to a `Negocio` owned by the ADMIN, THE GestorUsuarios SHALL return the full details of that `Usuario`.
4. IF the requested `Usuario` ID does not exist, THEN THE GestorUsuarios SHALL return HTTP 404 with a descriptive error message.
5. WHEN an EMPLEADO sends a request to list or view users, THE GestorPermisos SHALL reject the request and return HTTP 403.

---

### Requirement 3: Actualización de datos y rol de un usuario

**User Story:** As an ADMIN, I want to update the role, name, and email of a user in my business, so that I can manage their access level and personal data.

#### Acceptance Criteria

1. WHEN an ADMIN sends a valid update request for a `Usuario` that belongs to a `Negocio` owned by the ADMIN, THE GestorUsuarios SHALL update the `email`, `nombre`, and `rol` fields of that `Usuario` and return the updated record.
2. IF the ADMIN sends an update request that would change the `rol` of the last active ADMIN of a `Negocio` to `EMPLEADO`, THEN THE GestorUsuarios SHALL reject the request with HTTP 422 and the message "No es posible degradar al único ADMIN activo del negocio".
3. IF an ADMIN sends an update request for a `Usuario` that belongs to a `Negocio` that the ADMIN does not own, THEN THE GestorUsuarios SHALL return HTTP 403.
4. WHEN an EMPLEADO sends a request to update any `Usuario`, THE GestorPermisos SHALL reject the request and return HTTP 403.

---

### Requirement 4: Activación y desactivación de usuarios

**User Story:** As an ADMIN, I want to activate or deactivate employee accounts, so that I can revoke or restore access without deleting the account history.

#### Acceptance Criteria

1. WHEN an ADMIN sends a request to deactivate a `Usuario` that belongs to a `Negocio` owned by the ADMIN, THE GestorUsuarios SHALL set `activo = false` for that `Usuario`.
2. WHEN an ADMIN sends a request to activate a `Usuario` that belongs to a `Negocio` owned by the ADMIN, THE GestorUsuarios SHALL set `activo = true` for that `Usuario`.
3. IF the `Usuario` to be deactivated is the last active ADMIN of a `Negocio`, THEN THE GestorUsuarios SHALL reject the request with HTTP 422 and the message "No es posible desactivar al único ADMIN activo del negocio".
4. WHILE a `Usuario` has `activo = false`, THE Sistema SHALL reject any login attempt by that `Usuario` with HTTP 401 and the message "Cuenta desactivada".
5. IF an ADMIN sends an activation/deactivation request for a `Usuario` that belongs to a `Negocio` that the ADMIN does not own, THEN THE GestorUsuarios SHALL return HTTP 403.

---

### Requirement 5: Permisos granulares — Productos, Categorías y Proveedores

**User Story:** As a business owner, I want only ADMINs to be able to create, update, or delete products, categories, and suppliers, so that employees cannot accidentally corrupt catalog data.

#### Acceptance Criteria

1. WHEN an ADMIN sends a POST, PUT, or DELETE request to `/api/productos`, `/api/categorias`, or `/api/proveedores`, THE GestorPermisos SHALL allow the request to proceed.
2. WHEN an EMPLEADO sends a POST, PUT, or DELETE request to `/api/productos`, `/api/categorias`, or `/api/proveedores`, THE GestorPermisos SHALL reject the request with HTTP 403.
3. WHEN an EMPLEADO sends a GET request to `/api/productos` or `/api/categorias` for a `Negocio` that the EMPLEADO belongs to, THE GestorPermisos SHALL allow the request to proceed.
4. WHEN an ADMIN or EMPLEADO sends a GET request to any endpoint of `/api/productos`, `/api/categorias`, or `/api/proveedores` with a valid JWT, THE GestorPermisos SHALL allow the read operation.
5. IF a request reaches any protected endpoint without a valid JWT, THEN THE GestorPermisos SHALL return HTTP 401.

---

### Requirement 6: Permisos granulares — Movimientos de stock

**User Story:** As a business owner, I want both ADMINs and EMPLEADOs to be able to register stock movements, so that employees can operate the inventory without needing elevated permissions.

#### Acceptance Criteria

1. WHEN an ADMIN or EMPLEADO sends a POST request to `/api/movimientos` with valid data and a valid JWT, THE GestorPermisos SHALL allow the request to proceed.
2. WHEN an ADMIN or EMPLEADO sends a GET request to `/api/movimientos` with a valid JWT, THE GestorPermisos SHALL allow the request to proceed.
3. WHEN an EMPLEADO sends a DELETE request to `/api/movimientos`, THE GestorPermisos SHALL reject the request with HTTP 403.
4. WHEN an ADMIN sends a DELETE request to `/api/movimientos` with a valid JWT, THE GestorPermisos SHALL allow the request to proceed.

---

### Requirement 7: Permisos granulares — Negocios

**User Story:** As a platform administrator, I want only ADMINs to be able to create, update, or delete businesses, so that employees cannot create or remove business entities.

#### Acceptance Criteria

1. WHEN an ADMIN sends a POST, PUT, or DELETE request to `/api/negocios`, THE GestorPermisos SHALL allow the request to proceed.
2. WHEN an EMPLEADO sends a POST, PUT, or DELETE request to `/api/negocios`, THE GestorPermisos SHALL reject the request with HTTP 403.
3. WHEN an ADMIN or EMPLEADO sends a GET request to `/api/negocios` with a valid JWT, THE GestorPermisos SHALL allow the request.

---

### Requirement 8: Creación de pedidos a proveedores

**User Story:** As an ADMIN, I want to create purchase orders to suppliers, so that I can track pending replenishment requests for my business.

#### Acceptance Criteria

1. WHEN an ADMIN sends a valid POST request to create a `Pedido` specifying a `Proveedor` that belongs to the ADMIN's `Negocio`, at least one `ItemPedido` with a positive `cantidad` and a non-negative `precioUnitario`, THE GestorPedidos SHALL persist the `Pedido` with `estado = PENDIENTE` and `fechaPedido` equal to the current timestamp, and return HTTP 201 with the created `Pedido`.
2. IF the `Proveedor` specified in the creation request does not belong to the ADMIN's `Negocio`, THEN THE GestorPedidos SHALL return HTTP 403.
3. IF the `Producto` in any `ItemPedido` does not belong to the same `Negocio` as the `Pedido`, THEN THE GestorPedidos SHALL return HTTP 422 with a descriptive error message.
4. IF the `cantidad` in any `ItemPedido` is zero or negative, THEN THE GestorPedidos SHALL return HTTP 400 with a descriptive error message.
5. WHEN an EMPLEADO sends a request to create a `Pedido`, THE GestorPermisos SHALL reject the request with HTTP 403.

---

### Requirement 9: Consulta de pedidos a proveedores

**User Story:** As an ADMIN, I want to list and filter purchase orders for my business, so that I can monitor the status of pending replenishments.

#### Acceptance Criteria

1. WHEN an ADMIN requests the list of `Pedido`s for a `Negocio` that the ADMIN owns, THE GestorPedidos SHALL return all `Pedido`s for that `Negocio`, each including `id`, `proveedor`, `estado`, `fechaPedido`, `fechaRecepcion`, and the list of `ItemPedido`s.
2. WHEN an ADMIN requests the list of `Pedido`s filtered by `estado`, THE GestorPedidos SHALL return only the `Pedido`s with the matching `estado` for that `Negocio`.
3. WHEN an ADMIN requests a single `Pedido` by ID that belongs to the ADMIN's `Negocio`, THE GestorPedidos SHALL return the full details of that `Pedido`.
4. IF the requested `Pedido` ID does not exist, THEN THE GestorPedidos SHALL return HTTP 404 with a descriptive error message.
5. IF an ADMIN requests `Pedido`s for a `Negocio` that the ADMIN does not own, THEN THE GestorPedidos SHALL return HTTP 403.

---

### Requirement 10: Recepción de pedidos

**User Story:** As an ADMIN, I want to mark a purchase order as received, so that the system automatically registers the incoming stock movements for each ordered item.

#### Acceptance Criteria

1. WHEN an ADMIN sends a PATCH request to mark a `Pedido` with `estado = PENDIENTE` as received, THE GestorPedidos SHALL set `estado = RECIBIDO` and `fechaRecepcion` to the current timestamp, and SHALL create one `Movimiento` of type `ENTRADA` per `ItemPedido` using the `cantidad` of each item.
2. IF the `Pedido` to be received has `estado = RECIBIDO` or `estado = CANCELADO`, THEN THE GestorPedidos SHALL return HTTP 422 with the message "El pedido no está en estado PENDIENTE".
3. IF the `Pedido` to be received belongs to a `Negocio` that the ADMIN does not own, THEN THE GestorPedidos SHALL return HTTP 403.
4. THE GestorPedidos SHALL perform the state update and all `Movimiento` creations within a single database transaction, so that a failure in any step leaves the data unchanged.

---

### Requirement 11: Cancelación de pedidos

**User Story:** As an ADMIN, I want to cancel a pending purchase order, so that I can remove orders that will no longer be fulfilled without affecting stock levels.

#### Acceptance Criteria

1. WHEN an ADMIN sends a PATCH request to cancel a `Pedido` with `estado = PENDIENTE`, THE GestorPedidos SHALL set `estado = CANCELADO` and SHALL NOT create any `Movimiento`.
2. IF the `Pedido` to be cancelled has `estado = RECIBIDO` or `estado = CANCELADO`, THEN THE GestorPedidos SHALL return HTTP 422 with the message "Solo se pueden cancelar pedidos en estado PENDIENTE".
3. IF the `Pedido` to be cancelled belongs to a `Negocio` that the ADMIN does not own, THEN THE GestorPedidos SHALL return HTTP 403.

---

### Requirement 12: Aislamiento de datos por negocio

**User Story:** As a business owner, I want my business data to be isolated from other businesses, so that users of one business cannot access or modify data from another business.

#### Acceptance Criteria

1. WHILE a `Usuario_Autenticado` is making any request, THE Sistema SHALL only return or modify resources (`Producto`, `Categoria`, `Proveedor`, `Pedido`, `Movimiento`, `Usuario`) that belong to the `Negocio` associated with the `Usuario_Autenticado`'s JWT.
2. IF a `Usuario_Autenticado` sends a request that references a resource ID belonging to a different `Negocio`, THEN THE Sistema SHALL return HTTP 403.
3. THE Sistema SHALL include the `negocioId` claim in every JWT generated during login or registration, and THE GestorPermisos SHALL use this claim to validate ownership on every protected request.
