HEAD
# 🌱 EcoMaxTienda - Sistema de Gestión de Tienda Virtual

**EcoMaxTienda** es un sistema completo de gestión de tienda virtual enfocado en productos eco-sostenibles, desarrollado con Spring Boot, PostgreSQL y tecnologías web modernas.

## 📋 Características Principales

### 🛒 **Portal de Cliente**
- ✅ Registro y autenticación de usuarios
- ✅ Catálogo de productos eco-sostenibles
- ✅ Carrito de compras inteligente
- ✅ Gestión de perfil y configuración
- ✅ Historial de pedidos
- ✅ Sistema de suscripciones premium
- ✅ Subida y gestión de foto de perfil

### 🔧 **Panel Administrativo**
- ✅ Dashboard con estadísticas en tiempo real
- ✅ Gestión completa de productos
- ✅ Control de inventario y stock
- ✅ Administración de usuarios y roles
- ✅ Gestión de pedidos y pagos
- ✅ Reportes y análisis de ventas
- ✅ Sistema de alertas automáticas

### 📧 **Sistema de Comunicaciones**
- ✅ Envío de emails automatizado
- ✅ Notificaciones de bienvenida
- ✅ Confirmaciones de pedido
- ✅ Integración con Gmail SMTP

## 🛠️ Tecnologías Utilizadas

- **Backend:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Base de Datos:** PostgreSQL 14+
- **Frontend:** Thymeleaf, Bootstrap 5, JavaScript
- **Seguridad:** BCrypt para encriptación de contraseñas
- **Email:** Spring Mail con soporte SMTP
- **Build Tool:** Maven
- **Java:** JDK 17+

## 📦 Estructura del Proyecto

```
ecomaxtienda/
├── src/main/java/com/ecomaxtienda/
│   ├── controller/          # Controladores REST y Web
│   ├── entity/             # Entidades JPA
│   ├── repository/         # Repositorios de datos
│   ├── service/            # Lógica de negocio
│   └── config/             # Configuraciones
├── src/main/resources/
│   ├── templates/          # Plantillas Thymeleaf
│   ├── static/             # CSS, JS, imágenes
│   └── application.properties
├── script_completo_base_datos.sql  # Script completo de BD
└── documentación/          # Archivos de documentación
```

## 🚀 Instalación y Configuración

### Prerrequisitos
- Java JDK 17+
- PostgreSQL 16+
- Maven 3.6+
- Git

### 1. Clonar el repositorio
```bash
git clone https://github.com/ChrisRafaile/EcoMaxTienda.git
cd EcoMaxTienda
```

### 2. Configurar la base de datos
```sql
-- Crear base de datos
CREATE DATABASE ecomaxtienda_db;

-- Ejecutar el script completo
\i script_completo_base_datos.sql
```

### 3. Configurar application.properties
```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/ecomaxtienda_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# Email (opcional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email@gmail.com
spring.mail.password=tu_app_password
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

### 5. Acceder al sistema
- **URL:** http://localhost:8081
- **Admin:** admin@ecomaxtienda.com / admin123
- **Cliente:** cliente@test.com / cliente123

## 📊 Base de Datos

El proyecto incluye un script SQL completo (`script_completo_base_datos.sql`) con:

- ✅ 8 tablas principales con relaciones
- ✅ Índices para optimización
- ✅ Triggers para auditoría
- ✅ Vistas para reportes
- ✅ Datos iniciales de prueba
- ✅ Funciones útiles para el sistema

### Principales Tablas:
- `tb_usuario` - Gestión de usuarios
- `tb_producto` - Catálogo de productos
- `tb_pedido` - Órdenes de compra
- `tb_inventario` - Control de stock
- `tb_pago` - Transacciones
- `tb_rol` - Roles del sistema

## 🔐 Seguridad

- **Autenticación:** Spring Security con formularios
- **Autorización:** Control de acceso basado en roles
- **Passwords:** Encriptación BCrypt
- **CSRF:** Protección habilitada
- **SQL Injection:** Prevención con JPA/Hibernate

## 📱 Funcionalidades Destacadas

### Para Clientes:
- Registro con datos completos (nombre, apellido, DNI, teléfono, dirección)
- Subida de foto de perfil
- Edición de información personal
- Navegación intuitiva y responsive

### Para Administradores:
- Dashboard con métricas en tiempo real
- Gestión completa de productos e inventario
- Control de usuarios y pedidos
- Reportes de ventas y estadísticas

## 🌱 Enfoque Eco-Sostenible

EcoMaxTienda está diseñado específicamente para:
- Productos eco-sostenibles y amigables con el medio ambiente
- Métricas de impacto ambiental
- Puntuaciones ecológicas
- Promoción de consumo responsable

## 📄 Documentación Adicional

- `ADMIN_SYSTEM_DOCUMENTATION.md` - Documentación del panel admin
- `CLIENT_FLOW_COMPLETION.md` - Flujo completo del cliente
- `SISTEMA_EMAIL_COMPLETO_FINAL.md` - Sistema de emails
- `EXPLICACION_VALORES_NULL.md` - Manejo de valores NULL

## 👨‍💻 Autor

**Chris Rafaile**
- GitHub: [@ChrisRafaile](https://github.com/ChrisRafaile)
- Proyecto: Sistema de Gestión EcoMaxTienda

## 📧 Contacto

Para consultas sobre el proyecto, puedes contactar a través de GitHub Issues.

---

🌱 **EcoMaxTienda** - *Tecnología verde para un futuro sostenible*

# 🌿 EcoMaxTienda - Plataforma de Suscripción Ecológica

**EcoMaxTienda** es una solución web diseñada para facilitar el consumo responsable mediante la suscripción a productos ecológicos. Automatiza procesos de compra, suscripción y entrega a domicilio, fomentando hábitos sostenibles en la comunidad.

---

## 🚀 Funcionalidades Principales

- Registro e inicio de sesión de usuarios
- Catálogo de productos con filtro dinámico
- Gestión de suscripciones: mensual, trimestral, bimestral, semestral y anual
- Carrito de compras con resumen detallado
- Métodos de pago: Tarjeta, Yape/Plin, Transferencia, PayPal
- Confirmación automática del pedido
- Panel administrativo: métricas, reportes y gestión de stock

---

## 🧱 Tecnologías Utilizadas

| Tecnología     | Descripción                                 |
|----------------|---------------------------------------------|
| PostgreSQL 16  | SGBD para el diseño físico de la BD         |
| pgAdmin 4      | Interfaz de administración de base de datos |
| Java 17        | Lenguaje backend para desarrollo modular     |
| Balsamiq       | Diseño de prototipos UX/UI                  |
| Markdown       | Documentación técnica estructurada           |
| GitHub         | Repositorio y control de versiones           |

---

## 🗂️ Estructura del Proyecto (Futura Implementación)

```
EcoMaxTienda/
├── backend/
│   ├── models/
│   ├── controllers/
│   ├── routes/
│   └── DAO/
├── frontend/
│   ├── components/
│   ├── views/
│   └── assets/
├── docs/
│   └── README.md
└── sql/
    └── EcoMaxTienda.sql
```

---

## 📊 Capturas y Mockups

> Los prototipos fueron desarrollados en Balsamiq. Ingresar a `/docs/mockups/` para visualizar:

- Inicio, Registro y Login
- Catálogo de productos
- Carrito + selección de suscripción
- Pago
- Confirmación de pedido
- Panel administrativo

---

## 🧾 Documentación Técnica

Consulta la documentación detallada del diseño físico, modelo lógico, clases UML, casos de uso, etc., en mi informes/avances del proyecto.

---

## 👤 Autor

**Christopher Rafaile** – [@ChrisRafaile](https://github.com/ChrisRafaile)  
Proyecto académico desarrollado para el curso de *Curso Integrador I - Ingeniería de Sistemas e Informática*

---

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Revisa el archivo `LICENSE` para más información.
 8ea10c5c45050e39b246aabf8084e06b6d70dd6a
