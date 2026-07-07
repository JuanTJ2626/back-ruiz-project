# 🚂 Deploy en Railway con Emails Configurados

## Variables de entorno a configurar en Railway

Cuando despliegues tu backend en Railway, necesitas agregar estas variables de entorno:

---

## 📧 Variables de Email (Gmail SMTP)

```bash
MAIL_USERNAME=juanitotj003@gmail.com
MAIL_PASSWORD=ebihuaclbascybzg
MAIL_FROM=juanitotj003@gmail.com
```

⚠️ **Importante:** 
- El `MAIL_PASSWORD` debe estar **sin espacios** (todo junto)
- Original: `ebih uacl basc ybzg`
- Para Railway: `ebihuaclbascybzg`

---

## 🗄️ Variables de Base de Datos

```bash
DB_URL=jdbc:mysql://interchange.proxy.rlwy.net:51000/railway
DB_USERNAME=root
DB_PASSWORD=ZGcSBOfMXZBCVvqXwagfqFBNECLexwUt
```

---

## ☁️ Variables de Cloudinary (Imágenes)

```bash
CLOUDINARY_CLOUD_NAME=tu-cloud-name
CLOUDINARY_API_KEY=tu-api-key
CLOUDINARY_API_SECRET=tu-api-secret
```

---

## 🔐 Variables de Seguridad

```bash
JWT_SECRET=tu-secret-key-super-seguro-de-minimo-64-caracteres-aleatorios
```

**Genera un JWT_SECRET fuerte:**
```bash
# En Linux/Mac:
openssl rand -base64 64

# En Windows PowerShell:
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes((1..64 | ForEach-Object {[char](Get-Random -Minimum 33 -Maximum 126)})))
```

---

## 🤖 Variables del Bot (Opcional)

```bash
BOT_SECRET_KEY=tu-clave-secreta-para-bot
```

---

## 📋 Pasos para configurar en Railway

### 1. Accede a tu proyecto en Railway

Ve a: https://railway.app/dashboard

### 2. Selecciona tu servicio de backend

Click en el servicio de Spring Boot

### 3. Ve a la pestaña "Variables"

Click en "Variables" en el menú lateral

### 4. Agrega cada variable

Para cada variable de arriba:

1. Click en "+ New Variable"
2. **Variable Name:** (nombre de la variable)
3. **Value:** (valor correspondiente)
4. Click "Add"

### 5. Ejemplo completo

```
Variable Name          | Value
-----------------------|----------------------------------------
MAIL_USERNAME          | juanitotj003@gmail.com
MAIL_PASSWORD          | ebihuaclbascybzg
MAIL_FROM              | juanitotj003@gmail.com
DB_URL                 | jdbc:mysql://host:port/railway
DB_USERNAME            | root
DB_PASSWORD            | tu-db-password
CLOUDINARY_CLOUD_NAME  | tu-cloud-name
CLOUDINARY_API_KEY     | tu-api-key
CLOUDINARY_API_SECRET  | tu-api-secret
JWT_SECRET             | tu-jwt-secret-largo
```

### 6. Guarda los cambios

Railway automáticamente redesplegará tu aplicación con las nuevas variables.

---

## ✅ Verificar que funcionó

### 1. Revisa los logs de Railway

En la pestaña "Deployments" → Click en el último deploy → "View Logs"

Busca:
```
Started RuizApiApplication in X.XXX seconds
```

### 2. Prueba el endpoint de email

```bash
curl -X POST "https://tu-app.up.railway.app/api/pedidos/1/enviar-email" \
  -H "Authorization: Bearer tu-token-jwt"
```

### 3. Verifica el email

Revisa la bandeja del proveedor (y spam) para confirmar que llegó.

---

## 🐛 Problemas comunes

### ❌ "Username and Password not accepted" en producción

**Causa:** App Password mal configurado

**Solución:**
1. Verifica que `MAIL_PASSWORD` esté sin espacios: `ebihuaclbascybzg`
2. Verifica que no haya caracteres especiales mal escapados
3. Regenera la App Password si es necesario

---

### ❌ Emails no llegan en producción pero sí en local

**Causa:** Posible bloqueo de Railway al puerto SMTP

**Solución:**
1. Railway generalmente permite SMTP en puerto 587
2. Si persiste, considera usar SendGrid:
   ```bash
   # Variables para SendGrid
   MAIL_HOST=smtp.sendgrid.net
   MAIL_PORT=587
   MAIL_USERNAME=apikey
   MAIL_PASSWORD=tu-sendgrid-api-key
   ```

---

### ❌ Error de conexión timeout

**Causa:** Timeout muy corto

**Solución:**
Agrega estas variables en Railway:
```bash
MAIL_CONNECTION_TIMEOUT=10000
MAIL_TIMEOUT=10000
MAIL_WRITE_TIMEOUT=10000
```

---

## 🔄 Actualizar App Password

Si necesitas cambiar la App Password:

1. Ve a: https://myaccount.google.com/apppasswords
2. Genera una nueva App Password
3. En Railway → Variables → Edita `MAIL_PASSWORD`
4. Pega la nueva password **sin espacios**
5. Guarda

Railway redesplegará automáticamente.

---

## 📊 Monitoreo de emails enviados

### Ver logs en Railway

```
# Busca en los logs:
Email enviado exitosamente al proveedor: [Nombre] ([email])

# O errores:
Error al enviar email al proveedor [Nombre]: [Error]
```

### Límites de Gmail

- **Gratis:** 500 emails/día
- **Workspace:** 2,000 emails/día

Si necesitas más, migra a SendGrid o Amazon SES.

---

## 🚀 Alternativa: SendGrid (Recomendado para producción)

Si Gmail te da problemas o necesitas más volumen:

### 1. Crea cuenta en SendGrid

https://sendgrid.com (100 emails/día gratis)

### 2. Obtén API Key

Dashboard → Settings → API Keys → Create API Key

### 3. Actualiza variables en Railway

```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=tu-sendgrid-api-key-aqui
MAIL_FROM=proyectoruiz10@gmail.com
```

No necesitas cambiar código, solo las variables.

---

## 📝 Checklist final

Antes de dar por terminado el deploy:

- [ ] Todas las variables configuradas en Railway
- [ ] Backend desplegado exitosamente
- [ ] Logs muestran "Started RuizApiApplication"
- [ ] Endpoint de email probado y funcionando
- [ ] Email de prueba recibido correctamente
- [ ] Variables sensibles NO están en el código
- [ ] `.gitignore` protege archivos con credenciales

---

**¡Listo para producción!** 🎉

Tu backend ahora puede enviar emails profesionales a proveedores desde Railway.
