
FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
ARG SPRING_PROFILES_ACTIVE=dev
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

RUN mkdir -p /home/app
WORKDIR /home/app
#COPY proxy-client/ .
#ADD proxy-client/target/proxy-client-v${PROJECT_VERSION}.jar proxy-client.jar
COPY target/proxy-client-v0.1.0.jar proxy-client.jar
EXPOSE 8900
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "proxy-client.jar"]
