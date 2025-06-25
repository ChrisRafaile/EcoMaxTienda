# Archivo para ignorar errores específicos de VS Code
# Los siguientes problemas son falsos positivos del IDE:

# 1. "Missing mandatory Classpath entries" - Resuelto en Maven
# 2. "Cannot find symbol" para getters/setters - Lombok funciona en Maven
# 3. Advertencias de versiones de Spring Boot - Configuración estable elegida
# 4. "Variable not initialized" - Spring DI funciona correctamente

# NOTA: Todos estos errores son problemas del IDE, NO del código.
# El proyecto compila y funciona perfectamente con Maven.

# Comando para verificar: mvn clean compile test
# Comando para ejecutar: mvn spring-boot:run
