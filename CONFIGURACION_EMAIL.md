# 📧 Configuración de Emails con Gmail SMTP

Este documento explica cómo configurar el envío de emails a proveedores usando Gmail.

---

## 🔐 Paso 1: Obtener una App Password de Gmail

**Importante:** NO uses tu contraseña normal de Gmail. Necesitas una "App Password" específica.

### Instrucciones:

1. **Ir a tu cuenta de Google:**
   - Visita: https://myaccount.google.com/apppasswords
   - Inicia sesión con tu cuenta de Gmail

2. **Activar la verificación en 2 pasos (si no la tienes):**
   - Esto es **obligatorio** para crear App Passwords
   - Ve a: https://myaccount.google.com/security
   - Busca "Verificación en 2 pasos" y actívala

3. **Crear una App Password:**
   - Una vez activada la verificación en 2 pasos, vuelve a: https://myaccount.google.com/apppasswords
   - Selecciona:
     - **App:** "Correo"
     - **Dispositivo:** "Windows" o "Otro (nombre personalizado)"
   - Haz clic en "Generar"
   - **Copia la contraseña de 16 caracteres** que aparece (sin espacios)

---

## ⚙️ Paso 2: Configurar el Backend

### Opción A: Variables de entorno (Recomendado para producción)

Configura estas variables en tu sistema o en tu plataforma de deploy (Railway, Heroku, etc.):

```bash
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx   # Tu App Password de 16 dígitos
MAIL_FROM=tu-negocio@gmail.com      # Email que aparecerá como remitente
```

### Opción B: Archivo application.properties (Solo desarrollo local)

Edita `src/main/resources/application.properties`:

```properties
spring.mail.username=tu-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
app.mail.from=tu-negocio@gmail.com
```

⚠️ **NUNCA** subas este archivo a Git con credenciales reales. Usa variables de entorno en producción.

---

## 🚀 Paso 3: Usar la funcionalidad de emails

### Desde el frontend:

1. **Al crear un pedido:**
   - Marca el checkbox "📧 Enviar notificación por email"
   - El email se enviará automáticamente al crear el pedido

2. **Para pedidos existentes:**
   - Haz clic en el botón "📧 Reenviar email" en la vista de detalles del pedido

### Desde el backend (Swagger o Postman):

```http
POST /api/pedidos/{id}/enviar-email
Authorization: Bearer <tu-token-jwt>
```

**Respuesta exitosa (200):**
```json
"Email enviado exitosamente a Proveedor XYZ"
```

**Errores posibles:**
- `400`: El proveedor no tiene email configurado
- `404`: Pedido no encontrado
- `500`: Error al enviar el email (credenciales incorrectas, etc.)

---

## 📋 Requisitos para enviar emails

✅ El **proveedor** debe tener un email configurado
✅ Las credenciales de Gmail deben estar configuradas correctamente
✅ Solo usuarios con rol **ADMIN** pueden enviar emails

---

## 🔍 Verificar que funciona

1. **Inicia el backend:**
   ```bash
   mvn spring-boot:run
   ```

2. **Busca en los logs:**
   - Si todo está bien: `Email enviado exitosamente al proveedor: [Nombre]`
   - Si hay error: Verás el mensaje de error específico

3. **Revisa tu bandeja de spam:**
   - Los primeros emails pueden caer en spam
   - Márcalos como "No es spam" para futuros envíos

---

## 🐛 Solución de problemas

### Error: "Username and Password not accepted"

**Causa:** Credenciales incorrectas o no estás usando una App Password

**Solución:**
1. Verifica que estás usando una **App Password** (16 dígitos), no tu contraseña normal
2. Asegúrate de que la verificación en 2 pasos esté activa
3. Genera una nueva App Password

---

### Error: "Could not connect to SMTP host"

**Causa:** Problemas de red o firewall

**Solución:**
1. Verifica tu conexión a internet
2. Asegúrate de que el puerto 587 no esté bloqueado
3. Intenta desde otra red

---

### El email no llega

**Solución:**
1. Revisa la carpeta de spam del proveedor
2. Verifica que el email del proveedor esté bien escrito
3. Revisa los logs del backend para confirmar que se envió

---

## 🎯 Límites de Gmail

| Plan | Límite diario | Límite por minuto |
|------|---------------|-------------------|
| Gmail gratuito | 500 emails | ~10-15 emails |

Para volúmenes mayores, considera usar:
- **SendGrid** (100 emails/día gratis)
- **Amazon SES** ($0.10 por cada 1,000 emails)

---

## 📚 Recursos adicionales

- [Documentación oficial de Gmail SMTP](https://support.google.com/mail/answer/7126229)
- [Spring Boot Mail Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Generar App Passwords](https://myaccount.google.com/apppasswords)

---

**¡Listo!** Ahora tu sistema puede enviar emails profesionales a los proveedores. 🎉
