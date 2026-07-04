# ============================================
# yunxingcloud 多阶段 Docker 构建
# 用法: docker build --build-arg SERVICE=core -t yunxingcloud-core .
# ============================================

# ---- 阶段 1: 前端构建 ----
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ---- 阶段 2: 后端构建 ----
FROM eclipse-temurin:17-jdk-alpine AS backend-builder
WORKDIR /build
COPY . .
COPY --from=frontend-builder /frontend/dist/ /build/yunxingcloud-core/src/main/resources/static/
ARG SERVICE=yunxingcloud-core
RUN ./mvnw package -pl ${SERVICE} -am -DskipTests -q

# ---- 阶段 3: 运行时 ----
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
ARG SERVICE=yunxingcloud-core
ARG PORT=8080
COPY --from=backend-builder /build/${SERVICE}/target/*.jar app.jar
RUN mkdir -p uploads/backup logs && chown -R app:app /app
USER app
EXPOSE ${PORT}
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=90s \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
# OpenTelemetry Java Agent (可选, 通过 OTEL_ENABLED=true 启用)
ADD --chown=app:app https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/otel/
ENV OTEL_ENABLED=false
ENTRYPOINT ["sh", "-c", "if [ \"$OTEL_ENABLED\" = \"true\" ]; then \
  java $JAVA_OPTS -javaagent:/app/otel/opentelemetry-javaagent.jar -jar app.jar; \
  else java $JAVA_OPTS -jar app.jar; fi"]
