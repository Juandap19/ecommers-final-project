
FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
#COPY cloud-config/ .
COPY target/cloud-config-v0.1.0.jar cloud-config.jar
EXPOSE 9296
ENTRYPOINT ["java", "-jar", "cloud-config.jar"]