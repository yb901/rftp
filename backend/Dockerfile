# 使用 Java 21 运行 Spring Boot 服务，云效流水线通过 JAR_FILE 指定具体服务产物。
FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com/openanolis/dragonwell:21

RUN groupadd --gid 1000 appgroup && \
    useradd --uid 1000 --gid appgroup --home-dir /app --create-home --shell /bin/sh appuser

WORKDIR /app

RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

ARG JAR_FILE
COPY --chown=appuser:appgroup ${JAR_FILE} app.jar

USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPS -jar app.jar"]
