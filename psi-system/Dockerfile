FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/psi-system-*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
