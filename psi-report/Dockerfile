FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/psi-report-*.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
