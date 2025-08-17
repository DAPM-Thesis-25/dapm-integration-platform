# FROM eclipse-temurin:21-jdk-alpine
# WORKDIR /app
# COPY target/security-service-0.0.1-SNAPSHOT.jar app.jar
# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"]
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY target/security-service-0.0.1-SNAPSHOT.jar /app/app.jar

# Explode the fat jar so BOOT-INF/classes and BOOT-INF/lib/* exist on the filesystem
RUN jar -xf /app/app.jar

EXPOSE 8080

# Run using classpath (not -jar) so the app uses the exploded dirs
# (Your main class is com.dapm.security_service.SecurityServiceApplication)
ENTRYPOINT ["java", "-cp", "/app/BOOT-INF/classes:/app/BOOT-INF/lib/*", "com.dapm.security_service.SecurityServiceApplication"]
