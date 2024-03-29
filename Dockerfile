FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY lombok.config /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM eclipse-temurin:21-alpine
COPY --from=build /home/app/target/chaos-bot-0.0.1-SNAPSHOT.jar /usr/local/lib/chaos.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/chaos.jar"]