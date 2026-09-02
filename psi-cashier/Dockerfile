FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/psi-cashier-1.0.0.jar app.jar

# 复制初始化数据库（首次挂载 volume 时会自动复制到卷中）
COPY data/myp.db data/myp.db

EXPOSE 8088

VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]