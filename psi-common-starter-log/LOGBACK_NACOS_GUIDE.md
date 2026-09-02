# Logback配置整合Nacos指南

## 概述

将Logback配置整合到Nacos可以实现日志配置的集中管理和动态更新，无需重启应用即可调整日志级别、输出路径等配置。

## 整合方案

### 1. 配置文件结构

```
psi-common-starter-log/
├── src/main/resources/
│   ├── logback-spring.xml          # 本地开发环境配置
│   └── logback-nacos-template.xml  # Nacos配置模板
```

### 2. Nacos配置项

在Nacos配置中心创建以下配置项（Data ID: `psi-system-logback.properties`）：

```properties
# 日志基础配置
LOG_BASE_DIR=./logs
LOG_LEVEL=INFO
LOG_RETENTION_DAYS=7
LOG_MAX_FILE_SIZE=100MB
LOG_MAX_FILES_PER_DAY=10

# 应用名称（会自动从spring.application.name获取）
# spring.application.name=psi-system
```

### 3. 应用配置

#### 3.1 本地开发环境
使用本地的 `logback-spring.xml` 配置文件。

#### 3.2 生产环境
在 `application.properties` 中配置：

```properties
# 启用Nacos配置中心
spring.cloud.nacos.config.enabled=true

# 配置文件格式
spring.cloud.nacos.config.file-extension=properties

# 共享配置
spring.cloud.nacos.config.shared-configs[0].data-id=psi-common-logback.properties
spring.cloud.nacos.config.shared-configs[0].group=DEFAULT_GROUP
spring.cloud.nacos.config.shared-configs[0].refresh=true

# 日志配置（使用环境变量）
logging.config=classpath:logback-nacos-template.xml
```

### 4. 动态更新配置

#### 4.1 修改日志级别
在Nacos配置中心修改 `LOG_LEVEL` 参数：
- 开发环境：`DEBUG`
- 测试环境：`INFO`
- 生产环境：`WARN` 或 `ERROR`

#### 4.2 修改日志路径
修改 `LOG_BASE_DIR` 参数：
- 本地开发：`./logs`
- 测试环境：`/var/log/psi`
- 生产环境：`/data/psi/logs`

#### 4.3 修改日志保留策略
- `LOG_RETENTION_DAYS`: 日志保留天数
- `LOG_MAX_FILE_SIZE`: 单个日志文件最大大小
- `LOG_MAX_FILES_PER_DAY`: 每天最大日志文件数

### 5. 配置模板说明

`logback-nacos-template.xml` 使用了以下环境变量占位符：

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| 日志基础目录 | `LOG_BASE_DIR` | `./logs` | 日志文件存储根目录 |
| 日志级别 | `LOG_LEVEL` | `INFO` | 全局日志级别 |
| 保留天数 | `LOG_RETENTION_DAYS` | `7` | 日志文件保留天数 |
| 最大文件大小 | `LOG_MAX_FILE_SIZE` | `100MB` | 单个日志文件最大大小 |
| 每日最大文件数 | `LOG_MAX_FILES_PER_DAY` | `10` | 每天最多生成日志文件数 |

### 6. Nacos配置示例

#### 6.1 开发环境配置
```properties
# Data ID: psi-system-dev-logback.properties
LOG_BASE_DIR=./logs
LOG_LEVEL=DEBUG
LOG_RETENTION_DAYS=3
LOG_MAX_FILE_SIZE=50MB
LOG_MAX_FILES_PER_DAY=5
```

#### 6.2 测试环境配置
```properties
# Data ID: psi-system-test-logback.properties
LOG_BASE_DIR=/var/log/psi
LOG_LEVEL=INFO
LOG_RETENTION_DAYS=7
LOG_MAX_FILE_SIZE=100MB
LOG_MAX_FILES_PER_DAY=10
```

#### 6.3 生产环境配置
```properties
# Data ID: psi-system-prod-logback.properties
LOG_BASE_DIR=/data/psi/logs
LOG_LEVEL=WARN
LOG_RETENTION_DAYS=30
LOG_MAX_FILE_SIZE=200MB
LOG_MAX_FILES_PER_DAY=20
```

### 7. 使用步骤

1. **在Nacos中创建配置**
   - 登录Nacos控制台
   - 进入配置管理 -> 配置列表
   - 点击"创建配置"
   - Data ID: `psi-system-logback.properties`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `Properties`
   - 配置内容: 复制上面的配置示例

2. **修改应用配置**
   - 更新 `application.properties` 启用Nacos配置
   - 指定使用 `logback-nacos-template.xml`

3. **重启应用**
   - 应用启动时会自动从Nacos读取日志配置
   - 日志配置会应用到 `logback-nacos-template.xml` 中的占位符

4. **动态更新**
   - 在Nacos中修改配置
   - 点击"发布"按钮
   - 应用会自动刷新配置（需要实现配置刷新机制）

### 8. 注意事项

1. **配置刷新限制**
   - Logback配置默认不支持动态刷新
   - 修改日志级别等配置需要重启应用
   - 如需动态刷新，需要集成 `logback-ext-spring` 或自定义刷新机制

2. **环境变量优先级**
   - 环境变量 > Nacos配置 > 默认值
   - 可以通过 `-D` 参数覆盖配置

3. **多环境配置**
   - 建议为不同环境创建不同的配置文件
   - 使用 `spring.profiles.active` 区分环境

4. **配置安全**
   - 敏感信息不要放在Nacos配置中
   - 使用Nacos的命名空间隔离不同环境的配置

### 9. 高级配置

#### 9.1 按模块配置日志级别
```properties
# Nacos配置
LOG_LEVEL=INFO
LOG_LEVEL_SYSTEM=DEBUG
LOG_LEVEL_ORDER=INFO
LOG_LEVEL_STOCK=WARN
```

在 `logback-nacos-template.xml` 中：
```xml
<logger name="com.psi.system" level="${LOG_LEVEL_SYSTEM:-${LOG_LEVEL}}" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="ASYNC"/>
</logger>
```

#### 9.2 自定义日志格式
```properties
# Nacos配置
LOG_PATTERN=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
LOG_PATTERN_WITH_TRACE=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - traceId=%X{traceId} - %msg%n
```

### 10. 故障排查

#### 问题1: 配置不生效
- 检查Nacos连接是否正常
- 确认配置文件的Data ID和Group是否正确
- 查看应用启动日志中的配置加载信息

#### 问题2: 日志文件未生成
- 检查 `LOG_BASE_DIR` 路径是否有写权限
- 确认磁盘空间是否充足
- 查看日志配置中的路径是否正确

#### 问题3: 日志级别不生效
- 确认配置刷新机制是否正常
- 检查是否有其他配置覆盖了日志级别
- 查看Logback内部日志了解配置加载情况

## 总结

将Logback配置整合到Nacos可以带来以下好处：

1. **集中管理**: 所有服务的日志配置统一管理
2. **动态调整**: 无需重启即可调整日志配置（需要实现刷新机制）
3. **环境隔离**: 不同环境使用不同的配置
4. **版本控制**: Nacos提供配置版本管理
5. **权限控制**: 可以控制不同人员对配置的访问权限

建议在生产环境中使用此方案，提高运维效率。