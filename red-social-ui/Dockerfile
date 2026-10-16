# syntax=docker/dockerfile:1
# Contexto de build = raíz del monorepo (necesita el pom padre y red-social-common).
# El build de Vaadin descarga Node/npm y compila el bundle del frontend (-Pproduction).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY red-social-common/pom.xml red-social-common/
COPY red-social-api/pom.xml red-social-api/
COPY red-social-ui/pom.xml red-social-ui/

COPY red-social-common/src red-social-common/src
COPY red-social-ui/src red-social-ui/src

RUN mvn -B -pl red-social-ui -am -Pproduction -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/red-social-ui/target/red-social-ui-*.jar app.jar
RUN chown -R app:app /app

USER app
EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
