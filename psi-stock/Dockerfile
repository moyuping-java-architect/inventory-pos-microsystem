FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/psi-stock-*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
