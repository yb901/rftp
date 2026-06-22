# 生产环境部署说明

生产环境参考 `zy_qy` 的模式，使用云效 Flow 构建镜像并部署到 ACK/Kubernetes。

## 服务清单

- `rf-mng`：管理端后端，镜像由 `deploy/prod/aliyun-pipeline-rf-mng-backend.yaml` 构建。
- `rf-performance`：员工绩效后端，镜像由 `deploy/prod/aliyun-pipeline-rf-performance-backend.yaml` 构建。
- `rf-mng-node`：管理端前端，镜像由 `deploy/prod/aliyun-pipeline-rf-mng-node.yaml` 构建。
- `rf-h5-node`：员工绩效 H5，镜像由 `deploy/prod/aliyun-pipeline-rf-h5-node.yaml` 构建。

## 云效变量

四条流水线均需要配置：

- `ACR_REGISTRY`：阿里云镜像仓库地址，例如 `xxx.cn-hangzhou.cr.aliyuncs.com`。
- `ACR_NAMESPACE`：镜像命名空间。
- `CR_USER_NAME`：镜像仓库用户名。
- `CR_PWD`：镜像仓库密码。

流水线 YAML 中还包含以下占位符，需要在导入云效后替换：

- `RF_CODEUP_ENDPOINT_PLACEHOLDER`
- `RF_CODEUP_SERVICE_CONNECTION_PLACEHOLDER`
- `RF_YUNXIAO_RUNNER_GROUP_PLACEHOLDER`
- `RF_ACK_CLUSTER_PLACEHOLDER`

## ACK 预置资源

命名空间：`prod`

镜像拉取：

- `acr-secret`

配置：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rf-backend-config
  namespace: prod
data:
  dubbo-registry-address: nacos://nacos.prod.svc.cluster.local:8848
  xxl-job-admin-addresses: http://xxl-job-admin.prod.svc.cluster.local:8080/xxl-job-admin
```

平台主库密钥：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: rf-platform-db-secret
  namespace: prod
type: Opaque
stringData:
  url: jdbc:mysql://mysql.prod:3306/rf_pt?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
  username: rf_pt
  password: 请替换为生产密码
```

管理后台密钥：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: rf-mng-secret
  namespace: prod
type: Opaque
stringData:
  cookie-secret-key: 请替换为固定的16字节Base62编码SM4密钥
```

机器人协作库密钥：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: rf-robot-db-secret
  namespace: prod
type: Opaque
stringData:
  url: jdbc:mysql://mysql.prod:3306/rf_robot?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
  username: rf_robot
  password: 请替换为生产密码
```

员工绩效阿里云密钥：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: rf-performance-aliyun-secret
  namespace: prod
type: Opaque
stringData:
  sms-access-key-id: 请替换
  sms-access-key-secret: 请替换
  sms-sign-name: 请替换
  sms-template-code: 请替换
  captcha-prefix: 请替换
  captcha-scene-id: 请替换
```

## 路由建议

- 管理端前端 `rf-mng-node` 暴露给后台域名。
- 员工端 H5 `rf-h5-node` 暴露给公众号菜单域名或路径。
- `rf-mng-node` 的 `/api/` 代理到 `rf-mng`。
- `rf-h5-node` 的 `/api/performance/h5/` 代理到 `rf-performance`。

## 注意事项

- `rf-performance` 生产模板中已设置：
  - `RF_PERFORMANCE_H5_SMS_MOCK_ENABLED=false`
  - `RF_PERFORMANCE_H5_CAPTCHA_ENABLED=true`
- XXL-JOB 需要在调度中心配置执行器 `rf-performance`，并添加任务 handler：`employeePerformanceAutoConfirmJob`。
- 数据库拆分为 `rf_pt` 和 `rf_robot`：平台业务表放入 `rf_pt`，tax-browser-worker 与 rf-mng 交互的税务机器人表放入 `rf_robot`。
- 生产上线前需要在 `rf_pt` 执行 `backend/services/rf-mng/sql/rf_pt/20260622_platform_admin.sql`、`backend/services/rf-performance/sql/20260621_employee_performance.sql` 和 `backend/services/rf-mng/sql/rf_pt/20260615_social_security_payment_management.sql`。
- 生产上线前需要在 `rf_robot` 执行 qy_robot 税务机器人表结构。
