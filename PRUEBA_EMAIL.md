# 🧪 Guía de Prueba Rápida - Sistema de Emails

## ✅ Configuración completada

- ✅ Email configurado: `proyectoruiz10@gmail.com`
- ✅ App Password generada y configurada
- ✅ Backend listo para enviar emails

---

## 🚀 Prueba Paso a Paso

### 1️⃣ Inicia el backend

```bash
cd "c:\Users\DELL\Desktop\PROYECTO DE RUIZ BACK"
mvn spring-boot:run
```

Espera a ver el mensaje:
```
Started RuizApiApplication in X.XXX seconds
```

---

### 2️⃣ Abre Swagger UI

Ve a: http://localhost:8080/swagger-ui.html

---

### 3️⃣ Autentícate

1. **Login:**
   - Busca el endpoint: `POST /api/auth/login`
   - Click en "Try it out"
   - Usa tus credenciales:
     ```json
     {
       "username": "tu-usuario",
       "password": "tu-password"
     }
     ```
   - Click "Execute"
   - Copia el **token** de la respuesta

2. **Autorizar:**
   - Click en el botón verde "🔓 Authorize" (arriba a la derecha)
   - Pega el token (sin "Bearer", solo el token)
   - Click "Authorize"
   - Click "Close"

---

### 4️⃣ Asegúrate de tener un proveedor con email

1. **Ver proveedores:**
   - Busca: `GET /api/proveedores/negocio/{negocioId}`
   - Usa tu `negocioId`
   - Verifica que al menos un proveedor tenga **email configurado**

2. **Si no tiene email, agrégalo:**
   - Busca: `PUT /api/proveedores/{id}`
   - Actualiza el proveedor agregando un email de prueba
   - Ejemplo:
     ```json
     {
       "nombre": "Proveedor Test",
       "email": "tu-email-prueba@gmail.com",
       "telefono": "555-1234",
       "contacto": "Juan Pérez",
       "negocioId": 1
     }
     ```

---

### 5️⃣ Crea un pedido de prueba

1. **Busca:** `POST /api/pedidos`
2. **Datos de ejemplo:**
   ```json
   {
     "descripcion": "Pedido de prueba para email",
     "cantidad": 10,
     "precioUnitario": 50.00,
     "fechaEsperada": "2026-07-15",
     "notas": "Este es un pedido de prueba para verificar el envío de emails",
     "proveedorId": 1,
     "productoId": 1,
     "usuarioId": 1
   }
   ```
3. **Copia el ID** del pedido creado (ej: `5`)

---

### 6️⃣ Envía el email 📧

1. **Busca:** `POST /api/pedidos/{id}/enviar-email`
2. **Ingresa el ID** del pedido que creaste
3. **Click "Execute"**

---

### 7️⃣ Verifica el resultado

**En Swagger:**
- Deberías ver respuesta **200 OK**
- Mensaje: `"Email enviado exitosamente a [Nombre del Proveedor]"`

**En la consola del backend:**
- Busca el log:
  ```
  Email enviado exitosamente al proveedor: Proveedor Test (email@example.com)
  ```

**En tu bandeja de entrada:**
- Ve al email del proveedor que configuraste
- **Revisa la bandeja de entrada Y la carpeta de SPAM**
- Deberías ver un email profesional con:
  - Header con gradiente morado
  - "🛒 Nuevo Pedido"
  - Todos los detalles del pedido
  - Diseño responsive y profesional

---

## 🐛 Solución de problemas

### ❌ Error: "Username and Password not accepted"

**Problema:** Credenciales incorrectas

**Solución:**
1. Verifica que el App Password esté correcto en `application.properties`
2. Sin espacios: `ebihuaclbascybzg` (todo junto)
3. Asegúrate de que la verificación en 2 pasos esté activa en tu cuenta de Gmail

---

### ❌ Error: "El proveedor no tiene email configurado"

**Problema:** El proveedor no tiene email

**Solución:**
1. Actualiza el proveedor con un email válido
2. Usa `PUT /api/proveedores/{id}` para agregar el email

---

### ❌ El email no llega

**Solución:**
1. ✅ Revisa la carpeta de **SPAM**
2. ✅ Verifica que el email del proveedor esté correcto
3. ✅ Comprueba los logs del backend para confirmar que se envió
4. ✅ Intenta con otro email (Gmail, Outlook, etc.)

---

### ❌ Error: "Could not connect to SMTP host"

**Problema:** Conexión bloqueada

**Solución:**
1. Verifica tu conexión a internet
2. Asegúrate de que el puerto 587 no esté bloqueado
3. Desactiva temporalmente el firewall/antivirus

---

## 📸 Cómo debería verse el email

El proveedor recibirá un email con:

```
┌─────────────────────────────────────┐
│  🛒 Nuevo Pedido                    │
│  [Tu Negocio]                       │
│  [Gradiente morado]                 │
├─────────────────────────────────────┤
│  📋 Información del Proveedor       │
│  Proveedor: [Nombre]                │
│  Contacto: [Contacto]               │
│  Teléfono: [Teléfono]               │
├─────────────────────────────────────┤
│  📦 Detalles del Pedido             │
│  Producto: [Nombre]                 │
│  Descripción: [Texto]               │
│  Cantidad: 10 unidades              │
│  Precio Unitario: $50.00            │
│  Total: $500.00                     │
│  Estado: [PENDIENTE 🟡]             │
│  Fecha del Pedido: [Fecha]          │
│  Fecha Esperada: [Fecha]            │
├─────────────────────────────────────┤
│  💬 Notas Adicionales               │
│  [Tus notas]                        │
├─────────────────────────────────────┤
│  Footer con info del negocio        │
└─────────────────────────────────────┘
```

---

## 🎯 Resultado esperado

✅ Email enviado correctamente
✅ Diseño profesional y responsive
✅ Toda la información visible
✅ Sin errores en los logs

---

## 📝 Después de probar

Si todo funciona correctamente:

1. ✅ Puedes usar esta funcionalidad en producción
2. ✅ El frontend puede llamar al endpoint sin problemas
3. ✅ Los proveedores recibirán notificaciones automáticas

Si necesitas mayor volumen de emails (más de 500/día):
- Considera migrar a **SendGrid** (100 gratis/día, luego $15/mes)
- O **Amazon SES** ($0.10 por 1,000 emails)

---

**¡Listo para probar!** 🚀
