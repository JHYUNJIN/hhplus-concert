FROM openjdk:17-jdk-slim
ARG JAR_FILE=gateway-service/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
