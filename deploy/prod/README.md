# 生产环境部署说明

生产环境参考 `zy_qy` 的模式，使用云效 Flow 构建镜像并部署到 ACK/Kubernetes。

## 服务清单

- `rf-mng`：管理端后端，镜像由 `deploy/prod/aliyun-pipeline-rf-mng-backend.yaml` 构建。
- `rf-performance`：员工绩效后端，镜像由 `deploy/prod/aliyun-pipeline-rf-performance-backend.yaml` 构建。
- `rf-mng-node`：管理端前端，镜像由 `deploy/prod/aliyun-pipeline-rf-mng-node.yaml` 构建。
- `rf-h5-node`：员工绩效 H5，镜像由 `deploy/prod/aliyun-pipeline-rf-h5-node.yaml` 构建。

## 云效变量

四条流水线均复用 `zy_qy` 的云效资源：

- `CR_USER_NAME`：镜像仓库用户名。
- `CR_PWD`：镜像仓库密码。
- `serviceConnection`：`dgqwgyszp67p6z61`。
- `runsOn.group`：`private/PMCrDXq0zBgals4t`。
- 镜像仓库：`qy-prd-registry-vpc.cn-hangzhou.cr.aliyuncs.com/qy-prod`。

前端流水线还需要配置：

- `CDN_BASE_URL`：CDN 根地址或当前前端应用的完整 CDN 前缀，例如 `https://cdn.zcglhr.com`、`https://cdn.zcglhr.com/rf-mng-node/`、`https://cdn.zcglhr.com/rf-h5-node/`。流水线会按 `APP_CDN_NAME` 自动补齐应用路径。
- `APP_CDN_NAME`：前端应用在 CDN/OSS 下的项目目录名；管理端默认 `rf-mng-node`，员工端 H5 默认 `rf-h5-node`。构建资源前缀和 OSS 上传目录都从该变量派生，必须保持一致。
- `API_BASE_URL`：当前前端应用访问后端 API 的根地址，例如管理端 `https://mng.zcglhr.com`；H5 默认留空，直接请求同域 `/performance/*`，由外层网关转发到 `rf-performance`。
- `OSS_ENDPOINT`：OSS endpoint。
- `OSS_BUCKET`：静态资源 bucket。
- `OSS_ACCESS_KEY_ID`：OSS 访问密钥 ID。
- `OSS_ACCESS_KEY_SECRET`：OSS 访问密钥 Secret。
- `NGINX_IMAGE`：Nginx 基础镜像。

四条流水线的仓库地址均为：

```text
https://codeup.aliyun.com/6a0e7b2c7b6e0a0129639206/rfpt/rfpt.git
```

`KubectlApply.kubernetesCluster` 与 `zy_qy` 一样在云效 Flow 页面选择真实 ACK 集群连接，YAML 中不写死集群 ID。

## ACK 预置资源

ACK/K8s 基础资源复用 `zy_qy`：

- 命名空间：`prod`
- 镜像拉取：`acr-secret`
- Nacos Config 入口：`qy-backend-nacos-config`
- 配置密文解密密钥：`qy-backend-config-crypto-secret`

`rf-mng` 和 `rf-performance` 的本地 `application.properties` 只保留启动引导配置，真实配置按 `zy_qy` 的两层结构从 Nacos 读取：

1. `common-backend-prod.properties`：直接复用 `zy_qy` 已维护的后端公共基础设施配置，不在 `rf` 仓库单独维护样例。
2. `${spring.application.name}-prod.properties`：项目自己的配置。

生产只需要在 Nacos 新增 `rf` 自己的服务配置：

| Data ID | 说明 | 样例 |
| --- | --- | --- |
| `rf-mng-prod.properties` | 管理端后端数据库、ID 编解码、Cookie、Dubbo、数据库字段加密配置 | `backend/docs/config/rf-mng-prod.properties` |
| `rf-performance-prod.properties` | 员工绩效后端数据库、短信、验证码、XXL-JOB 执行器配置 | `backend/docs/config/rf-performance-prod.properties` |

Dubbo 注册、配置和元数据中心复用 `zy_qy` 公共配置里的 `ns-qy-dubbo` 命名空间；应用配置仍放在 `ns-qy-appconf`。如果服务启动后 Nacos 看不到 provider，优先确认当前环境实际读取的是 `zy_qy` 已维护的 `common-backend-prod.properties`。

敏感值使用 `SM4_密文`，解密密钥复用 `zy_qy` 的 `QY_CONFIG_CRYPTO_SECRET_KEY` 注入方式，不再为 rf 单独创建数据库、短信、Cookie 等 K8s Secret。

生产 Deployment 不需要、也不应引用 `rf-platform-db-secret`。如果 ACK 报错 `secret "rf-platform-db-secret" not found`，说明集群里仍在运行旧版 manifest 或云效流水线未使用最新提交。处理方式：

```bash
kubectl -n prod get deploy rf-mng rf-performance -o yaml | grep -n "rf-platform-db-secret\|secretKeyRef" -A 3
```

确认后重新使用最新流水线发布；最新 manifest 只引用：

- `qy-backend-nacos-config`
- `qy-backend-config-crypto-secret`

## 路由建议

- 管理端前端 `rf-mng-node` 暴露给后台域名。
- 员工端 H5 `rf-h5-node` 由外层网关按 `/h5/*` 路由到前端服务，当前绩效页面为 `/h5/performance`。
- `rf-mng-node` 的 `/api/`、`/mng/` 代理到 `rf-mng`。
- `/performance/*` 由外层网关代理到 `rf-performance`，员工端接口使用 `/performance/employee/*`，不在 `rf-h5-node` Nginx 内反代。

## 注意事项

- `rf-mng` 生产配置需要在 `rf-mng-prod.properties` 中设置：
  - `id-codec.enabled=true`：启用 ID 编解码自动配置；接口字段仍需要在代码中使用 `@IdEncode` / `@IdDecode` 标注才会实际生效。
  - `rf.robot.schema-init.enabled=true`：启动时通过 Java 初始化逻辑幂等补齐社保机器人协作字段和索引，不使用存储过程。
  - `db.encrypt.enabled=true`、`db.encrypt.secrets.S1=SM4_密文` 和 `db.encrypt.tables.tb_admin.columns.otp_secret.*`：启用 `tb_admin.otp_secret` 字段透明加解密。
  - `dubbo.protocol.id=dubbo`、`dubbo.protocol.port=20891`、`dubbo.application.qos-port=22221`、`dubbo.scan.base-packages=com.rf.mng.provider.interfaces.remoteserviceimpl`：避免与其他生产服务默认端口冲突，并对齐 `zy_qy` 的 Dubbo 扫描配置。
- `rf-performance` 生产配置需要在 `rf-performance-prod.properties` 中设置：
  - `dubbo.protocol.id=dubbo`
  - `dubbo.protocol.port=20893`
  - `dubbo.scan.base-packages=com.rf.performance.provider.interfaces.remoteserviceimpl`
  - `rf-performance.sms.mock-enabled=false`
  - `rf-performance.sms.access-key-id=SM4_密文`
  - `rf-performance.sms.access-key-secret=SM4_密文`
  - `rf-performance.sms.sign-name=短信签名`
  - `rf-performance.sms.template-code=短信模板CODE`
  - `rf-performance.sms.captcha-prefix=验证码身份标识`
  - `rf-performance.sms.captcha-scene-id=验证码场景ID`
- XXL-JOB 需要在调度中心配置执行器 `rf-performance`，并添加任务 handler：`employeePerformanceAutoConfirmJob`。
- 数据库拆分为 `rf_pt` 和 `rf_robot`：平台业务表放入 `rf_pt`，tax-browser-worker 与 rf-mng 通过 `rf_robot` 税务机器人表交互，管理端不直连内网机器人服务。
- 生产上线前需要在 `rf_pt` 执行 `backend/services/rf-mng/sql/rf_pt/20260622_platform_admin.sql`、`backend/services/rf-performance/sql/20260621_employee_performance.sql` 和 `backend/services/rf-mng/sql/rf_pt/20260615_social_security_payment_management.sql`。
- 生产上线前需要在 `rf_robot` 执行 qy_robot 税务机器人基础表结构；社保协作队列字段和索引由 rf-mng 启动时 Java 初始化逻辑自动补齐。如需关闭自动补齐，再执行普通 DDL 脚本 `backend/services/rf-mng/sql/rf_robot/20260623_social_security_payment_task_queue.sql`。
