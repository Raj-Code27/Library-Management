FROM eclipse-temurin:17-jre

RUN useradd -u 10001 -r -s /bin/false appuser

WORKDIR /app

COPY build/libs/library-management-api-1.0.0.jar app.jar
RUN chown appuser:appuser app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

