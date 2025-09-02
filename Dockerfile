############################
# Etapa de build (Maven)
############################
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests=true package

############################
# Etapa de runtime (JRE)
############################
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=""

ENTRYPOINT ["java","-jar","/app/app.jar"]