FROM eclipse-temurin:21-jre

WORKDIR /app
RUN mkdir -p /app/logs

COPY target/psi-purchase-*.jar app.jar
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8090

ENTRYPOINT ["/entrypoint.sh"]
