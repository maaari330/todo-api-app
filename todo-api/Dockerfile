# build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src

RUN mvn -Dmaven.test.skip=true clean package
# RUN mvn clean package -DskipTests

# runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/todo-api-0.0.1-SNAPSHOT.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]