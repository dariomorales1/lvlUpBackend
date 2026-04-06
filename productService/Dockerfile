# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY apigateway/pom.xml apigateway/
COPY authService/pom.xml authService/
COPY cartService/pom.xml cartService/
COPY orderService/pom.xml orderService/
COPY productService/pom.xml productService/
COPY userService/pom.xml userService/

RUN mvn dependency:go-offline -pl productService -am -q || true

COPY productService/src productService/src
RUN mvn clean package -pl productService -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/productService/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

ENV JAVA_OPTS="-Xms256m -Xmx640m"
EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
