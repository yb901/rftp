# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY . .
RUN ./gradlew --no-daemon :services:rf-mng:rf-mng-provider:bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""

COPY --from=builder /workspace/services/rf-mng/rf-mng-provider/build/libs/*.jar /app/app.jar

EXPOSE 18091
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
