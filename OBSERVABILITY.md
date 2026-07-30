# PSI 可观测性方案 (Apache SkyWalking)

> 为面试欧洲远程架构师岗准备的可观测性中间件集成方案
> 一个中间件搞定 **Metrics + Tracing + Logging** 三大支柱

---

## 1. 为什么选 SkyWalking？

| 维度 | SkyWalking | Prometheus+Grafana | ELK |
|------|------------|-------------------|-----|
| 学习成本 | ⭐⭐ (一个中间件) | ⭐⭐⭐ (两个+) | ⭐⭐⭐⭐ |
| 集成方式 | Java Agent 零代码 | SDK 改造 | 改 Logback |
| 三大支柱 | ✅ 一个全包 | ❌ 需补 Tempo/Loki | ❌ 只有日志 |
| 国产化加分 | ✅ 顶级项目 | ❌ CNCF | ❌ Elastic |
| 远程架构师岗 | ✅ APM 完整 | ✅ 云原生向 | ⚠️ 老牌 |

**结论：SkyWalking 投入产出比最高，1 个中间件讲完 APM 全部知识。**

---

## 2. 架构图

```
┌──────────────────────────────────────────────────────────────────┐
│  Browser / POS                                                  │
│  ────────  HTTP ────────►                                       │
└──────────────────────────────────────────────────────────────────┘
        │                          │                     │
        ▼                          ▼                     ▼
   ┌─────────┐               ┌─────────┐           ┌─────────┐
   │ psi-    │               │ psi-    │           │ psi-    │
   │ gateway │ ──Feign────►  │ sale    │ ──MQ──►   │ flow    │
   └─────────┘               └─────────┘           └─────────┘
        │                          │                     │
        │  Java Agent 拦截         │  Java Agent 拦截    │  Java Agent
        ▼                          ▼                     ▼
   ┌──────────────────────────────────────────────────────────────┐
   │                SkyWalking OAP (Collector)                     │
   │  ─ 接收所有服务的 trace / metric / log                          │
   │  ─ 写入 ES7，OAL 引擎做指标聚合                                  │
   └──┬─────────────────────────────────────────────┬────────────┘
      │ OAL                                         │ OAL
      ▼                                             ▼
   ┌──────────────┐                           ┌──────────────┐
   │ Elasticsearch│  ◄── store ──            │ SkyWalking   │
   │ 7.17         │                           │ UI (12900)   │
   └──────────────┘                           └──────────────┘
```

---

## 3. 快速启动

### 3.1 一键拉起 SkyWalking 中间件栈

```bash
# Windows
start-observability.bat

# Linux / Mac
./start-observability.sh
```

启动完成后访问：
- **SkyWalking UI**：http://localhost:12900
- **Elasticsearch**：http://localhost:9200

### 3.2 业务服务接入（两种方式）

#### 方式 A：本地开发

```bash
# 1. 下载 Agent（仅首次）
mkdir -p docker/skywalking/agent
cd docker/skywalking
curl -L -o agent.tgz https://archive.apache.org/dist/skywalking/10.1.0/apache-skywalking-java-agent-10.1.0.tgz
tar -xzf agent.tgz --strip-components=1

# 2. 启动服务（用我们提供的脚本）
cd ../..
start-with-trace.bat psi-goods 8082
```

#### 方式 B：Docker Compose（生产/演示）

```bash
# 1. 拉起可观测性
docker compose -f docker-compose-observability.yml up -d

# 2. 拉起业务
docker compose -f docker-compose-full.yml up -d psi-goods psi-sale psi-purchase psi-sync

# 3. 打开 SkyWalking UI
open http://localhost:12900
```

---

## 4. 关键演示场景（面试用）

### 4.1 看 Trace 全链路

打开 UI → 选 `psi-sale` → 看一条订单提交 trace：

```
/sale/order/submit (EntrySpan, psi-gateway)
   ├─ /sale/order/save (psi-sale)
   │   ├─ JDBC: INSERT order_main  ← SQL 慢查询一目了然
   │   └─ Feign: psi-flow/startWorkflow  ← 跨服务调用
   │       └─ /flow/start (psi-flow)
   │           └─ RabbitMQ: send process.completed
   └─ /sale/return (异步, 长事务)
```

### 4.2 看自定义业务 Tag（核心加分项）

在 `UpSyncServiceImpl.insertIgnore()` 上加的 `TraceCtx.putTag` 会在 UI 显示：

| Tag | Value |
|-----|-------|
| `psi.tenant_id` | tenant_001 |
| `psi.sync.table` | order_main |
| `psi.sync.key` | ORDER20240717001 |
| `psi.sync.version` | 1 |
| `psi.sync.action` | INSERT / SKIP |
| `psi.sync.result` | SUCCESS / EXISTS |

**面试话术**：
> "我在幂等插入方法上加了自定义 tag，这样在 UI 上可以直接看到这条同步是 INSERT 还是 SKIP，业务出现重复数据时不用查 SQL 就能定位。"

### 4.3 看全局仪表盘

UI → Dashboard → Service Overview：
- 服务 SLA
- 响应时间 P50/P75/P90/P95/P99
- 每分钟请求数
- 服务依赖拓扑图（**架构师面试必看**）

---

## 5. 面试话术（直接背）

### 5.1 自我介绍环节
> "在 PSI 零售 SaaS 项目里我集成了 Apache SkyWalking 做可观测性，用 Java Agent 方式接入，业务代码零侵入，7 个 Java 微服务全部自动上报 trace、metric、log 到 OAP，再写入 Elasticsearch。我额外写了一个 `psi-observability` 公共模块，统一管理 tag 命名和自定义埋点工具。"

### 5.2 被问 "你怎么排查线上问题？"
> "先用 SkyWalking UI 看 trace，找到慢的 span。如果是个 SQL 慢，就点进去看实际的 SQL 和执行时间。如果是跨服务调用慢，看每个 hop 的耗时。如果是某个租户的某条数据问题，就用我加的 `psi.tenant_id` tag 过滤 trace。日志和 trace 通过 traceId 关联，我让 Logback 集成了 SkyWalking 的 toolkit，grep traceId 就能拉出整条链路的日志。"

### 5.3 被问 "为什么用 SkyWalking 而不是 Prometheus？"
> "Prometheus 是 Metrics 之王，但 Tracing 还得接 Jaeger/Tempo，Logging 还得接 Loki/Loki-Studio，三个系统运维成本高。SkyWalking 一个 OAP 搞定三大支柱，自带 UI，对 Spring Boot 生态支持最完善（Tomcat/Feign/MyBatis/JDBC/MQ/Redis 全部自动埋点）。对中小团队来说投入产出比最高。"

### 5.4 被问 "Trace 上下文怎么传递的？"
> "两个层次。第一，SkyWalking Agent 通过 ThreadLocal 自动透传，HTTP 通过 W3C traceparent 头传递，Feign 自动注入，RabbitMQ 通过 SkyWalking 自带的 plugin 自动注入 message header。第二，跨线程我用阿里 `TransmittableThreadLocal`，业务代码里 `TraceContext.traceId()` 拿到的就是当前 traceId。"

### 5.5 被问 "采样策略怎么定？"
> "我们业务量不算大（赞比亚小商户，几十到几百 POS），所以默认 100% 全采样。但 SkyWalking 支持尾部采样（tail-based sampling），可以在 OAP 端配置规则，比如只保留 P99 > 1s 的慢请求。在 OAP 配置里改 `agent-analyzer/default/sampleRate` 即可。"

---

## 6. 文件清单

| 文件 | 作用 |
|------|------|
| [docker-compose-observability.yml](file:///E:/spring%20boot/psi-parent/docker-compose-observability.yml) | SkyWalking + ES7 一键编排 |
| [docker/skywalking/agent.config](file:///E:/spring%20boot/psi-parent/docker/skywalking/agent.config) | Agent 行为配置 |
| [docker/entrypoint.sh](file:///E:/spring%20boot/psi-parent/docker/entrypoint.sh) | 容器启动脚本（自动注入 Agent） |
| [psi-observability/pom.xml](file:///E:/spring%20boot/psi-parent/psi-observability/pom.xml) | 自定义埋点模块 |
| [psi-observability/.../PsiTags.java](file:///E:/spring%20boot/psi-parent/psi-observability/src/main/java/com/psi/observability/constant/PsiTags.java) | Tag 名常量 |
| [psi-observability/.../TraceCtx.java](file:///E:/spring%20boot/psi-parent/psi-observability/src/main/java/com/psi/observability/util/TraceCtx.java) | 业务埋点工具 |
| [psi-observability/.../PsiTraceAspect.java](file:///E:/spring%20boot/psi-parent/psi-observability/src/main/java/com/psi/observability/aspect/PsiTraceAspect.java) | @PsiTrace 注解切面 |
| [start-observability.bat](file:///E:/spring%20boot/psi-parent/start-observability.bat) | Windows 启动脚本 |
| [start-with-trace.bat](file:///E:/spring%20boot/psi-parent/start-with-trace.bat) | Windows 单服务启动 |
| 4 个服务 Dockerfile | 已更新为 SkyWalking 注入 |

---

## 7. 常见问题

**Q1: Agent 启动报 "no plugin available"？**
A: 确认 `docker/skywalking/agent/plugins/` 下有 jar，下载的是 `apache-skywalking-java-agent-x.x.x.tgz`（不是 oap-server）。

**Q2: UI 上看不到服务？**
A: 1) 确认服务确实起来了 2) `agent.service_name` 是否被覆盖 3) OAP 是否健康（`curl http://localhost:12800/status`）。

**Q3: ES 占内存太高？**
A: 改 `docker-compose-observability.yml` 里 `ES_JAVA_OPTS` 和 `SW_ES_JAVA_OPTS` 调到 256m。

**Q4: 业务代码里能完全不用 Agent 吗？**
A: 可以，注释掉 `docker-compose-full.yml` 里 `JAVA_OPTS` 那行就是纯净启动。但面试时记得讲 "默认会接 Agent 演示 trace"。
