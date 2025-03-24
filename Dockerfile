# Builder stage
FROM openjdk:21-jdk-slim AS builder
WORKDIR /application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Final stage
FROM openjdk:21-jdk-slim
WORKDIR /application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

EXPOSE 10000
ENTRYPOINT ["sh", "-c", "exec java -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Dserver.port=${PORT:-10000} org.springframework.boot.loader.launch.JarLauncher"]
