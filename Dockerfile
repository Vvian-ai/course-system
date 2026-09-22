# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

# 单元测试由 CI 负责（见 .github/workflows/ci.yml），镜像构建时跳过以加快速度
RUN mvn -B clean package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 以非 root 用户运行，降低容器内被提权的影响
RUN useradd -r -u 1001 appuser

COPY --from=build /build/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
