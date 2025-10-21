# build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# まず settings を入れてネットワーク強化
COPY ../maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .
# ネットワークが不安定でも数回リトライ (-U で更新)
RUN bash -lc '\
    for i in 1 2 3; do \
    mvn -B -U -DskipTests dependency:go-offline && exit 0; \
    echo "go-offline failed (attempt $i). Retrying in 5s..."; sleep 5; \
    done; \
    echo "go-offline failed after retries" >&2; exit 1 \
    '
# ここでソース投入して本ビルド
COPY src ./src
RUN mvn -Dmaven.test.skip=true clean package
# RUN mvn clean package -DskipTests

# runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/todo-api-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]