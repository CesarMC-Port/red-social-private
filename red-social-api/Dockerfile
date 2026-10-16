# syntax=docker/dockerfile:1
# Contexto de build = raíz del monorepo (necesita el pom padre y red-social-common).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY red-social-common/pom.xml red-social-common/
COPY red-social-api/pom.xml red-social-api/
COPY red-social-ui/pom.xml red-social-ui/

COPY red-social-common/src red-social-common/src
COPY red-social-api/src red-social-api/src

RUN mvn -B -pl red-social-api -am -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/red-social-api/target/red-social-api-*.jar app.jar
RUN chown -R app:app /app

USER app
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
