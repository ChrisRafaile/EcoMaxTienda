# EcoMaxTienda

EcoMaxTienda es una plataforma web de comercio electrónico orientada a la venta de productos eco-sostenibles, desarrollada como proyecto integrador universitario. El sistema está diseñado para ser seguro, escalable y fácil de usar, tanto para clientes como para administradores.

---

## 🚀 **¿Qué se ha desarrollado?**

- **Registro y autenticación de usuarios:**
  - Registro manual con email real y social login (Google, Facebook).
  - Envío automático de correo de bienvenida.
  - Recuperación y cambio de contraseña.
- **Panel de cliente:**
  - Visualización de productos, carrito de compras, historial de pedidos.
  - Suscripción premium y gestión de perfil.
- **Panel de administrador:**
  - Gestión de productos, usuarios, pedidos y reportes.
  - Monitoreo de métricas y logs de seguridad.
- **Seguridad:**
  - Spring Security, roles, CSRF, BCrypt, HTTPS.
  - Logs de acceso y eventos críticos.
- **Despliegue y mantenimiento:**
  - Docker Compose para orquestación.
  - Scripts de backup, restauración y health-check.
  - Preparado para despliegue en la nube.
- **Pruebas:**
  - Pruebas unitarias y de integración con JUnit y Mockito.

---

## 🛠️ **Guía de instalación y uso**

### **Requisitos previos**
- Java 17+
- Maven 3.8+
- Docker y Docker Compose (opcional, recomendado)
- PostgreSQL 16.4 (si no usas Docker)

### **Instalación local**
1. Clona el repositorio y entra al directorio del proyecto.
2. Configura la base de datos en `src/main/resources/application.properties`.
3. Ejecuta:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
4. Accede a [http://localhost:8081](http://localhost:8081)

### **Instalación con Docker**
1. Configura variables de entorno y certificados SSL si es necesario.
2. Ejecuta:
   ```bash
   docker-compose up -d
   ```
3. Accede a [http://localhost:8081](http://localhost:8081)

---

## 👤 **¿Cómo usar el sistema?**

### **Para clientes:**
- Regístrate con tu email o usando Google/Facebook.
- Recibe un correo de bienvenida.
- Explora el catálogo, agrega productos al carrito y realiza pedidos.
- Gestiona tu perfil, cambia tu contraseña y suscríbete a planes premium.
- Visualiza tu historial de pedidos y descárgalos en PDF.

### **Para administradores:**
- Accede al panel de administración con credenciales de admin.
- Gestiona productos, usuarios y pedidos.
- Visualiza reportes y métricas de ventas.
- Monitorea logs de seguridad y salud del sistema.

---

## 🔒 **Pruebas y seguridad**
- Pruebas unitarias y de integración ejecutables con `mvn test`.
- Seguridad robusta: roles, CSRF, BCrypt, HTTPS, logs de acceso.
- Simulación de envío de emails en entorno de pruebas.

---

## ☁️ **Despliegue y mantenimiento**
- Despliegue local o en la nube (AWS, GCP, Azure) usando Docker.
- Scripts de backup y restauración en la carpeta `scripts/`.
- Health-checks automáticos y logs de monitoreo.

---

## 🌱 **¿Qué se puede mejorar a futuro?**
- **Integración de pasarelas de pago reales** (Stripe, PayPal, etc.).
- **Notificaciones push y SMS** para pedidos y promociones.
- **Panel de analítica avanzada** para admins (ventas, usuarios activos, etc.).
- **Marketplace multi-vendedor** y gestión de inventario distribuido.
- **App móvil** (Android/iOS) conectada vía API REST.
- **Internacionalización** (multi-idioma, multi-moneda).
- **Integración con servicios cloud** (S3 para imágenes, Cloud SQL, etc.).
- **Pruebas automatizadas E2E** (Selenium, Cypress).
- **Mejoras en accesibilidad y experiencia de usuario.**

---

## 👨‍💻 **Créditos y contacto**
- Proyecto desarrollado por el equipo de EcoMaxTienda para el curso integrador universitario.
- Contacto: [tu-email@ejemplo.com]
- Repositorio: [GitHub/URL-del-proyecto]

---

¡Gracias por usar EcoMaxTienda! Si tienes sugerencias o encuentras algún bug, no dudes en contactarnos. 