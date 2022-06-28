FROM openjdk:11-jre-slim

ARG JAR_FILE=build/libs/metaticket-0.0.1-SNAPSHOT.jar

RUN mkdir /tmp/uploadImg

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]