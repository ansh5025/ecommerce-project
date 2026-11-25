# Build: docker build -t ecommerce-api ./backend
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# The Spring context test needs a live MySQL server, so tests are skipped here as in the old workflow.
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:11-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
USER app
EXPOSE 8080
ENV SERVER_PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
