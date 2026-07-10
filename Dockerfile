# ── Build stage ────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ── Run stage ──────────────────────────────────────────────────────────────────
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Limitar RAM: máx 256MB heap + 128MB metaspace = ~400MB total
# Sin esto Spring Boot agarra toda la RAM disponible y Railway te cobra de más
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
