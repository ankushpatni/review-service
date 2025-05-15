FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Create runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Set entry point
ENTRYPOINT ["java", "-jar", "app.jar"]