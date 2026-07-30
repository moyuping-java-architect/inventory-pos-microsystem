FROM eclipse-temurin:21-jre

WORKDIR /app
RUN mkdir -p /app/logs

COPY target/psi-goods-*.jar app.jar
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8082

ENTRYPOINT ["/entrypoint.sh"]
