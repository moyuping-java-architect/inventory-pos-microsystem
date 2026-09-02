package com.psi.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * JVM 参数配置类
 * 提供生产级 ZGC 配置，适配 Docker 部署
 * 
 * ZGC 优势：
 * 1. 极低的暂停时间（通常 < 1ms）
 * 2. 支持 TB 级堆内存
 * 3. 并发垃圾回收，不影响应用响应
 * 4. 适合微服务、高并发场景
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Data
public class JvmConfig {

    /**
     * 堆内存大小
     */
    private String heapSize = "4g";

    /**
     * 最小堆内存
     */
    private String minHeapSize = "2g";

    /**
     * 最大堆内存
     */
    private String maxHeapSize = "4g";

    /**
     * 新生代大小
     */
    private String youngGenSize = "1g";

    /**
     * 是否启用 ZGC
     */
    private boolean zgcEnabled = true;

    /**
     * 是否启用压缩指针
     */
    private boolean compressedOops = true;

    /**
     * 是否启用分层编译
     */
    private boolean tieredCompilation = true;

    /**
     * 是否启用字符串去重
     */
    private boolean stringDedup = true;

    /**
     * 是否启用偏向锁
     */
    private boolean biasedLocking = true;

    /**
     * GC 日志文件路径
     */
    private String gcLogPath = "/var/log/app/gc.log";

    /**
     * 是否启用 GC 日志
     */
    private boolean gcLogEnabled = true;

    /**
     * 是否启用调试模式
     */
    private boolean debugMode = false;

    /**
     * 是否启用断言
     */
    private boolean assertions = false;

    /**
     * 获取生产级 ZGC JVM 参数列表
     * 
     * @return JVM 参数列表
     */
    public List<String> getProductionZgcArgs() {
        List<String> args = new ArrayList<>();

        // 堆内存配置
        args.add("-Xms" + minHeapSize);
        args.add("-Xmx" + maxHeapSize);

        // ZGC 配置（JDK 17+）
        if (zgcEnabled) {
            args.add("-XX:+UseZGC");
            args.add("-XX:+ZGenerational");           // 启用分代 ZGC（JDK 21+）
            args.add("-XX:ZAllocationSpikeTolerance=5.0");  // 分配突增容忍度
            args.add("-XX:ZCollectionInterval=30");          // 最大 GC 间隔（秒）
            args.add("-XX:ZHeapSizeMin=" + minHeapSize);
            args.add("-XX:ZHeapSizeMax=" + maxHeapSize);
            
            // 并发线程数（默认 CPU核心数）
            int cpuCount = Runtime.getRuntime().availableProcessors();
            args.add("-XX:ConcGCThreads=" + Math.min(cpuCount, 8));
            args.add("-XX:ParallelGCThreads=" + cpuCount);
        }

        // 压缩指针（默认启用，32GB 以下堆内存推荐）
        if (compressedOops) {
            args.add("-XX:+UseCompressedOops");
            args.add("-XX:+UseCompressedClassPointers");
        }

        // 分层编译
        if (tieredCompilation) {
            args.add("-XX:+TieredCompilation");
            args.add("-XX:TieredStopAtLevel=4");
        }

        // 字符串去重
        if (stringDedup) {
            args.add("-XX:+UseStringDeduplication");
        }

        // GC 日志配置
        if (gcLogEnabled) {
            args.add("-Xlog:gc*:file=" + gcLogPath + ":time,level,tags:filecount=5,filesize=100m");
            args.add("-Xlog:safepoint*:file=" + gcLogPath + ".safepoint:time,level,tags:filecount=5,filesize=50m");
        }

        // 其他优化参数
        args.add("-XX:+AlwaysPreTouch");                     // 预触摸内存，避免运行时分配延迟
        args.add("-XX:+UseLargePages");                      // 启用大页
        args.add("-XX:+UseTransparentHugePages");            // 透明大页
        args.add("-XX:+IgnoreUnrecognizedVMOptions");        // 忽略不识别的选项（兼容不同JDK版本）
        
        // 禁用一些不必要的功能
        args.add("-XX:-UseBiasedLocking");                   // 禁用偏向锁（高并发场景可能有害）
        args.add("-XX:+DisableExplicitGC");                  // 禁用 System.gc()
        args.add("-XX:+AggressiveOpts");                     // 启用激进优化

        // 线程栈大小
        args.add("-Xss256k");

        // 时区设置
        args.add("-Duser.timezone=Asia/Shanghai");

        // UTF-8 编码
        args.add("-Dfile.encoding=UTF-8");
        args.add("-Dsun.jnu.encoding=UTF-8");

        // 禁用 JIT 编译警告
        args.add("-XX:-PrintCompilation");

        // 启用诊断选项
        args.add("-XX:+UnlockDiagnosticVMOptions");
        args.add("-XX:+DebugNonSafepoints");

        // 安全相关
        args.add("-Djava.security.egd=file:/dev/./urandom");  // 更快的随机数生成

        // 禁用一些调试选项
        if (!debugMode) {
            args.add("-XX:-OmitStackTraceInFastThrow");      // 保留异常堆栈
        }

        // 断言
        if (!assertions) {
            args.add("-ea:!java..*");
            args.add("-ea:!javax..*");
        }

        log.info("生成生产级 ZGC JVM 参数，共 {} 项", args.size());
        return args;
    }

    /**
     * 获取 Docker 部署脚本内容
     * 
     * @param appName 应用名称
     * @param jarPath JAR 文件路径
     * @return Docker 部署脚本
     */
    public String getDockerRunScript(String appName, String jarPath) {
        List<String> jvmArgs = getProductionZgcArgs();
        
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("# ============================================\n");
        script.append("# 生产级 ZGC JVM Docker 部署脚本\n");
        script.append("# 适配非洲弱网场景，优化网络超时和重试\n");
        script.append("# ============================================\n\n");

        script.append("APP_NAME=\"").append(appName).append("\"\n");
        script.append("JAR_PATH=\"").append(jarPath).append("\"\n");
        script.append("LOG_DIR=\"/var/log/app\"\n");
        script.append("PID_FILE=\"/var/run/${APP_NAME}.pid\"\n\n");

        // 创建日志目录
        script.append("# 创建日志目录\n");
        script.append("mkdir -p ${LOG_DIR}\n\n");

        // JVM 参数
        script.append("# JVM 参数（生产级 ZGC 配置）\n");
        script.append("JVM_ARGS=\"");
        for (String arg : jvmArgs) {
            script.append(arg).append(" ");
        }
        script.append("\"\n\n");

        // 启动命令
        script.append("# 启动应用\n");
        script.append("nohup java ${JVM_ARGS} \\\n");
        script.append("  -jar ${JAR_PATH} \\\n");
        script.append("  --spring.profiles.active=prod \\\n");
        script.append("  --server.port=8080 \\\n");
        script.append("  --spring.cloud.nacos.discovery.server-addr=nacos:8848 \\\n");
        script.append("  --spring.cloud.nacos.config.server-addr=nacos:8848 \\\n");
        script.append("  --spring.datasource.url=jdbc:mysql://mysql:3306/example_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai \\\n");
        script.append("  --spring.rabbitmq.host=rabbitmq \\\n");
        script.append("  --logging.file.path=${LOG_DIR} \\\n");
        script.append("  > ${LOG_DIR}/app.out 2>&1 &\n\n");

        // 保存 PID
        script.append("# 保存 PID\n");
        script.append("echo $! > ${PID_FILE}\n");
        script.append("echo \"${APP_NAME} 启动成功，PID: $(cat ${PID_FILE})\"\n\n");

        // 健康检查
        script.append("# 健康检查\n");
        script.append("sleep 10\n");
        script.append("if curl -s http://localhost:8080/actuator/health | grep -q \"UP\"; then\n");
        script.append("    echo \"${APP_NAME} 健康检查通过\"\n");
        script.append("else\n");
        script.append("    echo \"${APP_NAME} 健康检查失败\"\n");
        script.append("    exit 1\n");
        script.append("fi\n");

        return script.toString();
    }

    /**
     * 获取 Docker Compose 配置
     * 
     * @param appName 应用名称
     * @return Docker Compose YAML
     */
    public String getDockerComposeConfig(String appName) {
        StringBuilder compose = new StringBuilder();
        compose.append("version: '3.8'\n");
        compose.append("services:\n");
        compose.append("  ").append(appName).append(":\n");
        compose.append("    image: openjdk:21-jdk-slim\n");
        compose.append("    container_name: ").append(appName).append("\n");
        compose.append("    ports:\n");
        compose.append("      - \"8080:8080\"\n");
        compose.append("    volumes:\n");
        compose.append("      - ./logs:/var/log/app\n");
        compose.append("      - ./app.jar:/app/app.jar\n");
        compose.append("    environment:\n");
        compose.append("      - TZ=Asia/Shanghai\n");
        compose.append("      - JAVA_OPTS=-Xms4g -Xmx4g -XX:+UseZGC -XX:+ZGenerational -Xlog:gc*:/var/log/app/gc.log:time,level,tags:filecount=5,filesize=100m\n");
        compose.append("    command: [\"java\", \"-jar\", \"/app/app.jar\"]\n");
        compose.append("    restart: unless-stopped\n");
        compose.append("    networks:\n");
        compose.append("      - psi-network\n");
        compose.append("    healthcheck:\n");
        compose.append("      test: [\"CMD\", \"curl\", \"-f\", \"http://localhost:8080/actuator/health\"]\n");
        compose.append("      interval: 30s\n");
        compose.append("      timeout: 10s\n");
        compose.append("      retries: 3\n");
        compose.append("      start_period: 60s\n");
        compose.append("\n");
        compose.append("networks:\n");
        compose.append("  psi-network:\n");
        compose.append("    driver: bridge\n");

        return compose.toString();
    }
}