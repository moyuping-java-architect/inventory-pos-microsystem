FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 复制应用 jar
COPY target/psi-cashier-*.jar app.jar

# 复制 SQLite 模板库，首次运行时自动初始化到终端数据目录
COPY data/myp.db /app/myp-template.db

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8088

ENTRYPOINT ["/app/docker-entrypoint.sh"]
