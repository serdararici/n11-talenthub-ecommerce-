# Stage 1: build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent POM and all module POMs for dependency caching
COPY pom.xml .
COPY auth-service/pom.xml    auth-service/
COPY product-service/pom.xml product-service/
COPY cart-service/pom.xml    cart-service/
COPY order-service/pom.xml   order-service/

RUN mvn dependency:go-offline -pl cart-service -am -B --no-transfer-progress

# Copy source and build
COPY cart-service/src cart-service/src
RUN mvn package -pl cart-service -am -DskipTests -B --no-transfer-progress

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/cart-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
