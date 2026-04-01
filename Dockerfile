FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven wrapper and project descriptor first to maximize layer caching
COPY backend/.mvn/ backend/.mvn/
COPY backend/mvnw backend/pom.xml backend/

WORKDIR /app/backend
RUN chmod +x mvnw

# Copy source code after dependency metadata
COPY backend/src/ src/

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
