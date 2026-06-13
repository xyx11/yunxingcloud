FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY . .
ARG SERVICE=yunxingcloud-core
RUN ./mvnw package -pl ${SERVICE} -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
ARG SERVICE=yunxingcloud-core
ARG PORT=8080
COPY --from=builder /build/${SERVICE}/target/*.jar app.jar
RUN mkdir -p uploads/backup logs && chown -R app:app /app
USER app
EXPOSE ${PORT}
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
  CMD wget -qO- http://localhost:${PORT}/actuator/health || exit 1
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
