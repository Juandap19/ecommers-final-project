
FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0

ARG SPRING_PROFILES_ACTIVE=dev
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

RUN mkdir -p /home/app
WORKDIR /home/app
#COPY service-discovery/ .
#ADD service-discovery/target/service-discovery-v${PROJECT_VERSION}.jar service-discovery.jar
COPY target/service-discovery-v0.1.0.jar service-discovery.jar
EXPOSE 8761
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "service-discovery.jar"]
