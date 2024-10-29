# construcción
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /bankingApp
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /bankingApp
COPY --from=build /bankingApp/target/bankingapp-0.0.1-SNAPSHOT.jar bankingApp.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "bankingApp.jar"]
